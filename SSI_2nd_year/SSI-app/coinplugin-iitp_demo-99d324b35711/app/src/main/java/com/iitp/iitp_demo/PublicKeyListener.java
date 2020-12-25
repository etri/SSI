package com.iitp.iitp_demo;


public interface PublicKeyListener{

    void requestComplete(String publicKeyHex);

    void error(String error);

}
