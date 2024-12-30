package com.kafka.consumer.helper;

import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FhirMessageProcessor {

    @Autowired
    private FhirContext fhirContext;

    public Patient parsePatientMessage(String fhirMessage) {
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

}
