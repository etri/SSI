package com.bd.ssi.deal;

import com.bd.ssi.auth.UserRepository;
import com.bd.ssi.common.BuyListVO;
import com.bd.ssi.common.IssuerInfo;
import com.bd.ssi.common.ProductNDeal;
import com.bd.ssi.common.VcVpMngr;
import com.bd.ssi.common.fcm.FcmService;
import com.bd.ssi.common.fcm.PushMngrVO;
import com.bd.ssi.common.mapper.MarketDBMappers;
import com.bd.ssi.common.metadium.MetadiumService;
import com.bd.ssi.product.Product;
import com.bd.ssi.product.ProductImageRepository;
import com.google.common.collect.Maps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiableVerifier;
import com.iitp.verifiable.signer.MetadiumSigner;
import com.iitp.verifiable.util.ECKeyUtils;
import com.metadium.did.MetadiumWallet;
import com.metadium.did.crypto.MetadiumKey;
import com.metadium.did.exception.DidException;
import com.metadium.did.protocol.MetaDelegator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DealService {

    @Autowired
    DealRepository dealRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Autowired
    FcmService fcmService;

    @Autowired
    MetadiumService metadiumService;

    @Autowired
    MarketDBMappers marketDBMappers;

    private Logger logger = LoggerFactory.getLogger(DealService.class);


    /**
     * 구매내역 저장
     */
    @Transactional
    public Deal addBuying(Deal deal){
        Deal savedDeal = dealRepository.save(deal);
        return savedDeal;
    }

    /**
     * 거래내역 상세 조회
     */
    public Deal getDealById(Integer dealId){
        return dealRepository.findById(dealId).orElse(null);
    }


    /**
     * DID로 구매내역 목록 조회
     */
    public List<BuyListVO> getBuyList(String did) {
//        List<Deal> deals = dealRepository.findByBuyer(userRepository.findById(username).get());
//        List<Deal> deals = dealRepository.findByDid(did);

//        for (Deal deal : deals) {
//            setDealImage(deal);
//        }
//        return deals;

        return marketDBMappers.getBuyList(did);
    }

    /**
     * dealId 로 구매내역 목록 조회
     */
    public BuyListVO getBuyHistByDealId(String dealId) {
        return marketDBMappers.getBuyHistByDealId(dealId);
    }


    /**
     * 구매내역 목록 조회 - 구매확정만
     * @param did
     * @return
     */
    public List<BuyListVO> getBuyListBuyOk(String did) {
//        List<Deal> deals = dealRepository.findByBuyer(userRepository.findById(username).get());
//        List<Deal> deals = dealRepository.findByDid(did);

//        for (Deal deal : deals) {
//            setDealImage(deal);
//        }
//        return deals;

        return marketDBMappers.getBuyListBuyOk(did);
    }


    /**
     * 판매내역 목록 조회
     * @param did
     * @return
     */
    public List<ProductNDeal> getSellList(String did) {
//        List<Deal> deals = dealRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
//                criteriaBuilder.equal(root.join("product").get("did"), did)
//        ));

//        for (Deal deal : deals) {
//            setDealImage(deal);
//        }
        List<ProductNDeal> productNDeal = marketDBMappers.getProductNDeal(did);

        return productNDeal;
    }

    /**
     * dealId 거래 상세 조회
     */
    public Deal getSellListByDealId(int dealId) {
        Deal deal = dealRepository.findById(dealId).orElse(null);

        return deal;
    }

    /**
     * 수취 확인 처리
     * @param dealId
     * @return
     */
    @Transactional
    public Object receive(Integer dealId) {
        Map<String, String> paramMap = new HashMap<String, String>();

        Deal d;
        Optional<Deal> deal = dealRepository.findById(dealId);

        if(deal.isPresent()){
            d = deal.get();
            d.setState("배송완료");
            setDealImage(d);

            //FOR push alarm.
            paramMap.put("did", d.getDidSeller());
            paramMap.put("productId", d.getProduct().getProductId().toString());
            PushMngrVO pushMngrVO = marketDBMappers.getPushToken(paramMap.get("did"));
            if( pushMngrVO != null && !StringUtils.isBlank(pushMngrVO.getToken())){
                Product product = marketDBMappers.getProductById(paramMap.get("productId"));
                if(product != null){
                    Map<String, String> msgMap = Maps.newHashMap();
                    msgMap.put("title", "상품수취 알림");
                    msgMap.put("body", product.getProductName() + " 상품수취 완료");
                    msgMap.put("link", "https://ssishop.page.link?step=get&dealId=" + d.getDealId());

                    try {
                        String messageId = fcmService.sendDataMessage(pushMngrVO.getToken(), msgMap);
                    }catch(FirebaseMessagingException ex){
                        ex.printStackTrace();
                    }
                }
            }

            return dealRepository.save(d);
        }

        return null;
    }

    /**
     * 구매 확정 처리
     * @param dealId
     * @return
     */
    @Transactional
    public Object confirm(Integer dealId) {
        Optional<Deal> deal = dealRepository.findById(dealId);
        if(deal.isPresent()){
            Deal d = deal.get();
            d.setState("구매확정");
            setDealImage(d);
            return dealRepository.save(d);
        }
        return null;
    }

    /**
     * 배송 처리
     * @param dealId
     * @return
     */
    @Transactional
    public Object send(Integer dealId) {
        Optional<Deal> deal = dealRepository.findById(dealId);
        if(deal.isPresent()){
            Deal d = deal.get();
            d.setState("배송중");
            setDealImage(d);
            return dealRepository.save(d);
        }
        return null;
    }

    /**
     * 보증서 발급 요청 처리
     * @param request
     * @return
     */
    @Transactional
    public Object requestWarranty(Deal request){
        Optional<Deal> deal = dealRepository.findById(request.getDealId());
        String returnVal = "";
        String signedDealVc = "";
        String signedProductVc = "";

        if(deal.isPresent()){
            Deal d = deal.get();
            if(d.getProduct().getType().equals("새제품")) {
                // 만료일 캘린더 설정
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.YEAR, 2);

                //TODO 물품정보 VC 발급
                VerifiableCredential productVc = new VerifiableCredential();
                //초기 생성시에는 사용하지 않음으로 주석처리
//                BigInteger blockNumber = new BigInteger(((Map<String, String>)productVc.getCredentialSubject()).get("BlockNumber"));
                productVc.setTypes(Arrays.asList("CREDENTIAL", "ProductCredential"));
                productVc.setExpirationDate(calendar.getTime());
                productVc.setIssuanceDate(new Date());
                Map<String, Object> productSubject = new HashMap<>();
                productSubject.put("id", d.getProduct().getProductDid());	            // 물품 DID
                productSubject.put("name", d.getProduct().getProductName());            // 제품명
                productSubject.put("SN", UUID.randomUUID().toString());                // 시리얼 넘버
                productSubject.put("production_date", d.getProduct().getMadeDate());    // 제조일
                productVc.setCredentialSubject(productSubject);
                try {
                    signedProductVc = metadiumService.signVc(productVc);
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                //TODO 거래증명 VC 발급
                Date tempDate = null;
                try {
                    tempDate = new SimpleDateFormat("yyyy-MM-dd").parse(d.getDealDate().toString());
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                Map<String, String> claims = Stream.of(new String[][] {
                        { "ProductCredential_id", d.getProduct().getProductDid()},          //물품 DID
                        { "seller_id", IssuerInfo.ISSUER_DID},                              //판매자 DID : 새상품이므로 쇼핑몰 DID
                        { "buyer_id", request.getDidBuyer() },                              //구매자 DID("DID선택" 선택한 DID)
                        { "user_id", request.getDid() },                                    //로그인한 사용자 DID
                        { "BlockNumber", "" },                                              //초기 생성시에는 사용하지 않으므로 값이 없는게 맞음.
                        { "price", d.getPricePerOne() },                                    //개당 물품 가격
                        { "sell_date", new SimpleDateFormat("yyyy-MM-dd").format(tempDate) } //팔린 날짜
                }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

                VerifiableCredential dealVC = new VerifiableCredential();
                dealVC.setIssuer( URI.create(d.getProduct().getDid()) );                //판매자 DID
                dealVC.addTypes(Collections.singletonList("ProductProofCredential"));
                dealVC.setIssuanceDate(new Date());
                dealVC.setId(URI.create(UUID.randomUUID().toString()));
                dealVC.setCredentialSubject(claims);

                //판매자가 서명
                MetadiumSigner signer = new MetadiumSigner(IssuerInfo.ISSUER_DID, IssuerInfo.ISSUER_KID, ECKeyUtils.toECPrivateKey(new BigInteger(IssuerInfo.ISSUER_PRIVATEKEY, 16), "secp256k1"));
                try {
                    signedDealVc = signer.sign(dealVC);
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                VcVpMngr vcVpMngr = new VcVpMngr();
                returnVal = vcVpMngr.getPvcDvc(signedProductVc, signedDealVc);

                d.setDidBuyer(request.getDidBuyer());
                d.setEtc(returnVal);

                //TODO 물품정보 VC DID rotate
                try{
                    //여기 updateKeyOfDid 들어가는 did는 구매자 DID
                    BigInteger newKey = updateKeyOfDid(d.getProduct().getProductWallet(), request.getDid(), request.getPublicKey(), request.getSign());
                    d.setPublicKey(newKey.toString());
                    d.setVcState("발급완료");
                }catch(Exception ex){
//                    d.setVcState("미발급");
//                    ex.printStackTrace();
                }
            } else {
                // 중고품
                d.setDidBuyer(request.getDidBuyer());
                d.setPublicKey(request.getPublicKey());
                d.setSign(request.getSign());
                d.setVcState("발급요청");
            }
            setDealImage(d);

            Map<String, Object> rMap = new HashMap<String, Object>();
            rMap.put("deal", d);
            rMap.put("pvc", signedProductVc);
            rMap.put("dvc", signedDealVc);

            return dealRepository.save(d);
//            return rMap;
//            return returnVal;
        }
        return null;
    }

    /**
     * 중고품 보증서 발급 처리(판매자가 물품보증서 발급 버튼 클릭)
     * 판매자가 물품보증서발급 버튼 클릭 시 실행. 거래내역에서 저장된 did, 공개키, signature를 조회
     */
    @Transactional
    public Object issue(Deal request) throws Exception{
        Optional<Deal> deal = dealRepository.findById(request.getDealId());
        String signedDealVc = "";
        String signedProductVc = "";

        if(deal.isPresent()){
            Deal d = deal.get();

            //end push
            Map<String, String> data = Maps.newHashMap();
            data.put("type", "물품보증서");
//            try {
//                fcmService.sendDataMessage(d.getBuyer().getToken(), data);
//                fcmService.sendNotificationMessage(d.getBuyer().getToken(), "물품보증서 발급 완료", "물품보증서 발급이 완료되었습니다.");
//            } catch (FirebaseMessagingException e) {
//                logger.error("푸시 발송 에러", e);
//            }

            return d;
        }
        return null;
    }

    /**
     *
     */
    public String saveUsedVcs(Map<String, String> map) throws Exception{
        Deal tmpDeal = new Deal();
        tmpDeal.setDealId( Integer.parseInt(map.get("dealId")));
        Optional<Deal> deal = dealRepository.findById(tmpDeal.getDealId());

        if(deal.isPresent()) {
            Deal d = deal.get();
            d.setEtc(map.get("vc"));
            d.setVcState(map.get("vcState"));

            dealRepository.save(d);

            PushMngrVO pushMngrVO = marketDBMappers.getPushTokenByDealId(map.get("dealId"));
            if( pushMngrVO != null && !StringUtils.isBlank(pushMngrVO.getBuyerToken())){
//                Map<String, String> msgMap = Maps.newHashMap();
//                msgMap.put("title", "물품보증서 전달");
//                msgMap.put("body", pushMngrVO.getProductName() + " 상품 물품보증서 전달");
//
//                String messageId = fcmService.sendDataMessage(pushMngrVO.getBuyerToken(), msgMap);

                Message msg = Message.builder()
                        .putData("title", "물품보증서 발급")
                        .putData("body", pushMngrVO.getProductName() + " 상품 물품보증서 발급.")
                        .putData("link", "https://ssishop.page.link?step=pub_cert&dealId=" + map.get("dealId"))
                        .setToken(pushMngrVO.getBuyerToken())
                        .build();
                String messageId = FirebaseMessaging.getInstance().send(msg);
            }
        }

        return "true";
    }

    /**
     * 새상품 물품보증서요청 -> 발급완료으로 상태 변경
     */
    public String updateVcStateToEnd(int dealId) throws Exception{
        Deal tmpDeal = new Deal();
        tmpDeal.setDealId(dealId);
        Optional<Deal> deal = dealRepository.findById(tmpDeal.getDealId());

        if(deal.isPresent()) {
            Deal d = deal.get();
            d.setVcState("발급완료");

            dealRepository.save(d);
        }

        return "true";
    }

    /**
     * 발급완료 -> 구매확정으로 상태 변경
     */
    public String updateVcStateToCheckCertification(int dealId) throws Exception{
        Deal tmpDeal = new Deal();
        tmpDeal.setDealId(dealId);
        Optional<Deal> deal = dealRepository.findById(tmpDeal.getDealId());

        if(deal.isPresent()) {
            Deal d = deal.get();
            d.setVcState("보증서확인");

            dealRepository.save(d);
        }

        return "true";
    }

    /**
     * 발급완료 -> 구매확정으로 상태 변경
     */
    public String updateVcStateToComplete(int dealId) throws Exception{
        Deal tmpDeal = new Deal();
        tmpDeal.setDealId(dealId);
        Optional<Deal> deal = dealRepository.findById(tmpDeal.getDealId());

        if(deal.isPresent()) {
            Deal d = deal.get();
            d.setVcState("구매확정");

            dealRepository.save(d);

            PushMngrVO pushMngrVO = marketDBMappers.getPushTokenByDealId(String.valueOf(dealId));
            if( pushMngrVO != null && !StringUtils.isBlank(pushMngrVO.getSellerToken())){
                Map<String, String> msgMap = Maps.newHashMap();
                msgMap.put("title", "상품 구매 확정");
                msgMap.put("body", pushMngrVO.getProductName() + " 상품 구매 확정");
                msgMap.put("link", "https://ssishop.page.link?step=buy_end&dealId=" + dealId);

                String messageId = fcmService.sendDataMessage(pushMngrVO.getSellerToken(), msgMap);
            }
        }

        return "true";
    }

    /**
     * 구매내역에 해당하는 상품 이미지 조회
     * @param d
     */
    public void setDealImage(Deal d){
        d.getProduct().setImages(productImageRepository.findAllByProductId(d.getProduct().getProductId()));
    }

    public BigInteger updateKeyOfDid(String productWallet, String buyerDid, String buyersPublicKey, String buyerSignature) throws DidException, InvalidAlgorithmParameterException, ParseException {
        // Set verifier
//        VerifiableVerifier.register("did:meta:", MetadiumVerifier.class);	// META
//        VerifiableVerifier.register("did:icon:", IconVerifier.class);		// ICON
        VerifiableVerifier.setResolverUrl("http://129.254.194.103:9000"); // UNIVERSIAL : http://129.254.194.103:9000, META : http://129.254.194.113

        MetaDelegator delegator = new MetaDelegator("https://testdelegator.metadium.com", "https://testdelegator.metadium.com");

        // Metadium DID 생성
//        MetadiumWallet wallet = MetadiumWallet.createDid(delegator);
        MetadiumWallet wallet = MetadiumWallet.fromJson(productWallet);

        // 변경할 키 생성 및 소유자에게 보낼 서명 값 생성. DID 는 알고 있어야 함
        MetadiumKey newKey = new MetadiumKey();
        String signature = delegator.signAddAssocatedKeyDelegate(buyerDid, newKey);
        BigInteger publicKey = newKey.getPublicKey();

        // 소유자에게 변경할 키의 공개키와 서명값을 전달하여 키 변경
        wallet.updateKeyOfDid(delegator, Numeric.toBigInt(buyersPublicKey), buyerSignature);

        // 소유자 키 변경이 완료되면 새로운 Wallet 생성
//        MetadiumWallet newWallet = new MetadiumWallet(did, newKey);

        //구매자의 did, newKey를 업데이트 하기 위하여 리턴
        return publicKey;
    }
}
