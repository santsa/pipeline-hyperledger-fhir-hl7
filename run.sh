#!/bin/bash
#git clone https://github.com/salva/pipeline-hyperledger-fhir-hl7.git
#cd pipeline-hyperledger-fhir-hl7
#sudo -u sgorrita ./run.sh

echo "*******************************************************"
echo "************************run****************************"
echo "*******************************************************"
find . -type f -name "*.java" -exec sed -i 's/[ \t]*$//' {} \;
export PATH_HOME=${PWD}

# docker stop $(docker ps -a -q)
# docker rm $(docker ps -a -q)
# docker volume prune
# docker network prune

rm -rf organizations/peerOrganizations
rm -rf organizations/ordererOrganizations
rm -rf channel-artifacts/
mkdir channel-artifacts

#generate certificates in this new net
export PATH=$PATH_HOME/bin:$PATH_HOME:$PATH
export FABRIC_CFG_PATH=$PATH_HOME/config
echo "*******************************************************"
echo "*********************cyptogen**************************"
echo "*******************************************************"
cryptogen generate --config=./organizations/cryptogen/crypto-config-org1.yaml --output="organizations"
cryptogen generate --config=./organizations/cryptogen/crypto-config-org2.yaml --output="organizations"
cryptogen generate --config=./organizations/cryptogen/crypto-config-orderer.yaml --output="organizations"

echo "*******************************************************"
echo "*******************launch the net**********************"
echo "*******************************************************"
#launch the net
#docker-compose -f docker/docker-compose-pipeline.yaml down -v
docker-compose -f docker/docker-compose-pipeline-couchdb.yaml down -v
sleep 10
#docker-compose -f docker/docker-compose-pipeline.yaml up -d
docker-compose -f docker/docker-compose-pipeline-couchdb.yaml up -d

sleep 5
echo "*******************************************************"
echo "*******************create channel**********************"
echo "*******************************************************"
#configure the nodes and channel
export FABRIC_CFG_PATH=$PATH_HOME/configtx
configtxgen -profile TwoOrgsApplicationGenesis -outputBlock ./channel-artifacts/proyectochannel.block -channelID proyectochannel

sleep 5
echo "*******************************************************"
echo "*******************add orderer*************************"
echo "*******************************************************"
# add orderer
export FABRIC_CFG_PATH=$PATH_HOME/config
export ORDERER_CA=$PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
export ORDERER_ADMIN_TLS_SIGN_CERT=$PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt
export ORDERER_ADMIN_TLS_PRIVATE_KEY=$PATH_HOME/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.key
osnadmin channel join --channelID proyectochannel --config-block ./channel-artifacts/proyectochannel.block -o localhost:7053 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY"
#osnadmin channel join -o orderer.example.com:7053 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY" --channelID proyectochannel --config-block ./channel-artifacts/proyectochannel.block
osnadmin channel list -o localhost:7053 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY"
#osnadmin channel list -o orderer.example.com:7053 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY"
#osnadmin channel list -o orderer.example.com:7053 --ca-file "$ORDERER_CA" --client-cert "$ORDERER_ADMIN_TLS_SIGN_CERT" --client-key "$ORDERER_ADMIN_TLS_PRIVATE_KEY" --channelID proyectochannel

sleep 5
echo "*******************************************************"
echo "*******************add node 1**************************"
echo "*******************************************************"
#add node 1
export CORE_PEER_TLS_ENABLED=true
export PEER0_ORG1_CA=$PATH_HOME/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG1_CA
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
peer channel join -b ./channel-artifacts/proyectochannel.block
peer channel list

sleep 5
echo "*******************************************************"
echo "*******************add node 2**************************"
echo "*******************************************************"
#add node 2
export PEER0_ORG2_CA=$PATH_HOME/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_LOCALMSPID="Org2MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=$PEER0_ORG2_CA
export CORE_PEER_MSPCONFIGPATH=$PATH_HOME/organizations/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=localhost:9051
peer channel join -b ./channel-artifacts/proyectochannel.block
peer channel list

peer channel getinfo -c proyectochannel

#./run-chaincode-go.sh
#./run-chaincode-basic-java.sh
./run-chaincode-hl7-fhir-java.sh