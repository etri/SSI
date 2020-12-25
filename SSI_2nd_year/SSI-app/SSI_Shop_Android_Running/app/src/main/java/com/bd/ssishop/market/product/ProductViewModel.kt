package com.bd.ssishop.market.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.api.request.PurchaseRequest
import com.bd.ssishop.market.deal.Deal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 상품 뷰모델
 */
class ProductViewModel : ViewModel() {
    private val _productList = MutableLiveData<List<Product>>()
    val productList: LiveData<List<Product>> = _productList
    val selected = MutableLiveData<Product>()
    val count = MutableLiveData<Int>(1)

    fun getCount(): Int {
        return count.value!!
    }
    fun setCount(value: Int) {
        count.value = value
    }


    /**
     * 상품 목록 API 호출
     */
    fun loadProducts(searchWord:String, onFailure: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            val response = SsiApi.instance.getProductList(SsiShopApplication.token, searchWord)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    _productList.value = response.data
                } else {
                    onFailure()
                }
            }
        }
    }

    /**
     * 상품 구매 API 호출
     */
    fun purchase(vp: String, paymentMethod: String, phone: String, address: String , onSuccess:(Deal) -> Unit) {
        viewModelScope.launch{
            val request = PurchaseRequest(vp, selected.value!!.productId, paymentMethod, getCount(), phone, address)
            val response = SsiApi.instance.purchase(SsiShopApplication.token, request)
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    response.data?.let { onSuccess(it) }
                }
            }
        }
    }

    /**
     * 상품 구매 API 호출
     */
//    fun doVerifyingVpForPaying(vp: String, onSuccess:(String) -> Unit) {
//        viewModelScope.launch{
//            var res: ApiResponse<String> = SsiApi.instance.doVerifyingVpForPaying(SsiShopApplication.token, vp)
//            withContext(Dispatchers.Main){
//                if(res.code == 1000){
//                    Log.d("### ForPaying:", "SUCC_qqq")
//                    res.data?.let { onSuccess(it) }
//                }
//            }
//        }
//    }

    fun select(p: Product) {
        selected.value = p
    }





}