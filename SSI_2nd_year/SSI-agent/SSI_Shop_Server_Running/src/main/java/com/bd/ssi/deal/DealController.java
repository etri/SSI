
package com.bd.ssi.deal;

import com.bd.ssi.auth.User;
import com.bd.ssi.common.BuyListVO;
import com.bd.ssi.common.ProductNDeal;
import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.product.Product;
import com.bd.ssi.product.ProductService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/deal")
public class DealController {

    public static final Logger logger = LoggerFactory.getLogger(DealController.class);

    @Value("${file.path}")
    private String filePath;

    @Autowired
    DealService dealService;

    @Autowired
    ProductService productService;

    /**
     * 구매내역 저장
     */
    @RequestMapping("/addbuying")
    @ResponseBody
    public ApiResponse addBuying(@RequestBody Map<String, String> map){
        Deal deal = new Deal();
        User user = new User();
        Product product = new Product();

        user.setUsername(map.get("buyer"));
        user.setName(map.get("buyerName"));
        user.setDid(map.get("did"));

        product.setProductId( Integer.parseInt(map.get("productId")) );

        deal.setDidSeller(productService.getProductById(product.getProductId()).getDid());
        deal.setProduct(product);
        deal.setBuyer(user);
        deal.setBuyerName(map.get("buyerName"));
        deal.setPaymentMethod(map.get("paymentMethod"));
        deal.setPaymentCard(map.get("paymentCard"));
        deal.setCount(Integer.parseInt(map.get("count")));
        deal.setPricePerOne(map.get("pricePerOne"));
        deal.setTotalPrice(map.get("totalPrice"));
        deal.setAddress(map.get("address"));
        deal.setPhone(map.get("phone"));
        deal.setDid(map.get("did"));
        deal.setDidSelected(map.get("didSelected"));
        deal.setState("배송중");
        deal.setVcState("미발급");

        Deal savedDeal = dealService.addBuying(deal);

        //우체국 위임 관련 배송정보 전달
        Map<String, String> paramMap = new HashMap<String, String>();
//        if( !StringUtils.isBlank(deal.getProduct().getProductName()) ) {
//            paramMap.put("product", deal.getProduct().getProductName());
//        }else{
//            paramMap.put("product", "null");
//        }
        paramMap.put("product", map.get("productName"));
        paramMap.put("consumer", deal.getBuyerName());
        paramMap.put("birth", map.get("birth"));
        paramMap.put("address", deal.getAddress());
        doSendInfosForPostDelegate(paramMap);

        return ApiResponse.ok(savedDeal);
    }

    /**
     * 거래내역 상세 조회
     * @param deal
     * @return
     */
    @GetMapping("/detail/{dealId}")
    @ResponseBody
    public ApiResponse detail(Deal deal){
        logger.info("dealId : {}", deal.getDealId());
        Deal dealResult = dealService.getDealById(deal.getDealId());
        return ApiResponse.ok(dealResult);
    }

    /**
     * DID로 구매내역 목록 조회
     * @return
     */
    @RequestMapping("/buyList")
    @ResponseBody
    public ApiResponse buyList(@RequestBody String req){
        req = req.replaceAll("\"", "");
        List<BuyListVO> list = dealService.getBuyList(req);
        return ApiResponse.ok(list);
    }

    /**
     * dealId로 구매내역 목록 조회
     * @return
     */
    @RequestMapping("/buyHistByDealId")
    @ResponseBody
    public ApiResponse getBuyHistByDealId(@RequestBody String req){
        req = req.replaceAll("\"", "");
        BuyListVO list = dealService.getBuyHistByDealId(req);
        return ApiResponse.ok(list);
    }

    /**
     * 구매내역 목록 조회 - 구매확정만
     * @return
     */
    @RequestMapping("/buyListBuyOk")
    @ResponseBody
    public ApiResponse buyListBuyOk(@RequestBody String req){
        req = req.replaceAll("\"", "");
        List<BuyListVO> list = dealService.getBuyListBuyOk(req);
        return ApiResponse.ok(list);
    }

    /**
     * 판매내역 목록 조회
     * @return
     */
    @RequestMapping("/sellList")
    @ResponseBody
    public ApiResponse sellList(@RequestBody String req){
//        String username = SecurityHolder.getUsername();
        req = req.replaceAll("\"", "");
//        List<Deal> list = dealService.getSellList(req);
        List<ProductNDeal> list = dealService.getSellList(req);
        return ApiResponse.ok(list);
    }

    /**
     * dealId 거래 상세 조회
     * @return
     */
    @RequestMapping("/sellListByDealId")
    @ResponseBody
    public ApiResponse sellListByDealId(@RequestBody int req){
//        String username = SecurityHolder.getUsername();
        Deal deal = dealService.getSellListByDealId(req);
        return ApiResponse.ok(deal);
    }

    /**
     * 수취확인
     * @param deal
     * @return
     */
    @PostMapping("/receive")
    @ResponseBody
    public ApiResponse receive(@RequestBody Deal deal){
        logger.info("dealCtrl.receive dealId : {}", deal.getDealId());
        return ApiResponse.ok(dealService.receive(deal.getDealId()));
    }

    /**
     * 보증서 발급 요청
     */
    @PostMapping("/warranty")
    @ResponseBody
    public ApiResponse requestWarranty(@RequestBody Deal request) throws Exception {
        logger.info("dealId : {}", request.getDealId());
        return ApiResponse.ok(dealService.requestWarranty(request));
    }

    /**
     * 구매확정
     * @param deal
     * @return
     */
    @PostMapping("/confirm")
    @ResponseBody
    public ApiResponse confirm(@RequestBody Deal deal){
        logger.info("dealId : {}", deal.getDealId());
        return ApiResponse.ok(dealService.confirm(deal.getDealId()));
    }

    /**
     * 배송 처리
     * @param deal
     * @return
     */
    @PostMapping("/send")
    @ResponseBody
    public ApiResponse send(@RequestBody Deal deal){
        logger.info("dealId : {}", deal.getDealId());
        return ApiResponse.ok(dealService.send(deal.getDealId()));
    }

    /**
     * 보증서 발급 처리. 판매자가 물품보증서발급 버튼 클릭 시 실행. 거래내역에서 저장된 did, 공개키, signature를 조회
     * @param deal
     * @return
     */
    @PostMapping("/issue")
    @ResponseBody
    public ApiResponse issue(@RequestBody Deal deal) throws Exception{
        return ApiResponse.ok(dealService.issue(deal));
    }

    /**
     * 중고품 물품 보증서 발급 후 ssi://vclist로 넘기기 전 생성된 거래증명VC, 물품정보VC 저장.
     */
    @PostMapping("/saveUsedVcs")
    @ResponseBody
    public ApiResponse saveUsedVcs(@RequestBody Map<String, String> map) throws Exception{
        return ApiResponse.ok(dealService.saveUsedVcs(map));
    }

    /**
     * 새상품 물품보증서요청 -> 구매확정으로 상태 변경
     */
    @PostMapping("/updateVcStateToEnd")
    @ResponseBody
    public ApiResponse updateVcStateToEnd(@RequestBody Integer dealId) throws Exception{
        return ApiResponse.ok(dealService.updateVcStateToEnd(dealId));
    }

    /**
     * 발급완료 -> 물품보증서 발급 확인으로 상태 변경
     */
    @PostMapping("/updateVcStateToCheckCertification")
    @ResponseBody
    public ApiResponse updateVcStateToCheckCertification(@RequestBody Integer dealId) throws Exception{
        return ApiResponse.ok(dealService.updateVcStateToCheckCertification(dealId));
    }

    /**
     * 발급완료 -> 구매확정으로 상태 변경
     */
    @PostMapping("/updateVcStateToComplete")
    @ResponseBody
    public ApiResponse updateVcStateToComplete(@RequestBody Integer dealId) throws Exception{
        return ApiResponse.ok(dealService.updateVcStateToComplete(dealId));
    }

    /**
     * 우체국 신분증VC 위임 관련
     * 우체국 서버로 상품정보, 배송정보 전달
     */
    public void doSendInfosForPostDelegate(Map<String, String> paramMap){
        Gson gson = new Gson();

        String postMsgJson = gson.toJson(paramMap);
        String post_server = "http://129.254.194.112:9008/api/item/add";

        String rcvPostMsg = sendRestMngr(post_server, postMsgJson);
    }

    public String sendRestMngr(String serverUrl, String jsonStr){
        String inputLine = null;
        StringBuilder outResult = new StringBuilder();

        try{
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            OutputStream os = conn.getOutputStream();
            os.write(jsonStr.getBytes());
            os.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while((inputLine = in.readLine()) != null){
                outResult.append(inputLine);
            }

            conn.disconnect();;
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return outResult.toString();
    }
}
