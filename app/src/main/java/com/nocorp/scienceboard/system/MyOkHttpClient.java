package com.nocorp.scienceboard.system;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class MyOkHttpClient {
    private static MyOkHttpClient singletonInstance;
    private static OkHttpClient client;
    private final int CACHE_SIZE_IN_MBYTE = 50 * 1024 * 1024; //50mb cache
    private final int TIMEOUT = 5;
    private final TimeUnit TIME_UNIT = TimeUnit.SECONDS;



    //-------------------------------------------------------------------- CONSTRUCTORS

    private MyOkHttpClient(Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, CACHE_SIZE_IN_MBYTE);
        client = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(TIMEOUT, TIME_UNIT)
                .writeTimeout(TIMEOUT, TIME_UNIT)
                .readTimeout(TIMEOUT, TIME_UNIT)
                .build();

        // DON'T FORGET TO MAKE RETROFIT TO USE IT, OTW A SOCKET EXCEPTION CAN OCCUR
//        final Retrofit retrofitClient = new Retrofit.Builder()
//                .client(OkHttpSingleton.getClient())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(getBaseURL())
//                .build();


        // in case of SocketTimoutException, increase timeouts
//        client = new OkHttpClient.Builder()
//                .cache(cache)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .build();

        // in case the soluton above doesn't fix the problem
//        client = new OkHttpClient.Builder()
//                .cache(cache)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .connectionPool(new ConnectionPool(0, 5, TimeUnit.MINUTES))
//                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
//                .build();
    }// end MyOkHttpClient()




    //-------------------------------------------------------------------- METHODS

    public static MyOkHttpClient init(Context context) {
        if(singletonInstance==null)
            return new MyOkHttpClient(context);

        return singletonInstance;
    }


    public static OkHttpClient getClient() {
        return client;
    }

}// end MyOkHttpClient
