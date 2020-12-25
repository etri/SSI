package com.bd.ssi.common.fcm;

import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.common.mapper.MarketDBMappers;
import com.bd.ssi.product.Product;
import com.google.common.collect.Maps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class FcmController {
    @Autowired
    FcmService fcmService;

    @Autowired
    MarketDBMappers marketDBMappers;

    @RequestMapping("/fcmcall")
    @ResponseBody
    public void fcmCall() throws FirebaseMessagingException {
        String tk = "ckipZQmfQ8Oj6dzdjoEtVB:APA91bE7fG2czoWE2IkGMBdVHAokk2FDnTIT69Be5WWx1R7QeIKDFMW8KDkUAJW85y8ZFyV-bckihKrF7DDF0qdYTZn9n0blk6i6Qb2fVNNIg0bO0qr3SBCFFaAtIn0ijReGDSUrVKIG";
        Map<String, String> maps = Maps.newHashMap();
        maps.put("title", "test_aa_111");
        maps.put("body", "test_bb_111");
        String messageId = fcmService.sendNotificationMessage(tk, "title_22", "contents_22");
//        String messageId = fcmService.sendDataMessage(tk, maps);

//        fcmService.send(maps);
    }

    @RequestMapping("/fcmcallD")
    @ResponseBody
    public ApiResponse fcmCallD(@RequestBody Map<String, String> paramMap) throws FirebaseMessagingException {
        // Push 알람을 보내기 위한 정보 수집. Product, Deal, PushMngr
        PushMngrVO pushMngrVO = marketDBMappers.getPushToken(paramMap.get("did"));
        if( pushMngrVO != null && !StringUtils.isBlank(pushMngrVO.getToken())){
            Product product = marketDBMappers.getProductById(paramMap.get("productId"));
            if(product != null){
                Map<String, String> msgMap = Maps.newHashMap();
                msgMap.put("title", "결제완료 알림");
                msgMap.put("body", product.getProductName() + " 상품결제 완료");
                msgMap.put("link", "https://ssishop.page.link?step=paid&dealId="  + paramMap.get("dealId"));

//                String messageId = fcmService.sendNotificationMessage(tk, "title_22", "contents_22");
                String messageId = fcmService.sendDataMessage(pushMngrVO.getToken(), msgMap);

//        fcmService.send(maps);

            }
        }

        return ApiResponse.ok("");
    }

    /**
     * push 보내기 - dealId만 있는 경우
     */
    @RequestMapping("/fcmcallDByDealId")
    @ResponseBody
    public ApiResponse fcmcallDByDealId(@RequestBody String param) {
        if( !StringUtils.isBlank(param) ){
            param = param.replaceAll("\"", "");
        }

        // DealId만으로 Push 알람을 보내기 위한 정보 수집
        PushMngrVO pushMngrVO = marketDBMappers.getPushTokenByDealId(param);
        if( pushMngrVO != null ){
            try {
                Map<String, String> msgMap = Maps.newHashMap();
                msgMap.put("title", "물품보증서 요청");
                msgMap.put("body", pushMngrVO.getProductName() + " 상품 물품보증서 요청.");
                msgMap.put("link", "https://ssishop.page.link?step=req_cert&dealId=" + param);
                String messageId = fcmService.sendDataMessage(pushMngrVO.getSellerToken(), msgMap);

//                Message msg = Message.builder()
//                        .putData("title", "물품보증서 요청")
//                        .putData("body", pushMngrVO.getProductName() + " 상품 물품보증서 요청.")
//                        .putData("link", "https://ssishop.page.link/dealId=" + param)
//                        .setToken(pushMngrVO.getSellerToken())
//                        .build();
//                String messageId = FirebaseMessaging.getInstance().send(msg);

//                fcmService.fcmSendMsgByRest();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        return ApiResponse.ok("");
    }
}
