package com.iitp.iconloop.iitp.securities.icon;


import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import foundation.icon.icx.data.Address;

/**
 * 블록체인 네트워크 설정
 */
public class IconServiceConfig{
    // Icon network 설정
    String url;
    BigInteger networkId;
    Address scoreAddress;

    // http client 설정
    /**
     * {@link okhttp3.OkHttpClient.Builder#readTimeout(long, TimeUnit) readTimeout}
     */
    int readTimeout = 10_000;   // milli seconds
    /**
     * {@link okhttp3.OkHttpClient.Builder#writeTimeout(long, TimeUnit) writeTimeout}
     */
    int writeTimeout = 10_000;  // milli seconds

    public String getUrl(){
        return url;
    }

    public BigInteger getNetworkId(){
        return networkId;
    }

    public Address getScoreAddress(){
        return scoreAddress;
    }


    public int getReadTimeout(){
        return readTimeout;
    }

    public int getWriteTimeout(){
        return writeTimeout;
    }

    public static final class Builder{
        // Icon network 설정
        String url;
        BigInteger networkId;
        String scoreAddress;
        // http client 설정
        int readTimeout = 10_000;
        int writeTimeout = 10_000;

        public Builder(){
        }

        public Builder url(String url){
            this.url = url;
            return this;
        }

        public Builder networkId(BigInteger networkId){
            this.networkId = networkId;
            return this;
        }

        public Builder scoreAddress(String scoreAddress){
            this.scoreAddress = scoreAddress;
            return this;
        }

        public Builder readTimeout(int readTimeout){
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(int writeTimeout){
            this.writeTimeout = writeTimeout;
            return this;
        }

        public IconServiceConfig build() throws  IllegalArgumentException{
            if(url == null) throw new IllegalArgumentException("url is null");
            if(scoreAddress == null) throw new IllegalArgumentException("scoreAddress is null");
            if(networkId == null) throw new IllegalArgumentException("networkId is null");

            IconServiceConfig iconServiceConfig = new IconServiceConfig();
            iconServiceConfig.networkId = this.networkId;
            iconServiceConfig.readTimeout = this.readTimeout;
            iconServiceConfig.url = this.url.replace("/api/v3", "");
            iconServiceConfig.scoreAddress = new Address(this.scoreAddress);
            iconServiceConfig.writeTimeout = this.writeTimeout;
            return iconServiceConfig;
        }
    }
}
