#!/bin/bash
cd client
#sudo -u sgorrita ./scripts/03-start-client.sh
echo "******************************************************"
echo "***************Init 03-start-client*******************"
echo "******************************************************" 

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
docker-compose -f docker-compose-fhir-hl7.yml up --build -d
echo "*******************************************************"
echo "*********End launch client, server and kafka***********"
echo "*******************************************************"

echo "******************************************************"
echo "***************End 03-start-client***************]****"
echo "******************************************************" 