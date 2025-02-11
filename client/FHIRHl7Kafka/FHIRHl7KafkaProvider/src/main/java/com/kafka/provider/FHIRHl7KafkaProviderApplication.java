package com.kafka.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FHIRHl7KafkaProviderApplication {
	//mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005"
	public static void main(String[] args) {
		SpringApplication.run(FHIRHl7KafkaProviderApplication.class, args);
	}
}
