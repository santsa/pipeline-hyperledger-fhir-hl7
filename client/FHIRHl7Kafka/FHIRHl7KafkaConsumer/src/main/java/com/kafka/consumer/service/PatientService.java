package com.kafka.consumer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Hash;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.consumer.extensions.FhirMessageProcessor;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Getter
public class PatientService {

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

    private org.hyperledger.fabric.client.identity.Identity newIdentity() throws IOException, CertificateException {
        Path certPath = Paths.get(cryptoPath, "users/" + user + "/msp/signcerts");
        try (var certReader = Files.newBufferedReader(getFirstFilePath(certPath))) {
            var certificate = Identities.readX509Certificate(certReader);
            return new X509Identity(mspId, certificate);
        }
    }

    private org.hyperledger.fabric.client.identity.Signer newSigner() throws IOException, InvalidKeyException {
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
        String result = processor.prettyJson(submitResult);
        log.info("*** Transaction committed successfully" + result);
    }

    public Bundle search() throws GatewayException, JsonProcessingException {
        log.info("\n--> Evaluate Transaction: GetAll, function returns all the current patients on the ledger");
        var evaluateResult = contract.evaluateTransaction("GetAllAssets");
        return processor.decodeList(processor.prettyJson(evaluateResult));
    }

    public Patient searchById(String id) throws GatewayException, JsonProcessingException {
        log.info("\n--> Evaluate Transaction: readPatient, function returns patient attributes");
        var evaluateResult = contract.evaluateTransaction("ReadAsset", id);
        return (Patient) processor.decode(processor.prettyJson(evaluateResult)).get();
    }

    public boolean patientExists(String id) throws GatewayException {
        log.info("\n--> Evaluate Transaction: ReadAsset, function returns asset attributes");
        byte[] result = contract.evaluateTransaction("AssetExists", id);
        return Boolean.parseBoolean(new String(result));
    }

    public MethodOutcome updatePatientAsync(Patient patient) throws EndorseException, SubmitException, CommitStatusException {
        log.info("\n--> Async Submit Transaction: UpdateAsset");
        if (patient == null) {
            throw new InvalidRequestException("Patient is invalid");
        }
        MethodOutcome outcome = new MethodOutcome();
        /*ValidationResult result = processor.validateResource(patient);
        if (result.isSuccessful()) {*/
        var commit = contract.newProposal("UpdateAsset")
                .addArguments(patient.getId(), processor.encode(patient))
                .build().endorse().submitAsync();
        var resultCommit = commit.getResult();
        log.info("*** Waiting for transaction commit");
        var status = commit.getStatus();
        if (!status.isSuccessful()) {
            throw new RuntimeException("Transaction " + status.getTransactionId()
                    + " failed to commit with status code " + status.getCode());
        }
        outcome.setCreated(true);
        outcome.setId(new IdType("Patient", patient.getId()));
        outcome.setResource(patient);
        log.info("*** Transaction committed successfully " + status.isSuccessful() + "Transaction " + status.getTransactionId() + " failed to commit with status code " + status.getCode());
        /*} else {
            outcome.setCreated(false);
            outcome.setOperationOutcome(result.toOperationOutcome());
        }*/
        return outcome;
    }

    public MethodOutcome createOrUpdate(Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, Exception {
        if (patient == null) {
            throw new InvalidRequestException("Patient is invalid");
        }
        MethodOutcome outcome = new MethodOutcome();
        /*ValidationResult result = processor.validateResource(patient);
        if (result.isSuccessful()) {*/
        if (patient.getId() == null || patient.getId().isEmpty() || patient.getId().isBlank()) {
            patient.setId(UUID.randomUUID().toString());
            patient = createPatient(patient);
            outcome.setCreated(true);
        } else if (!patientExists(patient.getIdElement().getIdPart())) {
            patient.setId(patient.getIdElement().getIdPart());
            patient = createPatient(patient);
            outcome.setCreated(true);
        } else {
            patient.setId(patient.getIdElement().getIdPart());
            patient = updatePatient(patient);
            outcome.setCreated(false);
        }
        outcome.setId(new IdType("Patient", patient.getId()));
        outcome.setResource(patient);
        /*} else {
            outcome.setCreated(false);
            outcome.setOperationOutcome(result.toOperationOutcome());
        }*/
        return outcome;
    }

    private Patient createPatient(Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, Exception {
        log.info("\n--> Submit Transaction: createPatient, creates new patient with arguments");
        var submitResult = contract.submitTransaction("CreateAsset", processor.encode(patient));
        return (Patient) processor.decode(processor.prettyJson(submitResult)).get();
    }

    private Patient updatePatient(Patient patient) throws EndorseException, SubmitException, CommitException, CommitStatusException, Exception {
        log.info("\n--> Submit Transaction: UpdatePatient");
        var submitResult = contract.submitTransaction("UpdateAsset", processor.encode(patient));
        return (Patient) processor.decode(processor.prettyJson(submitResult)).get();
    }

    public MethodOutcome deletePatient(String id) throws EndorseException, SubmitException, CommitException, CommitStatusException, JsonProcessingException {
        log.info("\n--> Submit Transaction: deletePatient " + id);
        var submitResult = contract.submitTransaction("DeleteAsset", id);
        Patient patient = (Patient) processor.decode(processor.prettyJson(submitResult)).get();
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("Patient", patient.getId()));
        OperationOutcome opOutcome = new OperationOutcome();
        opOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                .setCode(OperationOutcome.IssueType.DELETED)
                .setDiagnostics("Patient successfully deleted.");
        outcome.setOperationOutcome(opOutcome);
        return outcome;
    }

}
