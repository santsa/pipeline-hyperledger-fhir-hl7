#!/bin/bash
#git clone https://github.com/salva/pipeline-hyperledger-fhir-hl7.git
#cd pipeline-hyperledger-fhir-hl7
#sudo -u sgorrita ./scripts/00-start-all.sh

echo "*******************************************************"
echo "*****************Init 00-start-all.sh******************"
echo "*******************************************************"
#./scripts/01-start-net.sh
#./scripts/02-start-chaincode-hl7-fhir-java.sh
./scripts/03-start-client.sh
echo "*******************************************************"
echo "*****************End 00-start-all.sh*******************"
echo "*******************************************************"