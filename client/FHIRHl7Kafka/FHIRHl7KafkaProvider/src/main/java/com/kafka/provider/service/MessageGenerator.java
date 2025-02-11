package com.kafka.provider.service;

import java.util.Random;
import java.util.UUID;

import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

@Service
public class MessageGenerator {

    //private final FhirContext fhirContextR5;
    private final FhirContext fhirContextR4;

    @Autowired
    public MessageGenerator(/*@Qualifier("fhirContextR5") FhirContext fhirContextR5,*/
            @Qualifier("fhirContextR4") FhirContext fhirContextR4) {
        //this.fhirContextR5 = fhirContextR5;
        this.fhirContextR4 = fhirContextR4;
    }

    public String generatePatientMessage() {
        Random random = new Random();
        Patient pat = new Patient();
        pat.setId(UUID.randomUUID().toString());
        pat.addName().setFamily("Simpson").addGiven("Homer").addGiven("J");
        pat.addIdentifier().setSystem("http://acme.org/MRNs").setValue(UUID.randomUUID().toString());
        pat.addTelecom().setUse(ContactPointUse.HOME).setSystem(ContactPointSystem.PHONE)
                .setValue("1 (416) 340-4800");
        pat.setGender(randomGender(random));
        pat.addAddress().setCity("boston").setPostalCode("12345");

        // FhirContext ctx = FhirContext.forR5Cached();
        IParser parser = fhirContextR4.newJsonParser();
        parser.setPrettyPrint(true);

        String encode = parser.encodeResourceToString(pat);
        System.out.println(encode);
        return encode;
    }

    private static AdministrativeGender randomGender(Random random) {
        // Randomly pick a gender
        int genderChoice = random.nextInt(3); // 0 = male, 1 = female, 2 = other
        return switch (genderChoice) {
            case 0 -> AdministrativeGender.MALE;
            case 1 -> AdministrativeGender.FEMALE;
            default -> AdministrativeGender.OTHER;
        };
    }
}
