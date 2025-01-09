#!/bin/bash
#git clone https://github.com/salva/pipeline-hyperledger-fhir-hl7.git
#cd pipeline-hyperledger-fhir-hl7
#sudo -u sgorrita ./run-chaincode-hl7-fhir-java.sh

echo "*******************************************************"
echo "**************run-chaincode-hl7-fhir-java**************"
echo "*******************************************************"
find . -type f -name "*.java" -exec sed -i 's/[ \t]*$//' {} \;
export PATH_HOME=${PWD}
export PATH=$PATH_HOME/bin:$PATH_HOME:$PATH
export FABRIC_CFG_PATH=$PATH_HOME/config

export PEER0_ORG2_CA=$PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051

echo "*******************************************************"
echo "*******************build chaincode*********************"
echo "*******************************************************"
cd chaincodes/chaincode-hl7-fhir
./gradlew clean
./gradlew build

cd $PATH_HOME

echo "*******************************************************"
echo "*******************package chaincode*******************"
echo "*******************************************************"
rm hl7-fhir-java.tar.gz
export VERSION="1.0"
export SEQUENCE="1"
peer version
peer lifecycle chaincode package hl7-fhir-java.tar.gz --path chaincodes/chaincode-hl7-fhir/build/libs/ --lang java --label hl7-fhir-java_$VERSION

echo "*******************************************************"
echo "***************install chaincode peer 1****************"
echo "*******************************************************"
export CORE_PEER_TLS_ENABLED=true
#change peer org1 and install
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
peer lifecycle chaincode install hl7-fhir-java.tar.gz

echo "*******************************************************"
echo "***************install chaincode peer 2****************"
echo "*******************************************************"
#change peer org2 and install
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051
peer lifecycle chaincode install hl7-fhir-java.tar.gz

echo "*******************************************************"
echo "***************queryinstalled chaincode****************"
echo "*******************************************************"
peer lifecycle chaincode queryinstalled
#export CC_PACKAGE_ID=hl7-fhir-java_1.0:xxxxxxxxxx.....
export CC_CHAIN_CODE_LABEL="hl7-fhir-java_$VERSION"
export CC_PACKAGE_ID=$(peer lifecycle chaincode queryinstalled | grep "$CC_CHAIN_CODE_LABEL" | awk -F 'Package ID: ' '{print $2}' | awk -F ', Label:' '{print $1}')

echo "*******************************************************"
echo "***********approveformyorg chaincode peer 1************"
echo "*******************************************************"
sleep 10
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID proyectochannel --signature-policy "OR('Org1MSP.member','Org2MSP.member')" --name hl7-fhir-java --version $VERSION --package-id $CC_PACKAGE_ID --sequence $SEQUENCE --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "*******************************************************"
echo "***********approveformyorg chaincode peer 1************"
echo "*******************************************************"
sleep 10
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_TLS_ROOTCERT_FILE=$PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_ADDRESS=localhost:7051
peer lifecycle chaincode approveformyorg -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --channelID proyectochannel --signature-policy "OR('Org1MSP.member','Org2MSP.member')" --name hl7-fhir-java --version $VERSION --package-id $CC_PACKAGE_ID --sequence $SEQUENCE --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "*******************************************************"
echo "************checkcommitreadiness chaincode*************"
echo "*******************************************************"
sleep 10
peer lifecycle chaincode checkcommitreadiness --channelID proyectochannel --name hl7-fhir-java --version $VERSION --sequence $SEQUENCE --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --output json

echo "*******************************************************"
echo "***************commit chaincode***********************"
echo "*******************************************************"
sleep 10
peer lifecycle chaincode commit -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --signature-policy "OR('Org1MSP.member','Org2MSP.member')" --channelID proyectochannel --name hl7-fhir-java --version $VERSION --sequence $SEQUENCE --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem --peerAddresses localhost:7051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt

echo "*******************************************************"
echo "***************querycommitted chaincode***************"
echo "*******************************************************"
echo "querycommitted"
sleep 10
peer lifecycle chaincode querycommitted --channelID proyectochannel --name hl7-fhir-java --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "*******************************************************"
echo "*******************invoke chaincode********************"
echo "*******************************************************"
#echo $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
sleep 5
peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile $PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C proyectochannel -n hl7-fhir-java --peerAddresses localhost:7051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles $PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c '{"function":"InitLedger","Args":[]}'

echo "*******************************************************"
echo "*******************query chaincode*********************"
echo "*******************************************************"
sleep 5
peer chaincode query -C proyectochannel -n hl7-fhir-java -c '{"Args":["GetAllAssets"]}'

#peer chaincode query -C proyectochannel -n hl7-fhir-java -c '{"Args":["ReadAsset","asset1"]}'