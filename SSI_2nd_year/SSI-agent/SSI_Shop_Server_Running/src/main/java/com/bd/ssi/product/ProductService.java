package com.bd.ssi.product;

import com.bd.ssi.auth.UserRepository;
import com.bd.ssi.common.VcVpMngr;
import com.bd.ssi.common.mapper.MarketDBMappers;
import com.bd.ssi.common.metadium.MetadiumService;
import com.bd.ssi.common.security.SecurityHolder;
import com.bd.ssi.deal.Deal;
import com.bd.ssi.deal.DealRepository;
import com.google.common.collect.Lists;
import com.iitp.verifiable.VerifiableCredential;
import com.iitp.verifiable.VerifiablePresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProductService {

    public static final String CARD_VC_NAME = "CardTokenCredentiall";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${file.path}")
    private String filePath;

    @Autowired
    private MetadiumService metadiumService;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private VcVpMngr vcVpMngr;

    @Autowired
    private MarketDBMappers marketDBMappers;

    /**
     * 상품 목록 조회
     * @return
     */
    public List<Product> getProducts() {
        List<Product> list = productRepository.findAll(Sort.by(Sort.Direction.DESC, "productId"));
        for (Product product : list) {
            product.setImages(productImageRepository.findAllByProductId(product.getProductId()));
        }
        return list;
    }

    /**
     * 상품 상세 조회
     * @param productId
     * @return
     */
    public Product getProductById(Integer productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if(product != null)
            product.setImages(productImageRepository.findAllByProductId(product.getProductId()));
        return product;
    }

    /**
     * 상품 구매 처리
     * @param request
     * @return
     */
    @Transactional
    public Deal purchaseProduct(PurchaseRequest request) throws Exception {

        VerifiablePresentation resultVp = metadiumService.verifyVp(request.getVp());

        String cardVc = null;

        // VP 내 VC 검증
        for (Object vcObject : resultVp.getVerifiableCredentials()) {
            if (vcObject instanceof String) {
                VerifiableCredential resultVc = metadiumService.verifyVc((String)vcObject);
                String type = metadiumService.getType(resultVc);

                //VC가 카드 VC이면 변수 할당
                if (CARD_VC_NAME.equals(type))
                    cardVc = (String)vcObject;

                Map<String, Object> claims = (Map<String, Object>)resultVc.getCredentialSubject();
//                System.out.println(claims.toString());
            }
        }

        // create VP for PG
        VerifiablePresentation vp = new VerifiablePresentation();
        vp.setTypes(Arrays.asList("PRESENTATION", "PG"));
        vp.addVerifiableCredential(cardVc);
        String pgSignedVp = metadiumService.signVp(vp);

        //TODO send VP to PG
        
        
        //결제 완료 후 결제 내역 생성
        Deal deal = new Deal();
        deal.setBuyer(userRepository.findById(SecurityHolder.getUsername()).get());
        deal.setPaymentMethod(request.getPaymentMethod());
        deal.setRequest(request.getRequest());
        deal.setProduct(getProductById(request.getProductId()));
        deal.setCount(request.getCount());
        deal.setPhone(request.getPhone());
        deal.setAddress(request.getAddress());
        deal.setState("결제완료");
        deal.setVcState("미발급");

        return dealRepository.save(deal);
    }

    @Transactional
    public void setPurchaseState(Integer dealId){
        dealRepository.findById(dealId).ifPresent(it -> {
            it.setState("결제완료");
            dealRepository.save(it);
        });
    }

    /**
     * 중고품 등록 전 VC생성, VP요청
     */
    public String addUsed(Product product) throws Exception {
        String signedProductVc = "";
        String signedDealVc = "";

        // 만료일 캘린더 설정
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 2);

        //TODO 물품정보 VC 발급
        VerifiableCredential productVc = new VerifiableCredential();
        productVc.setTypes(Arrays.asList("CREDENTIAL", "ProductCredential"));
        productVc.setExpirationDate(calendar.getTime());
        productVc.setIssuanceDate(new Date());
        Map<String, Object> productSubject = new HashMap<>();
        productSubject.put("id", product.getDid());	// 물품 DID
        productSubject.put("name", product.getProductName()); // 제품명
        productSubject.put("SN", product.getProductId()); // 시리얼 넘버
        productSubject.put("production_date", product.getCreateDate()); // 제조일
        productVc.setCredentialSubject(productSubject);
        signedProductVc = metadiumService.signVc(productVc);

        //TODO 거래증명 VC 발급
        VerifiableCredential dealVc = new VerifiableCredential();
        dealVc.setTypes(Arrays.asList("CREDENTIAL", "ProductProofCredential"));
        dealVc.setExpirationDate(calendar.getTime());
        dealVc.setIssuanceDate(new Date());
        Map<String, Object> dealSubject = new HashMap<>();
        dealSubject.put("id", product.getDid());	// 판매자 DID
        dealSubject.put("DID_buyer", product.getDid()); // 구매자DID
        dealSubject.put("product_DID", ""); // 물품정보VC DID
        dealSubject.put("Trade_DID", ""); // 거래 당시 DID
        dealSubject.put("DID_doc_ver_time", ""); // DID Document version time
        dealSubject.put("price", product.getPrice()); // DID Document version time
        dealVc.setCredentialSubject(dealSubject);
        signedDealVc = metadiumService.signVc(dealVc);

        return "";
    }

    /**
     * 상품 등록
     * @param request
     * @param files
     * @return
     */
    @Transactional
    public Product add(ProductRequest request, List<MultipartFile> files) {
        Product p = new Product();
        p.setProductName(request.getProductName());
        p.setDescription(request.getDescription());
        p.setPrice(request.getPrice());
        p.setType(request.getType());
        p.setAddress(request.getAddress());
        p.setDid(request.getDid());
        p.setDidSelected(request.getDidSelected());
        p.setCreateUser(SecurityHolder.getUsername());

        //물품DID
        String[] productThings = null;
        String productDid = "";
        String productWallet = "";
        //제조사DID
        String[] manufactureThings = null;
        String manufacturerDid = "";
        String manufacturerWallet = "";
        if( "새제품".equals(p.getType()) ) {
            /**
             * 새제품을 등록하는 경우 물품DID, 제조사DID를 새로 생성
             */
            //물품DID 생성.
            productThings = vcVpMngr.createDid();
            productDid = productThings[0];
            productWallet = productThings[1];
            //제조사DID 생성.
            manufactureThings = vcVpMngr.createDid();
            manufacturerDid = manufactureThings[0];
            manufacturerWallet = manufactureThings[1];
        }else{
            /**
             * (2020.10.22 기준) 중고품을 등록하는 경우 물품DID를 새로 생성하는 것이 아닌 기존 물품DID를 가져와야 함.
             *      중고품의 경우 물품정보VC, 거래증명VC 생성을 SSI APP에서 하므로 Wallet은 관리할 필요가 없음.
             */
            int usedProductId = request.getUsedProductId();
            Product productInfos = getProductById(usedProductId);
            productDid = productInfos.getProductDid();
            manufacturerDid = productInfos.getManufacturerDid();
        }

        //제조사VC Attribute.
        String manufacturer = "ETRI";
        UUID uuid = UUID.randomUUID();
        String serialNum = uuid.toString();                 //UUID 발생
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss");
        String madeDate = sdf.format(new Date());           //제조일시. Hard Coding

        p.setProductDid(productDid);
        p.setProductWallet(productWallet);
        p.setManufacturerDid(manufacturerDid);
        p.setManufacturer(manufacturer);
        p.setManufacturerWallet(manufacturerWallet);
        p.setSerialNum(serialNum);
        p.setMadeDate(madeDate);

        p = productRepository.save(p);

        p.setImages(Lists.newArrayList());

        if( files != null && files.size() > 0){
            for (MultipartFile file : files) {
                if(!file.isEmpty()){
                    String filename = StringUtils.cleanPath(file.getOriginalFilename());

                    try(InputStream inputStream = file.getInputStream()) {
                        Path target = Paths.get(filePath, filename);
                        logger.info("store file to {}", target.toString());
                        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);

                        ProductImage image = new ProductImage();
                        image.setProductId(p.getProductId());
                        image.setImg(filename);
                        productImageRepository.save(image);
                        p.getImages().add(image);
                    } catch (IOException e) {
                        logger.error("failed to store file " + filename, e);
                    }
                }
            }
        }

        if(files == null || files.size() < 1){
            ProductImage image = new ProductImage();
            image.setProductId(p.getProductId());
            image.setImg(request.getImg());
//            productImageRepository.save(image);
            marketDBMappers.doSaveProductImg(p.getProductId().toString(), request.getImg());
            p.getImages().add(image);
        }

        return p;
    }
}
