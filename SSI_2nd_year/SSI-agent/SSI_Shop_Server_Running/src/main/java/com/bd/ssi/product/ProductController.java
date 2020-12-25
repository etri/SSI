package com.bd.ssi.product;

import com.bd.ssi.common.Utils;
import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.common.mapper.MarketDBMappers;
import com.google.common.io.ByteStreams;
import com.iitp.verifiable.VerifiableCredential;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping("/product")
public class ProductController {

    public static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Value("${file.path}")
    private String filePath;

    @Autowired
    ProductService productService;

    @Autowired
    Utils utils;

    @Autowired
    MarketDBMappers marketDBMappers;


    /**
     * 상품 목록 조회
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ApiResponse list(@RequestBody String searchWord){
        List<Product> result = new ArrayList<Product>();
        List<Product> products = new ArrayList<Product>();

        if( !StringUtils.isBlank(searchWord) ){
            searchWord = searchWord.replaceAll("\"", "");
        }

        if(StringUtils.isBlank(searchWord)){
            products = marketDBMappers.getProductListNoSearch();
        }else {
            products = marketDBMappers.getProductList(searchWord);
        }
        for(Product one : products){
            one.setImages(marketDBMappers.getProductImages(one.getProductId()));
            result.add(one);
        }

        return ApiResponse.ok(result);
    }

    /**
     * 상품 상세 조회
     * @param product
     * @return
     */
    @GetMapping("/detail/{productId}")
    @ResponseBody
    public ApiResponse detail(Product product){
        logger.info("productId : {}", product.getProductId());
        Product productResult = productService.getProductById(product.getProductId());
        return ApiResponse.ok(productResult);
    }

    /**
     * 상품 구매 처리
     * @param purchaseRequest
     * @return
     */
    @PostMapping("/purchase")
    @ResponseBody
    public ApiResponse purchase(@RequestBody PurchaseRequest purchaseRequest) throws Exception {
        return ApiResponse.ok(productService.purchaseProduct(purchaseRequest));
    }

    /**
     * 상품 등록
     * @param request
     * @param files
     * @return
     */
    @PostMapping("/add")
    @ResponseBody
    public ApiResponse add(@RequestPart("body") ProductRequest request, @RequestPart("files") List<MultipartFile> files){
        Product p = productService.add(request, files);
        return ApiResponse.ok(p);
    }

    /**
     * 중고품 등록 전
     */
    @PostMapping("/addUsed")
    @ResponseBody
    public ApiResponse addUsed(@RequestBody Product product){
        try {
            productService.addUsed(product);
        }catch(Exception ex){
            ex.printStackTrace();
        }
//        Product p = productService.add(request, files);
        return ApiResponse.ok("");
    }

    /**
     * 상품 이미지 조회
     * @param name
     * @return
     * @throws IOException
     */
    @GetMapping(value = "/image", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public byte[] image(@RequestParam String name) throws IOException {
        InputStream is = new FileInputStream(new File(filePath, name));
        return ByteStreams.toByteArray(is);
    }

    /**
     * 상품등록.테스트 클릭시 초기에 세팅된 6개 상품 가져오기
     */
    @PostMapping("/initProductList")
    @ResponseBody
    public ApiResponse getInitProductList(){
        List<ProductForQuery> products = new ArrayList<ProductForQuery>();
        try {
            products = marketDBMappers.getInitProductList();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return ApiResponse.ok(products);
    }
}
