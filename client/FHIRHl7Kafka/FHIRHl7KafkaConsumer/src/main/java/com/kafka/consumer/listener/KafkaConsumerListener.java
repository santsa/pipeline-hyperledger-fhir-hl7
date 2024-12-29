package com.kafka.consumer.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

import com.kafka.consumer.service.FabricService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConsumerListener {

    @Autowired
    private FabricService fabricService;
    
    @KafkaListener(topics = "FHIR-messages-patient", groupId = "fhir-hl7-consumers")
    public void listenerFHIR(String message) {
        log.info("Received message: " + message);
        try {
            fabricService.createOrUpdatePatient(message);
        } catch (Exception e) {
            log.error("Error processing FHIR message: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "HL7-messages-patient", groupId = "fhir-hl7-consumers")
    public void listenerHL7(String message) {
        log.info("Received message: " + message);
    }

}
