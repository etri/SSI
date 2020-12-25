package com.bd.ssishop.market.register

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.market.product.Product
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
 * 상품 등록 뷰모델
 */
class RegisterViewModel : ViewModel() {
    val selected = MutableLiveData<Product>()

    /**
     * 상품 등록 API 호출
     */
    fun add(p: Product, file: File, onSuccess: (Product) -> Unit){
        viewModelScope.launch{
            val body = RequestBody.create(MediaType.parse("application/json"), Gson().toJson(p))
            val files = RequestBody.create(MediaType.parse("image/*"), file)

            var fileData:MultipartBody.Part?
            val response:ApiResponse<Product>?
            if( file != null && !file.name.isEmpty() && file.exists()) {
                fileData = MultipartBody.Part.createFormData("files", file.name, files)
                response = SsiApi.instance.addProduct(SsiShopApplication.token, body, fileData)
            }else{
                response = SsiApi.instance.addProduct(SsiShopApplication.token, body, null)
            }
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    response.data?.let {
                        select(it)
                        onSuccess(it)
                    }
                }
            }
        }
    }

    fun addUsed(p: Product, file: File, onSuccess: (Product) -> Unit){
        viewModelScope.launch{
            val body = RequestBody.create(MediaType.parse("application/json"), Gson().toJson(p))
            val files = RequestBody.create(MediaType.parse("image/*"), file)

            var fileData:MultipartBody.Part?
            val response:ApiResponse<Product>?
            if( file != null && !file.name.isEmpty()) {
                fileData = MultipartBody.Part.createFormData("files", file.name, files)
                response = SsiApi.instance.addProduct(SsiShopApplication.token, body, fileData)
            }else{
                response = SsiApi.instance.addProduct(SsiShopApplication.token, body, null)
            }
            withContext(Dispatchers.Main){
                if(response.code == 1000){
                    response.data?.let {
                        select(it)
                        onSuccess(it)
                    }
                }
            }
        }
    }

    fun select(p: Product) {
        selected.value = p
    }
}