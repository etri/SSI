package uniresolver.driver.did.icon;

import foundation.icon.icx.IconService;
import foundation.icon.icx.transport.http.HttpProvider;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

public class IconServiceFactory {

    private static boolean DEBUG = false;

    private IconServiceFactory() {
    }

    public static IconService createInstance(String NODE_URL) {
        return create(NODE_URL);
    }


    public static IconService create(String url) {
        return new IconService(createHttpProvider(url, DEBUG));
    }

    private static HttpProvider createHttpProvider(String url, boolean debug) {
        if (!debug) {
            return new HttpProvider(url);
        } else {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .readTimeout(100L, TimeUnit.SECONDS)
                    .build();
            return new HttpProvider(httpClient, url);
        }
    }
}