package com.bd.ssi.common.mapper;

import com.bd.ssi.auth.User;
import com.bd.ssi.common.BuyListVO;
import com.bd.ssi.common.ProductNDeal;
import com.bd.ssi.common.fcm.PushMngrVO;
import com.bd.ssi.product.Product;
import com.bd.ssi.product.ProductForQuery;
import com.bd.ssi.product.ProductImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface MarketDBMappers{
    @Select("SELECT * FROM User where did = #{did}")
    User getUser(@Param("did")int did);

    //상품리스트, 검색기능 포함.
    @Select("SELECT * " +
            "FROM Product " +
            "WHERE productName LIKE CONCAT('%', #{searchWord}, '%') " +
            "ORDER BY productId DESC ")
    List<Product> getProductList(@Param("searchWord")String searchWord);

    //상품리스트.
    @Select("SELECT * " +
            "FROM Product " +
            "ORDER BY productId DESC ")
    List<Product> getProductListNoSearch();

    @Select("SELECT * " +
            "FROM Product_img " +
            "WHERE productId = #{productId}")
    List<ProductImage> getProductImages(@Param("productId")int productId);

    //판매내역 조회
    @Select ("SELECT a.productId as productId, a.productName as productName, a.description as description, a.price as price, a.type as type, a.did as did, a.didSelected as didSelected, " +
                    "a.address as address, a.createDate as createDate, a.createUser as createUser, a.productDid as productDid, a.manufacturerDid as manufacturerDid, " +
                    "a.manufacturer as manufacturer, a.madeDate as madeDate, a.serialNum as serialNum, a.productWallet as productWallet, a.manufacturerWallet as manufacturerWallet, " +
                    "b.dealId as dealId, b.productId as productIdOnDeal, b.buyer as buyer, b.buyerName as buyerName, b.request as request, b.paymentMethod as paymentMethod, " +
                    "b.state as state, b.dealDate as dealDate, b.count as count, b.pricePerOne as pricePerOne, b.totalPrice as totalPrice, b.address as addressOfDeal, " +
                    "b.phone as phone, b.publicKey as publicKey, b.did as didOfDeal, b.didSeller as didSeller, b.didSelected as didSelectedOfDeal, b.didBuyer as didBuyerOfDeal, " +
                    "b.vcState as vcState, b.buyNSell as buyNSell, b.etc as etc, b.sign as sign, " +
                    "c.id as id, c.productId as productIdOfImg, c.img as img " +
            "FROM Product AS a " +
            "LEFT OUTER JOIN Deal AS b ON a.did = b.didSeller AND a.productId = b.productId " +
            "LEFT OUTER JOIN Product_img AS c ON a.productId = c.productId " +
            "WHERE a.did = #{did} " +
            "ORDER BY a.productId DESC, b.dealId DESC")
    List<ProductNDeal> getProductNDeal(@Param("did")String did);

    //DID로 구매내역 조회
    @Select("SELECT b.productId as productId, b.productName as productName, b.description as description, b.price as price, b.type as type, b.did as did, b.didSelected as didSelected, " +
                   "b.address as address, b.createDate as createDate, b.createUser as createUser, b.productDid as productDid, b.manufacturerDid as manufacturerDid," +
                   "b.manufacturer as manufacturer, b.madeDate as madeDate, b.serialNum as serialNum, b.productWallet as productWallet, b.manufacturerWallet as manufacturerWallet," +
                   "a.dealId as dealId, a.productId as productIdOnDeal, a.buyer as buyer, a.buyerName as buyerName, a.request as request, a.paymentMethod as paymentMethod," +
                   "a.state as state, a.dealDate as dealDate, a.count as count, a.pricePerOne as pricePerOne, a.totalPrice as totalPrice, a.address as addressOfDeal," +
                   "a.phone as phone, a.publicKey as publicKey, a.did as didOfDeal, a.didSeller as didSeller, a.didSelected as didSelectedOfDeal, a.vcState as vcState, a.buyNSell as buyNSell, a.etc as etc," +
                   "a.sign as sign," +
                   "c.id as id, c.productId as productIdOfImg, c.img as img " +
            "FROM Deal AS a " +
                "INNER JOIN Product AS b " +
                    "ON a.productId = b.productId " +
                "LEFT OUTER JOIN Product_img AS c " +
                    "ON b.productId = c.productId " +
            "WHERE a.did=#{did} " +
            "ORDER BY a.dealId DESC")
    List<BuyListVO> getBuyList(@Param("did")String did);


    //dealId로 구매내역 조회
    @Select("SELECT b.productId AS productId, b.productName AS productName, b.description AS description, b.price AS price, b.type AS type, b.did AS did, b.didSelected as didSelected, " +
                   "b.address AS address, b.createDate AS createDate, b.createUser AS createUser, b.productDid AS productDid, b.manufacturerDid AS manufacturerDid, " +
                   "b.manufacturer AS manufacturer, b.madeDate AS madeDate, b.serialNum AS serialNum, b.productWallet AS productWallet, b.manufacturerWallet AS manufacturerWallet, " +
                   "a.dealId AS dealId, a.productId AS productIdOnDeal, a.buyer AS buyer, a.buyerName AS buyerName, a.request AS request, a.paymentMethod AS paymentMethod, " +
                   "a.state AS state, a.dealDate AS dealDate, a.count AS count, a.pricePerOne AS pricePerOne, a.totalPrice AS totalPrice, a.address AS addressOfDeal, " +
                   "a.phone AS phone, a.publicKey AS publicKey, a.did AS didOfDeal, a.didSeller AS didSeller, a.didSelected AS didSelectedOfDeal, a.vcState AS vcState, a.buyNSell AS buyNSell, a.etc AS etc, " +
                   "a.sign AS sign, " +
                   "c.id AS id, c.productId AS productIdOfImg, c.img AS img, " +
                   "d.did AS didPush, d.token AS token, d.create_date AS create_date, d.update_date AS update_date " +
            "FROM Deal AS a " +
                "INNER JOIN Product AS b " +
                    "ON a.productId = b.productId " +
                "LEFT OUTER JOIN Product_img AS c " +
                    "ON b.productId = c.productId " +
                "LEFT OUTER JOIN PushMngr AS d " +
                    "ON a.didSeller = d.did " +
            "WHERE a.dealId=#{dealId} " +
            "ORDER BY a.dealId, d.create_date DESC " +
            "LIMIT 1 ")
    BuyListVO getBuyHistByDealId(@Param("dealId")String dealId);


    //"구매확정"만 구매내역 조회
    @Select("SELECT b.productId as productId, b.productName as productName, b.description as description, b.price as price, b.type as type, b.did as did, b.didSelected as didSelected, " +
            "b.address as address, b.createDate as createDate, b.createUser as createUser, b.productDid as productDid, b.manufacturerDid as manufacturerDid," +
            "b.manufacturer as manufacturer, b.madeDate as madeDate, b.serialNum as serialNum, b.productWallet as productWallet, b.manufacturerWallet as manufacturerWallet," +
            "a.dealId as dealId, a.productId as productIdOnDeal, a.buyer as buyer, a.buyerName as buyerName, a.request as request, a.paymentMethod as paymentMethod," +
            "a.state as state, a.dealDate as dealDate, a.count as count, a.pricePerOne as pricePerOne, a.totalPrice as totalPrice, a.address as addressOfDeal," +
            "a.phone as phone, a.publicKey as publicKey, a.did as didOfDeal, a.didSeller as didSeller, a.didSelected as didSelectedOfDeal, a.vcState as vcState, a.buyNSell as buyNSell, a.etc as etc," +
            "a.sign as sign," +
            "c.id as id, c.productId as productIdOfImg, c.img as img " +
            "FROM Deal AS a " +
            "INNER JOIN Product AS b " +
            "ON a.productId = b.productId " +
            "LEFT OUTER JOIN Product_img AS c " +
            "ON b.productId = c.productId " +
            "WHERE a.did=#{did} " +
            "  and a.vcState = '구매확정' " +
            "ORDER BY a.dealId DESC")
    List<BuyListVO> getBuyListBuyOk(@Param("did")String did);


    //상품등록.테스트 클릭시 초기에 세팅된 6개 상품 가져오기
    @Select("SELECT a.productId as productId, a.productName as productName, a.description as description, a.price as price, a.type as type, a.did as did, a.didSelected as didSelected, " +
                   "a.address as address, a.createDate as createDate, a.createUser as createUser, a.productDid as productDid, a.manufacturerDid as manufacturerDid, " +
                   "a.manufacturer as manufacturer, a.madeDate as madeDate, a.serialNum as serialNum, a.productWallet as productWallet, a.manufacturerWallet as manufacturerWallet, " +
                   "b.id as id, b.img as img " +
            "FROM Product as a, Product_img as b " +
            "WHERE a.productId < 116 " +
              "and a.productId = b.productId " +
            "ORDER BY a.productId DESC ")
    List<ProductForQuery> getInitProductList();


    //DB 초기화를 위한 Product Table 기본 6개 제외하고 삭제
    @Delete("DELETE FROM Product WHERE productId > 115")
    void doSsiShopProductInit();

    //DB 초기화를 위한 Deal Table 삭제
    @Delete("DELETE FROM Deal")
    void doSsiShopDealInit();

    //DB 초기화를 위한 Product_img Table 기본 6개 제외하고 삭제
    @Delete("DELETE FROM Product_img WHERE productId > 115")
    void doSsiShopProductImgInit();

    //public_img에 저장
    @Insert("INSERT INTO Product_img(productId, img) VALUES(${productId}, #{img})")
    void doSaveProductImg(@Param("productId")String productId, @Param("img")String img);

    //market.PushMngr 테이블에 update전에 유무 확인
    @Select("SELECT COUNT(did) FROM PushMngr " +
            "WHERE did=#{did} " +
              "and token=#{token} ")
    int doCheckDidNToken(@Param("did")String did, @Param("token")String token);

    //market.PushMngr 테이블에 did, token 저장
    @Insert("INSERT INTO PushMngr(did, token) VALUES(#{did}, #{token})")
    void doSaveTokenMapping(@Param("did")String did, @Param("token")String token);

    // Push 알람을 보내기 위한 정보 수집. Product, Deal, PushMngr
    @Select("SELECT did, token, create_date " +
            "FROM PushMngr a " +
            "WHERE 1 = 1 " +
            "  AND a.did = #{did} " +
            "ORDER BY create_date DESC " +
            "LIMIT 1 ")
    PushMngrVO getPushToken(@Param("did")String did);

    // Product 테이블에서 productId로 조회
    @Select("SELECT * " +
            "FROM Product " +
            "WHERE productId = #{productId} ")
    Product getProductById(@Param("productId")String productId);


    //DealId만으로 push를 보내기 위한 select
    @Select("SELECT b.productName AS productName, a.did as did, a.didSeller AS didSeller, " +
                    "(SELECT token " +
                     "FROM PushMngr c " +
                     "WHERE c.did = a.did " +
                     "ORDER BY create_date DESC " +
                     "LIMIT 1 " +
                    ") AS buyerToken, " +
                    "(SELECT token " +
                     "FROM PushMngr c " +
                     "WHERE c.did = a.didSeller " +
                     "ORDER BY create_date DESC " +
                     "LIMIT 1 " +
                    ") AS sellerToken " +
            "FROM Deal AS a, Product AS b " +
            "WHERE a.dealId = #{dealId} " +
              "AND a.productId = b.productId ")
    PushMngrVO getPushTokenByDealId(@Param("dealId")String dealId);
}