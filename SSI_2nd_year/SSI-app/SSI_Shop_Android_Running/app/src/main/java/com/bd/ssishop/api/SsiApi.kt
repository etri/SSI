package com.bd.ssishop.api

import com.bd.ssishop.api.request.DealRequest
import com.bd.ssishop.api.request.LoginRequest
import com.bd.ssishop.api.request.PurchaseRequest
import com.bd.ssishop.api.request.TokenRequest
import com.bd.ssishop.api.response.LoginResponse
import com.bd.ssishop.login.User
import com.bd.ssishop.market.BuyListVO
import com.bd.ssishop.market.ProductNDeal
import com.bd.ssishop.market.deal.Deal
import com.bd.ssishop.market.product.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * SSI API 인터페이스
 */
interface SsiApi {
    /**
     * test용 API
     */
    @GET("/test")
    suspend fun test(): ApiResponse<String>

    /**
     * login API
     */
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    /**
     * FCM 토큰 업데이트 API
     */
    @POST("/token")
    suspend fun token(@Header("Authorization") token: String, @Body request: TokenRequest): ApiResponse<String>

    /**
     * 상품 목록 API
     */
    @POST("/product/list")
    suspend fun getProductList(@Header("Authorization") token: String, @Body searchWord:String): ApiResponse<List<Product>>

    /**
     * 상품 구매 API
     */
    @POST("/product/purchase")
    suspend fun purchase(@Header("Authorization") token: String, @Body request: PurchaseRequest): ApiResponse<Deal>

    /**
     * 구매 목록 API
     */
    @POST("/deal/buyList")
    suspend fun getBuyList(@Header("Authorization") token: String, @Body did: String): ApiResponse<List<BuyListVO>>

    /**
     * 구매 목록 API - 구매확정만
     */
    @POST("/deal/buyListBuyOk")
    suspend fun getBuyListBuyOk(@Header("Authorization") token: String, @Body did: String): ApiResponse<List<BuyListVO>>

    /**
     * 판매 목록 API
     */
    @POST("/deal/sellList")
    suspend fun getSellList(@Header("Authorization") token: String, @Body did: String): ApiResponse<List<ProductNDeal>>

    /**
     * 수취 확인 API
     */
    @POST("/deal/receive")
    suspend fun receive(@Header("Authorization") token: String, @Body request: DealRequest): ApiResponse<Deal>

    /**
     * 구매 확정 API
     */
    @POST("/deal/confirm")
    suspend fun confirm(@Header("Authorization") token: String, @Body request: DealRequest): ApiResponse<Deal>

    /**
     * 보증서 요청 API
     */
    @POST("/deal/warranty")
    suspend fun warranty(@Header("Authorization") token: String, @Body request: DealRequest): ApiResponse<Deal>

    /**
     * 배송 처리 API
     */
    @POST("/deal/send")
    suspend fun send(@Header("Authorization") token: String, @Body request: DealRequest): ApiResponse<Deal>

    /**
     * 보증서 발급 API
     */
    @POST("/deal/issue")
    suspend fun issue(@Header("Authorization") token: String, @Body request: DealRequest): ApiResponse<Deal>

    /**
     * 상품 등록 API
     */
    @Multipart
    @POST("/product/add")
    suspend fun addProduct(@Header("Authorization") token: String, @Part("body") body: RequestBody, @Part files: MultipartBody.Part?): ApiResponse<Product>

    companion object {
        val instance = ApiGenerator().generate(SsiApi::class.java)
    }




    //================= 추가된 부분 ======================================================
    /**
     * VC 로그인을 위해서 Metadium SDK를 이용하여 VP 검증
     */
    @POST("/common/verifyingVpForVcLogin")
    suspend fun doVerifyingVpForVcLogin(@Header("Authorization") token: String, @Body vp: MutableMap<String, String>): ApiResponse<String>

    /**
     * 결제를 위해서 Metadium SDK를 이용하여 VP 검증
     */
    @POST("/common/verifyingVpForPaying")
    suspend fun doVerifyingVpForPaying(@Header("Authorization") token: String, @Body vps: Map<String, String>): ApiResponse<String>

    /**
     * 결제를 위해서 Metadium SDK를 이용하여 VP 검증
     */
    @POST("/common/verifyingVpForUsedRegi")
    suspend fun doVerifyingVpForUsedRegi(@Header("Authorization") token: String, @Body vps: Map<String, String>): ApiResponse<String>

    /**
     * 구매내역 저장
     */
    @POST("/deal/addbuying")
    suspend fun addBuying(@Header("Authorization") token: String, @Body valueMap: Map<String, String>): ApiResponse<Deal>

    /**
     * 중고품 등록
     */
    @POST("/deal/addUsed")
    suspend fun addUsed(@Header("Authorization") token: String, @Body product: Product): ApiResponse<String>

    /**
     * 중고품 물품 보증서 발급 후 ssi://vclist로 넘기기 전 생성된 거래증명VC, 물품정보VC 저장.
     */
    @POST("/deal/saveUsedVcs")
    suspend fun saveUsedVcs(@Header("Authorization") token: String, @Body request: Map<String, String>): ApiResponse<String>

    /**
     * dealId로 구매내역 가져오기
     */
    @POST("/deal/sellListByDealId")
    suspend fun getSellListByDealId(@Header("Authorization") token: String, @Body dealId: Int): ApiResponse<Deal>

    /**
     * 새상품 물품보증서요청 -> 구매확정으로 상태 변경
     */
    @POST("/deal/updateVcStateToEnd")
    suspend fun updateVcStateToEnd(@Header("Authorization") token: String, @Body dealId:Int): ApiResponse<String>

    /**
     * 발급완료 -> 물품보증서 발급 확인으로 상태 변경
     */
    @POST("/deal/updateVcStateToCheckCertification")
    suspend fun updateVcStateToCheckCertification(@Header("Authorization") token: String, @Body dealId:Int): ApiResponse<String>


    /**
     * 발급완료 -> 구매확정으로 상태 변경
     */
    @POST("/deal/updateVcStateToComplete")
    suspend fun updateVcStateToComplete(@Header("Authorization") token: String, @Body dealId:Int): ApiResponse<String>

    /**
     * 상품등록.테스트 클릭시 초기에 세팅된 6개 상품 가져오기
     */
    @POST("/product/initProductList")
    suspend fun getInitProductList(@Header("Authorization") token: String): ApiResponse<List<Product>>

    /**
     * 초기화 메뉴 클릭시 실행.
     */
    @GET("/common/product_init")
    suspend fun doProductInit():ApiResponse<String>

    /**
     * push 보내기
     */
    @POST("/fcmcallD")
    suspend fun sendPush(@Header("Authorization") token: String, @Body request: Map<String, String>): ApiResponse<String>

    /**
     * push 보내기 - dealId만 있는 경우
     */
    @POST("/fcmcallDByDealId")
    suspend fun sendPushByDealId(@Header("Authorization") token: String, @Body request: String): ApiResponse<String>

    /**
     * dealId로 구매 목록 API
     */
    @POST("/deal/buyHistByDealId")
    suspend fun getBuyHistByDealId(@Header("Authorization") token: String, @Body dealId: String): ApiResponse<BuyListVO>
}