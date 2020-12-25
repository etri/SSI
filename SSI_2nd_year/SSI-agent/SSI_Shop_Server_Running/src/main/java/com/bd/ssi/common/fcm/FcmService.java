package com.bd.ssi.common.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * FCM 서비스
 */
@Service
public class FcmService {

    @Value("${firebase.key.path}")
    private String firebaseKeyPath;

    @Value("${firebase.db.url}")
    private String firebaseDBUrl;

    private String fcm_authorization_key = "AAAAV4a7biw:APA91bF_xxaUepyviai8E7SAjOagNOfUGjAY1LUSDRWo2ZjBrG5MLAyPJNUav5lgdp-DlVPsRJ-Hu5fi8idb1H6cDN_EenUITIK4FHkg-Bl7TaK-JOykwZkCSyei1KLYpRy9cofqho3V";

    /**
     * Firebase 셋팅 초기화
     * @throws IOException
     */
    @PostConstruct
    public void init() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(firebaseKeyPath);
        if(classPathResource.exists() == false){
            System.out.println("Invalid filePath:" + firebaseKeyPath);
            throw new IllegalArgumentException();
        }

        InputStream serviceAccount = classPathResource.getInputStream();    //서버용
//        FileInputStream serviceAccount = new FileInputStream(classPathResource.getFile());    //로컬용

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDBUrl)
                .build();

        FirebaseApp.initializeApp(options);
    }

    /**
     * Noti 메시지 발송
     * @param token
     * @param title
     * @param contents
     * @return
     * @throws FirebaseMessagingException
     */
    public String sendNotificationMessage(String token, String title, String contents) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(contents).build())
                .setToken(token)
                .build();

        return send(message);
    }

    /**
     * 데이터 메시지 발송
     * @param token
     * @param data
     * @return
     * @throws FirebaseMessagingException
     */
    public String sendDataMessage(String token, Map<String, String> data) throws FirebaseMessagingException {
        Message message = Message.builder()
                .putAllData(data)
                .setToken(token)
                .build();

        return send(message);
    }

    /**
     * FCM 메시지 발송
     */
    public String send(Message message) throws FirebaseMessagingException {
        return FirebaseMessaging.getInstance().send(message);

//        String regiToken = serverKeyToken;
//        Message msg = Message.builder().putData("title", "test111").putData("body", "test222").setToken(regiToken).build();
//        String res = FirebaseMessaging.getInstance().send(msg);
//        System.out.println("### result of sent message:" + res);
//
//        return "";
    }

    public void fcmSendMsgByRest(){
        String fcm_url = "https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send";
        String msg ="{" +
                        "\"message\":{" +
                            "\"token\":\"fqVq0o6ZQDuhfr-SJJ0KbQ:APA91bEmXHlPtiYDViO4mUazJ8t2rWbjAuRCQGC-B8FJP7ufM1GqwSnwteGOofGJwyNMSxjo91Cxyw7tSkPmlhvcE1xzRN3f-Xnb21eg32UG2l5zebhDfgfng_3awyn2uT5Nc7PkGqfg\", " +
                            "\"notification\":{" +
                                "\"body\":\"zz-test-body\", " +
                                "\"title\":\"zz-test-title\"" +
                            "}" +
                        "}" +
                    "}";
        try {
            URL url = new URL(fcm_url);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", fcm_authorization_key);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
            osw.write(msg);
            osw.flush();
            osw.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            if (conn.getResponseCode() != 200) {
//                System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
                throw new RuntimeException("Failed: HTTP error code : " + conn.getResponseCode());
            } else {
                System.out.println("발송 성공");
            }

            String line = null;
            while((line = br.readLine()) != null){
                System.out.println(line);
            }
            br.close();
            conn.disconnect();
        } catch (IOException e) {
            System.out.println("RestCall Fail : " + e.getMessage());
        }
    }
}
