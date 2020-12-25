package com.bd.ssi.common.api;

import org.graalvm.compiler.replacements.StringSubstitutions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class PgMngr {
    String UUID = "";
    String pgUUID = "";
    String VPs = "";



    String payJson = "" +
            "{" +
                "'iss': '${ISSUER_DID}'," +
                "'iat': ''," +
                "'id': '${UUID}'," +
                "'presentationURL': ''," +
                "'presentationRequest': {" +
                    "'criteria': [{" +
                        "'nonZKP': {" +
                            "'nonce': '123432421212'," +
                            "'name': ‘Market-Payment-Request'," +
                            "'version': ‘0.1'," +
                            "‘delegated_attributes: {" +
                                "‘delegated_attr1_referent': {" +
                                    "‘type’: ‘delegated_VC’," +
                                    "‘delegated_attr’: ‘CardTokenCredentiall’," +
                                    "‘did_delegator’: ‘${ISSUER_DID}’, " +
                                    "‘payment’: ‘${PRICE}’" +
                                "}" +
                            "}" +
                        "}," +
                        "'requested_attributes': {" +
                            "'attr1_referent': {" +
                                "'restrictions': [{'issuer': '${ISSUER_DID}'}, {‘type’: ‘CardTokenCredentiall’}]" +
                            "}," +
                            "'attr2_referent': {" +
                                "'restrictions': [{'issuer': '${ISSUER_DID}'}, {‘type’: ‘폰번호 VC type’}]" +
                            "}," +
                            "'attr3_referent': {" +
                                "'restrictions': [{'issuer': '${ISSUER_DID}'}, {‘type’: ‘주소 VC type’}]" +
                            "}" +
                        "}" +
                    "}]" +
                "}" +
            "}";



    public String getSendVpStr(String uuid, String pgUuid, String vp){
        /**
         * PG연동을 위해 payment_url로 요청을 하고 받은 VP를 분석해서 만든 리턴해야할 메세지
         */
        String sendVpStr = "" +
                "{" +
                    "\"id\":\"" + uuid + "\"," +
                    "\"presentationRequestId\":\"" + pgUuid + "\"," +
                    "\"Presentations\":\"" + vp + "\"" +
                "}";

        return sendVpStr;
    }

    public String callUrlMngr(String urlStr, String msg){
        String rcvMsg = "";

        try{
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.addRequestProperty("x-api-key", "RestTestCommon.API_KEY"); //key값 설정
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setDefaultUseCaches(false);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(msg); //json 형식의 message 전달
            wr.flush();

            StringBuilder sb = new StringBuilder();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //Stream을 처리해줘야 하는 귀찮음이 있음.
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                rcvMsg = sb.toString();
            } else {
                rcvMsg = "ERROR";
            }
        }catch(Exception ex){
            rcvMsg = "ERROR - EXCEPTION";
            ex.printStackTrace();
        }

        return rcvMsg;
    }
}
