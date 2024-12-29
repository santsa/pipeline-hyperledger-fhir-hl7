package com.kafka.provider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kafka.provider.service.KafkaProvider;

@RestController
@RequestMapping("/fabric")
public class FakeProducerController {

    @Autowired
    private KafkaProvider kafkaProducer;

    @GetMapping("/send-messages-fhir/{size}")
    public String sendMessagesFhir(@PathVariable int size) {
        kafkaProducer.sendMessages("FHIR-messages-patient", size);
        return "Messages fhir sent!";
    }
    
    @GetMapping("/send-messages-hl7/{size}")
    public String sendMessagesHl7(@PathVariable int size) {
        kafkaProducer.sendMessages("HL7-messages-patient", size);
        return "Messages hl7 sent!";
    }
}

