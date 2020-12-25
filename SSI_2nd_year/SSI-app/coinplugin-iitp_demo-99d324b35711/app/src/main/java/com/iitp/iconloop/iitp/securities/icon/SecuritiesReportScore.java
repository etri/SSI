package com.iitp.iconloop.iitp.securities.icon;


import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;

/**
 * Score interface 에 맞게 요청할 데이터 생성
 */
public class SecuritiesReportScore{

    private Address address;

    public SecuritiesReportScore(Address address){
        this.address = address;
    }

    /**
     * 호출하려는 score address
     *
     * @return score address
     */
    public Address getAddress(){
        return address;
    }

    /**
     * Score version 조회
     *
     * @return score 호출 정보를 갖고 있는 ScoreCall
     */
    public ScoreCall getVersion(){
        return new ScoreCall.Builder()
                .address(address)
                .method("getVersion")
                .readOnly(true)
                .build();
    }

    /**
     * 증권보고서 해시 저장
     *
     * @param hash 증권보고서 해시
     * @return score 호출 정보를 갖고 있는 ScoreCall
     */
    public ScoreCall putReport(String hash){
        validHash(hash);
        RpcObject params = new RpcObject.Builder()
                .put("_hash", new RpcValue(hash))
                .build();
        return new ScoreCall.Builder()
                .address(address)
                .method("putReport")
                .params(params)
                .build();
    }

    /**
     * 증권보고서 해시가 블록체인에 등록됐는지 확인
     *
     * @param hash 증권보고서 해시
     * @return score 호출 정보를 갖고 있는 ScoreCall
     */
    public ScoreCall hasReport(String hash){
        validHash(hash);
        RpcObject params = new RpcObject.Builder()
                .put("_hash", new RpcValue(hash))
                .build();
        return new ScoreCall.Builder()
                .address(address)
                .method("hasReport")
                .params(params)
                .build();
    }

    private void validHash(String hash) throws IllegalArgumentException{
        if(!isValidHex(hash)){
            throw new IllegalArgumentException("The value is not hex string.");
        }
    }

    private boolean isValidHex(String value){
        return value.matches("^[0-9a-fA-F]+$");
    }
}
