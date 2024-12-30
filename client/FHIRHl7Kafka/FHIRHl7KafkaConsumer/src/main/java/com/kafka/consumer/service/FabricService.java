package com.kafka.consumer.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.kafka.consumer.helper.FhirMessageProcessor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import org.hl7.fhir.r4.model.Patient;
import org.hyperledger.fabric.client.*;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class FabricService {

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	// Inject configuration values from application.properties
	@Value("${fabric.mspId}")
	private String mspId;

	@Value("${fabric.channelName}")
	private String channelName;

	@Value("${fabric.chaincodeName}")
	private String chaincodeName;

	@Value("${fabric.peerEndpoint}")
	private String peerEndpoint;

	@Value("${fabric.overrideAuth}")
	private String overrideAuth;

	@Value("${fabric.cryptoPath}")
	private String cryptoPath;

	@Value("${fabric.peer}")
	private String peer;

	@Value("${fabric.user}")
	private String user;

	private Gateway gateway;
	private ManagedChannel channel;
	private Contract contract;

	@Autowired
	FhirContext fhirContext;

	@Autowired
	private FhirMessageProcessor processor;

	@PostConstruct
	public void init() throws Exception {
		// Initialize the gateway connection
		this.channel = newGrpcConnection();
		Gateway.Builder builder = Gateway.newInstance()
				.identity(newIdentity())
				.signer(newSigner())
				.hash(Hash.SHA256)
				.connection(channel)
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		this.gateway = builder.connect();
		var network = gateway.getNetwork(channelName);
		this.contract = network.getContract(chaincodeName);
	}

	@PreDestroy
	public void cleanUp() throws InterruptedException {
		if (channel != null) {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private ManagedChannel newGrpcConnection() throws IOException {
		Path tlsCertPath = Paths.get(cryptoPath, "peers/" + peer + "/tls/ca.crt");
		var credentials = TlsChannelCredentials.newBuilder()
				.trustManager(tlsCertPath.toFile())
				.build();
		return Grpc.newChannelBuilder(peerEndpoint, credentials)
				.overrideAuthority(overrideAuth)
				.build();
	}

	private Identity newIdentity() throws IOException, CertificateException {
		Path certPath = Paths.get(cryptoPath, "users/" + user + "/msp/signcerts");
		try (var certReader = Files.newBufferedReader(getFirstFilePath(certPath))) {
			var certificate = Identities.readX509Certificate(certReader);
			return new X509Identity(mspId, certificate);
		}
	}

	private Signer newSigner() throws IOException, InvalidKeyException {
		Path keyPath = Paths.get(cryptoPath, "users/" + user + "/msp/keystore");
		try (var keyReader = Files.newBufferedReader(getFirstFilePath(keyPath))) {
			var privateKey = Identities.readPrivateKey(keyReader);
			return Signers.newPrivateKeySigner(privateKey);
		}
	}

	private Path getFirstFilePath(Path dirPath) throws IOException {
		try (var keyFiles = Files.list(dirPath)) {
			return keyFiles.findFirst().orElseThrow();
		}
	}

	public void initLedger()
			throws EndorseException, SubmitException, CommitStatusException, CommitException {
		log.info("\n--> Submit Transaction: InitLedger, function creates the initial set of assets on the ledger");
		var submitResult = contract.submitTransaction("InitLedger");
		String result = prettyJson(submitResult);
		log.info("*** Transaction committed successfully" + result);
	}

	public String getAll() throws GatewayException {
		log.info("\n--> Evaluate Transaction: GetAll, function returns all the current patients on the ledger");
		var evaluateResult = contract.evaluateTransaction("GetAllAssets");
		String result = prettyJson(evaluateResult);
		log.info("*** Result: " + result);
		return result;
	}

	public String readPatientById(String id) throws GatewayException {
		log.info("\n--> Evaluate Transaction: readPatient, function returns patient attributes");
		var evaluateResult = contract.evaluateTransaction("ReadAsset", id);
		String result = prettyJson(evaluateResult);
		log.info("*** Result:" + result);
		return result;
	}

	public boolean patientExists(String id) throws Exception {
		log.info("\n--> Evaluate Transaction: ReadAsset, function returns asset attributes");
		byte[] result = contract.evaluateTransaction("AssetExists", id);
		return Boolean.parseBoolean(new String(result));
	}

	public String updatePatientAsync(String pat) throws EndorseException, SubmitException, CommitStatusException {
		log.info("\n--> Async Submit Transaction: UpdateAsset");
		Patient patient = processor.parsePatientMessage(pat);
		if(patient == null){
			return "Invalid Patient";
		}
		var commit = contract.newProposal("UpdateAsset")
				.addArguments(patient.getId(), pat)
				.build().endorse().submitAsync();
		var result = commit.getResult();
		log.info("*** Waiting for transaction commit");
		var status = commit.getStatus();
		if (!status.isSuccessful()) {
			throw new RuntimeException("Transaction " + status.getTransactionId() +
					" failed to commit with status code " + status.getCode());
		}
		log.info("*** Transaction committed successfully " + status.isSuccessful());
		return status.toString();
	}

	public String createOrUpdatePatient(String patient) throws Exception{
		Patient pat = processor.parsePatientMessage(patient);
		if(pat == null){
			return "Invalid Patient";
		}
		if(pat.getId() == null || pat.getId().isEmpty() || pat.getId().isBlank()){
			pat.setId(UUID.randomUUID().toString());
			return createPatient(pat);
		}
		else if(!patientExists(pat.getIdElement().getIdPart())){
			pat.setId(pat.getIdElement().getIdPart());
			return createPatient(pat);
		}
		pat.setId(pat.getIdElement().getIdPart());
		return updatePatient(pat);
	}

	private String createPatient(Patient patient)
			throws EndorseException, SubmitException, CommitStatusException, CommitException {
		log.info("\n--> Submit Transaction: createPatient, creates new patient with arguments");
		var submitResult = contract.submitTransaction("CreateAsset", patient.getId(), encode(patient));
		String result = prettyJson(submitResult);
		log.info("*** Transaction committed successfully " + result);
		return result;
	}

	private String updatePatient(Patient patient) throws Exception {
		log.info("\n--> Submit Transaction: UpdatePatient");
		var submitResult = contract.submitTransaction("UpdateAsset", patient.getId(), encode(patient));
		String result = prettyJson(submitResult);
		log.info("*** Result:" + result);
		return result;
	}

	public String deletePatient(String id) throws Exception {
		log.info("\n--> Submit Transaction: deletePatient " + id);
		var submitResult = contract.submitTransaction("DeleteAsset", id);
		String result = prettyJson(submitResult);
		log.info("*** Result:" + result);
		return result;
	}

	private String prettyJson(final byte[] json) {
		return prettyJson(new String(json, StandardCharsets.UTF_8));
	}

	private String prettyJson(final String json) {
		var parsedJson = JsonParser.parseString(json);
		return gson.toJson(parsedJson);
	}

	private String encode(Patient patient) {
		IParser parser = fhirContext.newJsonParser();
		//parser.setPrettyPrint(true);
		return parser.encodeResourceToString(patient);
	}

}
