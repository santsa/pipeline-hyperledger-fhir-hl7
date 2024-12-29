package com.kafka.consumer.service;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FhirMessageProcessor {

    @Autowired
    private FhirContext fhirContext;

    public Patient parsePatientMessage(String fhirMessage) {
        IParser parser = fhirContext.newJsonParser();        
        //Bundle b = (Bundle) fhirContext.newJsonParser().parseResource(fhirMessage);
        Patient pat = parser.parseResource(Patient.class, fhirMessage);
        return pat;
    }
}

