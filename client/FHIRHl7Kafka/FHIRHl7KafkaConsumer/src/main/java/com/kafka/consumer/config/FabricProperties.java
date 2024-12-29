package com.kafka.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fabric")
public class FabricProperties {
    private String mspId;
    private String channelName;
    private String chaincodeName;
    private String peerEndpoint;
    private String overrideAuth;
    private String cryptoPath;

    // Getters and Setters
    public String getMspId() {
        return mspId;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChaincodeName() {
        return chaincodeName;
    }

    public void setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
    }

    public String getPeerEndpoint() {
        return peerEndpoint;
    }

    public void setPeerEndpoint(String peerEndpoint) {
        this.peerEndpoint = peerEndpoint;
    }

    public String getOverrideAuth() {
        return overrideAuth;
    }

    public void setOverrideAuth(String overrideAuth) {
        this.overrideAuth = overrideAuth;
    }

    public String getCryptoPath() {
        return cryptoPath;
    }

    public void setCryptoPath(String cryptoPath) {
        this.cryptoPath = cryptoPath;
    }
}
