package com.iitp.core.protocol;

import android.util.Log;

import androidx.annotation.NonNull;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Async;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Client of Metadium open api server.<br>
 * default url is https://api.metadium.com/dev
 */
public class Web3jBuilder {
    private final static String NODE_URL = "https://api.metadium.com/dev";

    public static Web3j build(String url) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                Log.d("Web3jBuilder", message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        HttpService httpService = new HttpService(url == null ? NODE_URL : url, builder.build(), false);

        return  Web3j.build(httpService, 1000, Async.defaultExecutorService());
    }

    public static Web3j build() {
        return build(null);
    }
}
