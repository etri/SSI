package com.bd.ssi.common.fcm;

import com.google.common.collect.Maps;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class FcmServiceTest {

    @Autowired
    FcmService fcmService;

    @Test
    public void test() throws FirebaseMessagingException {
        String tk = "ckipZQmfQ8Oj6dzdjoEtVB:APA91bE7fG2czoWE2IkGMBdVHAokk2FDnTIT69Be5WWx1R7QeIKDFMW8KDkUAJW85y8ZFyV-bckihKrF7DDF0qdYTZn9n0blk6i6Qb2fVNNIg0bO0qr3SBCFFaAtIn0ijReGDSUrVKIG";
        Map<String, String> maps = Maps.newHashMap();
        maps.put("title", "test_aa_111");
        maps.put("body", "test_bb_111");
        String messageId = fcmService.sendNotificationMessage(tk, "title_22", "contents_22");
//        String messageId = fcmService.sendDataMessage(tk, maps);
//        fcmService.send(maps);
    }
}