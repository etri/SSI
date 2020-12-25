package com.iconloop.iitp.friends.v1.web.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iconloop.iitp.friends.core.model.Delegated;
import com.iconloop.iitp.friends.core.model.Friend;
import com.iconloop.iitp.friends.core.repository.DelegatedRepository;
import com.iconloop.iitp.friends.core.repository.FriendsRepository;
import com.iconloop.iitp.friends.v1.web.model.FcmMessage;
import com.iconloop.iitp.friends.v1.web.model.FcmPushBody;
import com.iconloop.iitp.friends.v1.web.model.FcmPushBodyForEmployeeId;
import com.iconloop.iitp.friends.v1.web.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.codehaus.jackson.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * SSI-2차 과제
 * 설계 : https://icon-project.atlassian.net/wiki/spaces/DID/pages/758874122
 */
@Api(value = "Delegate API", consumes = "application/json", tags = {"Delegate"})
@RestController
@RequestMapping("/v1")
@Slf4j
public class DelegateController {
    @Autowired
    private DelegatedRepository delegatedRepository;
    @Autowired
    private FriendsRepository friendsRepository;

    private static final String FCM_PUSH_TITLE = "SSI-PUSH";

    @PostMapping("/credential/send")
    public String delegate(@RequestBody Map<String, Object> request) {
        String holder = (String) request.get("holder");
        String delegated = (String) request.get("delegated");
        String holderVc = (String) request.get("holderVc");
        String poaVc = (String) request.get("posVc");

        log.info("/credential/send:");
        if (holder == null || delegated == null || holderVc == null || poaVc == null) {
            return makeResponse(false, "Invalid argument");
        }

        Friend holderFa = friendsRepository.findFriendByDid((String) request.get("holder"));
        if (holderFa == null) {
            return makeResponse(false, "Not found holder did");
        }
        String holderMsToken = holderFa.getMsToken();
        if (holderMsToken == null || holderMsToken.length() < 10) {
            return makeResponse(false, "Holder's messaging service token is null");
        }

        Friend delegatedFa = friendsRepository.findFriendByDid((String) request.get("delegated"));
        if (delegatedFa == null) {
            return makeResponse(false, "Not found delegated did");
        }
        String deleMsToken = delegatedFa.getMsToken();
        if (deleMsToken == null || deleMsToken.length() < 10) {
            return makeResponse(false, "Delegated messaging service token is null");
        }

        String token = makeToken();
        log.info("/credential/send:token:" + token);

        // Save
        Delegated delegatedData = new Delegated(holder, delegated, holderVc, poaVc, token);
        delegatedRepository.save(delegatedData);

        // FCM Push
        // Make Push Body
        FcmPushBody pushBody = FcmPushBody.builder().action("delegate").token(token).build();
        Gson gson = new GsonBuilder().create();
        String pushJsonString = gson.toJson(pushBody);
        log.info("/credential/send:fcm_token:" + deleMsToken);
        log.info("/credential/send:push:" + pushJsonString);
        try {
            // 위임자의 FCM Token
            sendFirebaseMessage(deleMsToken, FCM_PUSH_TITLE, pushJsonString, null);
        } catch (Exception e) {
            e.printStackTrace();
            return makeResponse(false, "Fail to send push message");
        }

        return makeResponseWithToken(token);
    }

    @GetMapping("/credential/receive")
    public String getDelegatedVc(@RequestParam(value = "token") String token) {
        log.info("/credential/receive:" + token);
        Delegated delegatedRecode = delegatedRepository.findDelegatedByToken(token);
        if (delegatedRecode == null) {
            return makeResponse(false, "Not found delegated data");
        }
        String response = makeResponseDelegatedVc(delegatedRecode.getHolder(), delegatedRecode.getVcHolder(), delegatedRecode.getVcPoa());
        log.info("/credential/receive:res:" + response);

        // FCM Push
        Friend vcHolder = friendsRepository.findFriendByDid(delegatedRecode.getHolder());
        if (vcHolder == null) {
            return makeResponse(false, "Not found holder did");
        }

        FcmPushBody pushBody = FcmPushBody.builder().action("sendResult").token(delegatedRecode.getToken()).build();
        Gson gson = new GsonBuilder().create();
        String pushJsonString = gson.toJson(pushBody);
        log.info("/credential/receive:push:" + pushJsonString);
        try {
            // 홀더의 FCM token
            sendFirebaseMessage(vcHolder.getMsToken(), FCM_PUSH_TITLE, pushJsonString, null);
        } catch (Exception e) {
            e.printStackTrace();
            return makeResponse(false, "Fail to send push message");
        }

        return response;
    }

    /**
     * 20자리 Random 문자+숫자
     *
     * @return
     */
    private String makeToken() {
        StringBuffer temp = new StringBuffer();
        Random rnd = new Random();
        for (int i = 0; i < 20; i++) {
            int rIndex = rnd.nextInt(3);
            switch (rIndex) {
                case 0:
                    // a-z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 97));
                    break;
                case 1:
                    // A-Z
                    temp.append((char) ((int) (rnd.nextInt(26)) + 65));
                    break;
                case 2:
                    // 0-9
                    temp.append((rnd.nextInt(10)));
                    break;
                default:
                    break;
            }
        }
        temp.append(System.currentTimeMillis());
        return temp.toString();
    }

    /**
     * 사원증VC와 FCM token을 받아서 Push Message를 전달 한다.
     * @param request
     * @return
     */
    @ApiOperation(value = "사원증VC 전달")
    @PostMapping("/employee/send/vc")
    public String sendEmployeeVc(@RequestBody Map<String, Object> request) {
        log.info("sendEmployeeVc()");
        String holderFcmToken = (String) request.get("holderFcmToken");
        String employeeIdVc = (String) request.get("employeeIdVc");
        log.info("holderFcmToken:" + holderFcmToken);
        log.info("employeeIdVc:" + employeeIdVc);

        FcmPushBodyForEmployeeId pushBody = FcmPushBodyForEmployeeId.builder().action("sendEmployeeIdVc").employeeIdVc(employeeIdVc).build();
        Gson gson = new GsonBuilder().create();
        String pushJsonString = gson.toJson(pushBody);
        log.info("sendEmployeeVc()" + pushJsonString);
        try {
            // 홀더의 FCM token
            sendFirebaseMessage(holderFcmToken, FCM_PUSH_TITLE, pushJsonString, null);
        } catch (Exception e) {
            e.printStackTrace();
            return makeResponse(false, "Fail to send push message");
        }

        return makeResponseSuccess();
    }

    /**
     * FCM notification 에서 body 부분을 jsonMsg라는 것으로 받아서 메세지를 보내다. (20201204 - 사용하지 않음) sendPushMessageData() 사용하자!!
     * @param request
     * @return
     */
    @ApiOperation(value = "FCM Push 보내기")
    @PostMapping("/fcm/send")
    public String sendPushMessage(@RequestBody Map<String, Object> request) {
        log.info("sendPushMessage()");
        String fcmToken = (String) request.get("fcmToken");
        String jsonMsg = (String) request.get("jsonMsg");   // notification body
        log.info("holderFcmToken:" + fcmToken);
        log.info("jsonMsg:" + jsonMsg);

        try {
            // 홀더의 FCM token
            sendFirebaseMessage(fcmToken, FCM_PUSH_TITLE, jsonMsg, null);
        } catch (Exception e) {
            e.printStackTrace();
            return makeResponse(false, "Fail to send push message");
        }
        return makeResponseSuccess();
    }

    /**
     * notificationBody 와 data를 받아서 notification을 보낸다.
     * notificationBody 가 없으면 data 만 보내고, 그 반대도 마찬가지다.
     * notificationBody는 message string을 받고, data는 Json Map을 받는다.
     * @param request
     * @return
     */
    @ApiOperation(value = "FCM Push 보내기")
    @PostMapping("/fcm/send/data")
    public String sendPushMessageData(@RequestBody Map<String, Object> request) {
        log.info("sendPushMessage()");
        String fcmToken = (String) request.get("fcmToken");
        String notificationBody = (String) request.get("notificationBody");   // notification body
        Map data = (Map) request.get("data");       //
        log.info("holderFcmToken:" + fcmToken);
        log.info("notificationBody:" + notificationBody);

        try {
            // 홀더의 FCM token
            sendFirebaseMessage(fcmToken, FCM_PUSH_TITLE, notificationBody, data);
        } catch (Exception e) {
            e.printStackTrace();
            return makeResponse(false, "Fail to send push message");
        }
        return makeResponseSuccess();
    }

    //////////////////////////
    // Firebase Messaging
    //////////////////////////
    private void sendFirebaseMessage(String token, String title, String body, Map fcmData) throws Exception {
        String fcmKeyfileName = "ssi-fcm-firebase-adminsdk-lv82o-f95f434596.json";
        Path fcmKeyFilePath = Paths.get(File.separatorChar + fcmKeyfileName);
        log.info("FCM KEY PATH:" + fcmKeyFilePath.toString());
        InputStream stream = getClass().getResourceAsStream(fcmKeyFilePath.toString());

        String apiURL = "https://fcm.googleapis.com/v1/projects/ssi-fcm/messages:send";
        String pushMessage = makeFcmPushJsonMessage(token, title, body, fcmData);
        log.info("PUSH_JSON_MSG:" + pushMessage);

        String messageScope = "https://www.googleapis.com/auth/firebase.messaging";
        String[] messageScopes = {messageScope};


        GoogleCredential googleCredential = GoogleCredential
                .fromStream(stream)
                .createScoped(Arrays.asList(messageScopes));
        googleCredential.refreshToken();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + googleCredential.getAccessToken())
                .addHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .url(apiURL)
                .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), pushMessage)) //POST로 전달할 내용 설정
                .build();

        //동기 처리시 execute함수 사용
        Response response = client.newCall(request).execute();
        String message = response.body().string();
        log.info("PUSH_RESULT:" + message);
        if(stream != null) {
            stream.close();
        }
    }

    private String makeFcmPushJsonMessage(String token, String title, String body) throws JsonProcessingException {
        FcmMessage.Notification notification = FcmMessage.Notification.builder()
                .title(title)
                .body(body)
                .image(null)
                .build();

        FcmMessage.Message msg = FcmMessage.Message.builder()
                .token(token)
                .notification(notification)
                .build();

        FcmMessage fcmMessage = FcmMessage.builder()
                .message(msg)
                .validate_only(false)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(fcmMessage);
        return json;
    }

    /**
     * FCM Message 중 notification 과 data를 모두 보내는 API. notification 과 data를 추가하는데 없다면 넣지 않는다.
     * @param token
     * @param notificationBody
     * @param fcmData
     * @return
     */
    private String makeFcmPushJsonMessage(String token, String notificationTitle, String notificationBody, Map fcmData) {

        FcmMessage.Notification notification = null;
        if(notificationTitle != null && notificationBody != null) {
            notification = FcmMessage.Notification.builder()
                    .title(notificationTitle)
                    .body(notificationBody)
                    .image(null)
                    .build();
        }

        FcmMessage.Message msg = FcmMessage.Message.builder()
                .token(token)
                .notification(notification)
                .data(fcmData)
                .build();

        FcmMessage fcmMessage = FcmMessage.builder()
                .message(msg)
                .validate_only(false)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(fcmMessage);
        return json;
    }

    /////////////////////////////
    // Response
    /////////////////////////////
    private String makeResponseSuccess() {
        ResponseData responseData = ResponseData.builder()
                .status(true)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }

    private String makeResponse(boolean status, String errorMessage) {
        ResponseData responseData = ResponseData.builder()
                .status(status)
                .errorMessage(errorMessage)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }

    private String makeResponseWithToken(String token) {
        ResponseData responseData = ResponseData.builder()
                .status(true)
                .token(token)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }


    private String makeResponseDelegatedVc(String holderDid, String holderVc, String poaVc) {
        ResponseData responseData = ResponseData.builder()
                .status(true)
                .holderDid(holderDid)
                .holderVc(holderVc)
                .poaVc(poaVc)
                .build();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }
}
