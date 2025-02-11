#!/bin/bash
cd client
#sudo -u sgorrita ./run-client.sh
echo "******************************************************"
echo "***************Start launch kafka*********************"
echo "******************************************************" 
docker-compose -f docker-compose-fhir-hl7.yml down -v
sleep 5
docker-compose -f docker-compose-fhir-hl7.yml up -d
echo "*******************************************************"
echo "*****************End launch kafka**********************"
echo "*******************************************************"
sleep 5
export PATH="/mnt/c/Program Files/apache-maven-3.6.3/bin:$PATH"
echo "*******************************************************"
echo "**************Start launch provider********************"
echo "*******************************************************" 
cd  FHIRHl7Kafka/FHIRHl7KafkaProvider
#mvn spring-boot:run &
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005" &
echo "*******************************************************"
echo "***************End launch provider*********************"
echo "*******************************************************" 
cd -
echo "*******************************************************"
echo "**************Start launch consumer********************"
echo "*******************************************************" 
sleep 10
cd FHIRHl7Kafka/FHIRHl7KafkaConsumer
#mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5006"
echo "*******************************************************"
echo "**************Start launch consumer********************"
echo "*******************************************************" 