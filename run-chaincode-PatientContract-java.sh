#!/bin/bash
#git clone https://github.com/salva/pipeline-hyperledger-fhir-hl7.git

#cd pipeline-hyperledger-fhir-hl7
#sudo -u sgorrita ./run-chaincode-PatientContract-java.sh
export PATH_HOME=${PWD}
export PATH=$PATH_HOME/bin:$PATH_HOME:$PATH
export FABRIC_CFG_PATH=$PATH_HOME/config

export PEER0_ORG2_CA=$PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051

cd chaincodes/chaincode-test/fabric-contract-hl7fhir
./gradlew clean
./gradlew build

cd $PATH_HOME

#export PATH=$PATH_HOME/bin:$PATH_HOME:$PATH
#export FABRIC_CFG_PATH=$PATH_HOME/config
peer version
peer lifecycle chaincode package PatientContract.tar.gz --path chaincodes/chaincode-test/fabric-contract-hl7fhir/build/libs --lang java --label PatientContract_1.0

export CORE_PEER_TLS_ENABLED=true
#change peer org1 and install
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
peer lifecycle chaincode install PatientContract.tar.gz

#change peer org2 and install
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051
peer lifecycle chaincode install PatientContract.tar.gz

peer lifecycle chaincode queryinstalled
#export CC_PACKAGE_ID=basic-java_1.0:xxxxxxxxxx.....
export CC_CHAIN_CODE_LABEL="PatientContract_1.0"
export CC_PACKAGE_ID=$(peer lifecycle chaincode queryinstalled | grep "$CC_CHAIN_CODE_LABEL" | awk -F 'Package ID: ' '{print $2}' | awk -F ', Label:' '{print $1}')

echo "approveformyorg"
sleep 10
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID proyectochannel --name PatientContract --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "approveformyorg"
sleep 10
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_ADDRESS=localhost:7051
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID proyectochannel --name PatientContract --version 1.0 --package-id $CC_PACKAGE_ID --sequence 1 --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "checkcommitreadiness"
sleep 10
peer lifecycle chaincode checkcommitreadiness --channelID proyectochannel --name PatientContract --version 1.0 --sequence 1 --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --output json

echo "commit"
sleep 10
peer lifecycle chaincode commit -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID proyectochannel --name PatientContract --version 1.0 --sequence 1 --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --peerAddresses localhost:7051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt

echo "querycommitted"
sleep 10
peer lifecycle chaincode querycommitted --channelID proyectochannel --name PatientContract --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "invoke"
echo $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
sleep 5
peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C proyectochannel -n PatientContract --peerAddresses localhost:7051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"InitLedger","Args":[]}'

echo "query"
sleep 5
peer chaincode query -C proyectochannel -n PatientContract -c '{"Args":["GetAllPatients"]}'

echo "invoke"
sleep 5
peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C proyectochannel -n PatientContract --peerAddresses localhost:7051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"createPatient","Args":["patient1","first patient"]}'

echo "query"
sleep 5
peer chaincode query -C proyectochannel -n PatientContract -c '{"Args":["GetAllPatients"]}'
