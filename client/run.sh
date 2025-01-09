#!/bin/bash
#cd client
#sudo -u sgorrita ./run.sh
export PATH="/mnt/c/Program Files/apache-maven-3.6.3/bin:$PATH"
echo "*******************************************************"
echo "************************mvn****************************"
echo "*******************************************************" 
cd  FHIRHl7Kafka/
#echo "Current Directory: $(pwd)"
mvn clean
#mvn -DskipTest package
mvn -DskipTest package
echo "*******************************************************"
echo "********Start launch client, server and kafka**********"
echo "*******************************************************" 
cd -
docker-compose -f docker-compose-fhir-hl7.yml down -v
sleep 5
docker-compose -f docker-compose-fhir-hl7.yml up -d
echo "*******************************************************"
echo "*********End launch client, server and kafka***********"
echo "*******************************************************"