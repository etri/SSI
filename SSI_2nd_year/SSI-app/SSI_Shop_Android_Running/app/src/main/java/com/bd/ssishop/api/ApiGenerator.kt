package com.bd.ssishop.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 객체 생성
 */
class ApiGenerator {
    companion object {
        //        val HOST = if(BuildConfig.BUILD_TYPE.equals("debug")) "http://10.0.2.2:8080" else "http://10.0.2.2:8080"
//        const val HOST = "http://10.0.2.2:8080"
        const val HOST = "http://129.254.194.112:8080"
        const val IMAGE_URL = "${HOST}/product/image?name="
    }

    /**
     * Retrofit 생성
     */
    fun <T> generate(api: Class<T>): T = Retrofit.Builder()
        .baseUrl(HOST)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient())
        .build()
        .create(api)

    /**
     * OKHttpClient 생성
     */
    private fun httpClient() = OkHttpClient.Builder().apply {
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()
}