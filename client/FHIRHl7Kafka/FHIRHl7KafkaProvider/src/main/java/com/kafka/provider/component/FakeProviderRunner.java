package com.kafka.provider.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kafka.provider.service.KafkaProvider;

@Component
@Profile("!test")
public class FakeProviderRunner /*implements CommandLineRunner */{

    @Autowired
    private KafkaProvider kafkaProducer;

    @Value("${size.kafka.messages}")
    private int size;

    @Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
    //public void run(String... args) {
    public void run() {
        kafkaProducer.sendMessages("FHIR-messages-patient", size);
        kafkaProducer.sendMessages("HL7-messages-patient", size);
    }

}
