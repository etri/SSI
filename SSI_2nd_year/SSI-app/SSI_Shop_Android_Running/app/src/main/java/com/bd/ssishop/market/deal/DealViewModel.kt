package com.bd.ssishop.market.deal

import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.api.request.DealRequest
import com.bd.ssishop.market.BuyListVO
import com.bd.ssishop.market.ProductNDeal
import com.bd.ssishop.market.product.Product
import com.bd.ssishop.service.SsiHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.info_order_frame.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import kotlin.reflect.KFunction2

/**
 * 거래내역 뷰모델
 */
class DealViewModel : ViewModel() {

    val buyList = MutableLiveData<List<BuyListVO>>()
    val sellList = MutableLiveData<List<ProductNDeal>>()

    val selected = MutableLiveData<Deal>()
    val selectedBL = MutableLiveData<BuyListVO>()
    val selectedPD = MutableLiveData<ProductNDeal>()
    val tab = MutableLiveData<String>()

    /**
     * 구매내역 API 호출
     */
    fun loadBuyList(onFailure: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.getBuyList(SsiShopApplication.token, SsiShopApplication.user.did)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    buyList.value = response.data
                } else {
                    onFailure()
                }
            }
        }
    }

    /**
     * 판매내역 API 호출
     */
    fun loadSellList(onFailure: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.getSellList(SsiShopApplication.token, SsiShopApplication.user.did)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    sellList.value = response.data
                } else {
                    onFailure()
                }
            }
        }
    }

    /**
     * 수취확인 API 호출
     */
    fun receive(onFailure: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.receive(SsiShopApplication.token, DealRequest(selectedBL.value!!.dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000) {
                    val oneVO = response.data!!
                    val buyListVO = BuyListVO()
                    buyListVO.dealId = oneVO.dealId
                    buyListVO.productId = oneVO.product.productId.toString()
                    buyListVO.productName = oneVO.product.productName
                    buyListVO.productDid = oneVO.product.productDid.toString()
                    buyListVO.type = oneVO.product.type
                    buyListVO.count = oneVO.count
                    buyListVO.price = oneVO.product.price.toInt()
                    buyListVO.buyerName = oneVO.buyerName
                    buyListVO.phone = oneVO.phone
                    buyListVO.addressOfDeal = oneVO.address
                    buyListVO.state = oneVO.state
                    buyListVO.vcState = oneVO.vcState
                    if (oneVO.product.images != null && oneVO.product.images?.size!! > 0) {
                        buyListVO.img = oneVO.product.images?.get(0)?.img.toString()
                    }else{
                        buyListVO.img = "none"
                    }

                    selectBL(buyListVO)
                } else {
                    onFailure()
                }
            }
        }
    }

    /**
     * 구매확정를 위한 거래 내역 조획
     */
    fun confirm(onFailure: (deal:Deal?) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.getSellListByDealId(SsiShopApplication.token, selectedBL.value!!.dealId)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    val deal_one = response.data
                    onFailure(deal_one)
                } else {
                    onFailure(null)
                }
            }
        }
    }

    /**
     * 새생품 발급완료
     */
    fun updateVcStateToEnd(dealId: String, onFailure: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.updateVcStateToEnd(SsiShopApplication.token, Integer.parseInt(dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    val deal_one = response.data
                } else {
                    onFailure()
                }
            }
        }
    }


    /**
     * 보증서 발급 확인 완료
     */
    fun updateVcStateToCheckCertification(dealId: String, onFailure: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.updateVcStateToCheckCertification(SsiShopApplication.token, Integer.parseInt(dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000){
//                    val deal_one = response.data
                    onFailure("SUCCESS")
                } else {
                    onFailure("FAIL")
                }
            }
        }
    }

    /**
     * 구매확정
     */
    fun updateVcStateToComplete(dealId: String, onFailure: (String) -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.updateVcStateToComplete(SsiShopApplication.token, Integer.parseInt(dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    val deal_one = response.data
                    onFailure("SUCCESS")
                } else {
                    onFailure("FAIL")
                }
            }
        }
    }

    /**
     * 보증서 요청 API 호출
     */
    fun warranty(map: Map<String, String>, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = DealRequest(selectedBL.value!!.dealId).also {
                it.did = map.get("did").toString()
                it.didLogin = map.get("didLogin").toString()
                it.didBuyer = map.get("didBuyer").toString()
                it.publicKey = map.get("publicKey").toString()
                it.sign = map.get("sign").toString()
            }
            val response = SsiApi.instance.warranty(SsiShopApplication.token, request)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    val oneVO = response.data!!
                    val buyListVO = BuyListVO()
                    buyListVO.dealId = oneVO.dealId
                    buyListVO.productId = oneVO.product.productId.toString()
                    buyListVO.productDid = oneVO.product.productDid.toString()
                    buyListVO.productName = oneVO.product.productName
                    buyListVO.type = oneVO.product.type
                    buyListVO.count = oneVO.count
                    buyListVO.price = oneVO.product.price.toInt()
                    buyListVO.buyerName = oneVO.buyerName
                    buyListVO.phone = oneVO.phone
                    buyListVO.addressOfDeal = oneVO.address
                    buyListVO.state = oneVO.state
                    buyListVO.vcState = oneVO.vcState
                    if (oneVO.product.images != null && oneVO.product.images?.size!! > 0) {
                        buyListVO.img = oneVO.product.images?.get(0)?.img.toString()
                    }else{
                        buyListVO.img = "none"
                    }
                    SsiShopApplication.productType = buyListVO.type
                    selectBL( buyListVO )

                    onFailure(response.data!!.etc.toString())

                    //FCM push requesting. dealId만으로 판매자에게 물품보증서 발급 요청 Push
                    val res = SsiApi.instance.sendPushByDealId(SsiShopApplication.token, oneVO.dealId.toString())
//                    onFailure("true")
                } else {
                    onFailure("fasle")
                }
            }
        }
    }

    /**
     * 배송 처리 API 호출
     */
    fun send(onFailure: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.send(SsiShopApplication.token, DealRequest(selected.value!!.dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    select(response.data!!)
                } else {
                    onFailure()
                }
            }
        }
    }

    /**
     * 보증서 발급 API 호출
     */
    fun issue(onFailure: (MutableMap<String, String>) -> Unit) {
        val map:MutableMap<String, String> = mutableMapOf()

        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.issue(SsiShopApplication.token, DealRequest(selectedPD.value!!.dealId))
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    select(response.data!!)
                    map.put("result", "SUCCESS")
                    map.put("publicKey", response.data!!.publicKey)
                    map.put("buyer_id", response.data!!.didBuyer)
                    map.put("user_id", response.data!!.didSeller)
                    map.put("seller_id", response.data!!.didSelected)
                    map.put("sign", response.data!!.sign)
                    map.put("price", NumberFormat.getNumberInstance().format(response.data!!.count * response.data!!.product.price))
                    map.put("date", response.data!!.dealDate)
                    map.put("buyDid", response.data!!.did)
                    onFailure(map)
                } else {
                    map.put("result", "FAIL")
                    onFailure(map)
                }
            }
        }
    }

    fun saveUsdedVcs(map:Map<String, String>, onFailure:(String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            val response = SsiApi.instance.saveUsedVcs(SsiShopApplication.token, map)
            withContext(Dispatchers.Main) {
                if (response.data!!.toString() == "true") {
                    onFailure("true")
                } else {
                    onFailure("false")
                }
            }
        }
    }


    /**
     * 선택된 거래내역 hold.
     */
    fun select(d: Deal) {
        selected.value = d
    }

    fun selectBL(d: BuyListVO) {
        selectedBL.value = d
    }

    fun selectPD(d: ProductNDeal) {
        selectedPD.value = d
    }

    /**
     * 선택된 탭 hold
     */
    fun setTab(s: String){
        tab.value = s
    }
}