package com.kafka.consumer.helper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Meta;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

@Slf4j
@Component
public class FhirMessageProcessor {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private FhirContext fhirContext;

    public Patient parsePatientMessage(String fhirMessage) {
        String currentPath = Paths.get("").toAbsolutePath().toString();
        log.info("Current working directory: " + currentPath);
        if (validateResource(fhirMessage)) {
            return fhirContext.newJsonParser().parseResource(Patient.class, fhirMessage);
        }
        return null;
    }

    public boolean validateResource(String resource) {
        try {
            log.info("\n--> Init validate Resource");
            // Ask the context for a validator
            FhirValidator validator = fhirContext.newValidator();

            // Create a validation module and register it
            IValidatorModule module = new FhirInstanceValidator(fhirContext);
            validator.registerValidatorModule(module);
            ValidationResult result = validator.validateWithResult(resource);
            if (result.isSuccessful()) {
                log.info("Validation successful. No issues found.");
                return true;
            } else {
                StringBuilder outcome = new StringBuilder("Validation failed with issues:\n");
                OperationOutcome operationOutcome = (OperationOutcome) result.toOperationOutcome();
                operationOutcome.getIssue().forEach(issue -> {
                    outcome.append("Severity: ").append(issue.getSeverity()).append("\n");
                    outcome.append("Details: ").append(issue.getDiagnostics()).append("\n\n");
                });
                log.error(outcome.toString());
                return false;
            }
        } catch (Exception e) {
            log.error("\n--> Error during validation: " + e.getMessage());
            throw e;
        }
    }

    public String prettyJson(final byte[] json) {
		return prettyJson(new String(json, StandardCharsets.UTF_8));
	}

	private String prettyJson(final String json) {
		var parsedJson = JsonParser.parseString(json);
		return gson.toJson(parsedJson);
	}

	public String encode(Patient patient) {
		IParser parser = fhirContext.newJsonParser();
		return parser.encodeResourceToString(patient);
	}

    public String processHyperledgerResponse(String response) throws Exception {
        JsonObject responseObject = JsonParser.parseString(response).getAsJsonObject();
        String jsonValue = responseObject.get("jsonValue").getAsString();
        JsonObject patientObject = JsonParser.parseString(jsonValue).getAsJsonObject();
        JsonObject meta = new JsonObject();
        meta.addProperty("versionId", "1");
        meta.addProperty("lastUpdated", Instant.now().toString());
        meta.addProperty("source", "#hyperledger-fabric");
        patientObject.add("meta", meta);
        return gson.toJson(patientObject);
    }

}
