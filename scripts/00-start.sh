#!/bin/bash
#git clone https://github.com/salva/pipeline-hyperledger-fhir-hl7.git
#cd pipeline-hyperledger-fhir-hl7
#sudo -u sgorrita ./scripts/00-start.sh all

echo "*******************************************************"
echo "*****************Init 00-start-all.sh******************"
echo "*******************************************************"
function launchNet() {
    ./scripts/01-start-net.sh
}

function launchChaincode() {
    ./scripts/02-start-chaincode-hl7-fhir-java.sh
}

function launchClient() {
    ./scripts/03-start-client.sh
}

# Call the functions in the desired order
#launchNet
#launchChaincode
#launchClient

# Check if a parameter was passed
case "$1" in
    net)
        launchNet
        ;;
    chaincode)
        launchChaincode
        ;;
    client)
        launchClient
        ;;
    net-chaincode)
        launchNet
        launchChaincode
        ;;
    chaincode-client)
        launchChaincode
        launchClient
        ;;                
    all)
        launchNet
        launchChaincode
        launchClient
        ;;
    *)
        echo "Invalid option. Use: net | chaincode | client | net-chaincode | chaincode-client | all"
        exit 1
        ;;
esac

echo "*******************************************************"
echo "*****************End 00-start-all.sh*******************"
echo "*******************************************************"
