#!/bin/bash
#cd client
#sudo -u sgorrita ./run.sh
export PATH="/mnt/c/Program Files/apache-maven-3.6.3/bin:$PATH"
echo "mvn provider"
cd  FHIRHl7Kafka/FHIRHl7KafkaProvider/
echo "Current Directory: $(pwd)"
mvn clean
#mvn -DskipTest package
mvn -DskipTest -Dspring.profiles.active=test package
sleep 10
cd -
sleep 10
echo "mvn consumer"
cd  FHIRHl7Kafka/FHIRHl7KafkaConsumer/
mvn clean
mvn -DskipTest package
cd -

docker-compose -f docker-compose-fhir-hl7.yml up -d