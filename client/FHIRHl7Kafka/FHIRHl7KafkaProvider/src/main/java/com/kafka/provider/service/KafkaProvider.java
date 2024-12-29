package com.kafka.provider.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProvider {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MessageGenerator messageGenerator;

    AtomicInteger atomicInteger = new AtomicInteger();

    public void sendMessages(String topic, int size) {
        for (int i = 0; i < size; i++) {
            String message = messageGenerator.generatePatientMessage(atomicInteger.getAndIncrement());
            sendMessage(topic, message);
        }
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("Sent message: " + message);
    }
}
