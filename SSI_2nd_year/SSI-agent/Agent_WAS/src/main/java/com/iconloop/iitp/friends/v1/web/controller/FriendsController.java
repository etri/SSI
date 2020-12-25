package com.iconloop.iitp.friends.v1.web.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iconloop.iitp.friends.core.model.Friend;
import com.iconloop.iitp.friends.core.repository.FriendsRepository;
import com.iconloop.iitp.friends.v1.web.model.FcmMessage;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SSI-2차 과제
 * 설계 : https://icon-project.atlassian.net/wiki/spaces/DID/pages/758874122
 */
@Api(value = "Friends Management API", consumes = "application/json", tags = {"Friends"})
@RestController
@RequestMapping("/v1")
@Slf4j
public class FriendsController {

    @Autowired
    private FriendsRepository friendsRepository;


    @ApiOperation(value = "푸시 메세지 토큰등록",
            notes = "FCM 토을 등록한다.",
            response = ResponseData.class
    )
    /*
    @ApiImplicitParams({
            @ApiImplicitParam(name="holder", value="Holder's DID", required = true, dataType="json"),
            @ApiImplicitParam(name="msToken", value="FCM token String", required = true, dataType="json")
    })
     */
    @PostMapping("/ms/register")
    public String registerMsToken(@RequestBody Map<String, Object> request) {
        String holderDid = (String) request.get("holder");
        String msToken = (String) request.get("msToken");
        log.info("holderDid:" + holderDid + ", msToken:" + msToken);
        if (holderDid == null || msToken == null) {
            return makeResponse(false, "Invalid argument");
        }
        // find did
        Friend fa = friendsRepository.findFriendByDid(holderDid);
        if (fa == null) {
            log.info("Not found exist did : " + request.get("holder"));
            return makeResponse(false, "Not found did");
        }
        fa.setMsToken(msToken);
        Friend newF = friendsRepository.save(fa);
        log.info(newF.toString());
        return makeResponse(true, null);
    }

    @ApiOperation(value = "친구 리스트 조회",
            notes = "친구 리스트 조회",
            response = ResponseData.class
    )
    @PostMapping("/friends")
    public String getFriends(@RequestBody Map<String, Object> request) {
        String reqDid = (String) request.get("holder");
        log.info("getFriends() did:" + reqDid);
        // 본인의 DID를 제외한 DID리스트를 찾는다.
        List<Friend> friendList = friendsRepository.findFriendsByDidIsNot(reqDid);
        String resString = makeResponseFriends(friendList);
        log.info("friend list:" + resString);
        return resString;
    }

    @GetMapping("/friends/clean")
    public String cleanFriends(@RequestParam(value = "pass") String pass) {
        log.info("/friends/clean:" + pass);
        if(!pass.equals("0000")) {
            return makeResponse(false, "Invalid password");
        }
        List<Friend> friendList = friendsRepository.findFriendsByDidIsNot("000");

        for(Friend f : friendList) {

            log.info("/friends/clean delete did:" + f.getDid());

            String did = f.getDid();
            friendsRepository.deleteFriendByDid(did);
        }

        List<Friend> newList = friendsRepository.findFriendsByDidIsNot("000");
        if(newList == null || newList.size() < 1) {
            log.info("/friends/clean list is empty!");
        }
        else {
            for(Friend f : friendList) {
                log.info("/friends/clean clean fail:" + f.getDid());
            }
        }

        return makeResponse(true, null);
    }

    @ApiOperation(value = "친구 등록",
            notes = "DID와 이름으로 친구 리스트에 등록",
            response = ResponseData.class
    )
    @PostMapping("/friends/create")
    public String create(@RequestBody Map<String, Object> request) {
        log.info("=== CREATE ===");
        log.info("req:" + request.toString());

        // Find DID : did로 DB 쿼리.
        Friend fa = friendsRepository.findFriendByDid((String) request.get("did"));
        if (fa == null) {
            log.info("Not found exist did : " + request.get("did"));
        } else {
            log.info("Found exist did so ignore");
            return makeResponse(false, "Already Registered DID");
        }

        Friend f = new Friend((String) request.get("did"), (String) request.get("name"));
        Friend friend1 = friendsRepository.save(f);
        log.info("friend:" + friend1.toString());
        return makeResponse(true, null);
    }

    @ApiOperation(value = "FCM 푸시 테스트")
    @GetMapping("/fcm/test")
    public String testGet() {
        log.info("=== testGet ===");

        try {
            String testToken = "diN-OKL5_t8:APA91bGPreVi3qS9_w9nFlJYKSWmEZTRpUgan-voWGoOwIzRwD0MTSfFN-eR_e_WUCSel72ioId61Wt-sDgeDFexPzoAl7RxL4VHMeHybh6CoIHj31IO1JijvZoCIPkrCrHOpC_Yw_PN";
            sendFirebaseMessage(testToken, "TEST TOKEN", "12345667902092902092092902");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "testGet";
    }

    //////////////////////////
    // Firebase Messaging
    //////////////////////////

    private void sendFirebaseMessage(String token, String title, String body) throws Exception {
        String fcmKeyfileName = "ssi-fcm-firebase-adminsdk-lv82o-f95f434596.json";
        Path fcmKeyFilePath = Paths.get(File.separatorChar + fcmKeyfileName);
        log.info("FCM KEY PATH:" + fcmKeyFilePath.toString());
        InputStream stream = getClass().getResourceAsStream(fcmKeyFilePath.toString());

        String apiURL = "https://fcm.googleapis.com/v1/projects/ssi-fcm/messages:send";
        String pushMessage = makeFcmPushJsonMessage(token, title, body);
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

    /////////////////////////////
    // Response
    /////////////////////////////
    private String makeResponse(boolean status, String errorMessage) {
        ResponseData responseData = ResponseData.builder()
                .status(status)
                .errorMessage(errorMessage)
                .build();

        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }

    private String makeResponseFriends(List<Friend> list) {
        List<ResponseData.Friend> fList = new ArrayList<>();
        for (Friend friend : list) {
            ResponseData.Friend f = ResponseData.Friend.builder().did(friend.getDid()).name(friend.getName()).build();
            fList.add(f);
        }

        ResponseData responseData = ResponseData.builder()
                .status(true)
                .friends(fList)
                .build();
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(responseData);
        return json;
    }
}
