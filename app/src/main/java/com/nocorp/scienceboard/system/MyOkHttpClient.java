package com.nocorp.scienceboard.system;

import android.content.Context;
import android.util.Log;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class MyOkHttpClient {
    private static MyOkHttpClient singletonInstance;
    private static OkHttpClient client;
    private final int CACHE_SIZE_IN_MBYTE = 50 * 1024 * 1024; //50mb cache



    //-------------------------------------------------------------------- CONSTRUCTORS

    private MyOkHttpClient(Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, CACHE_SIZE_IN_MBYTE);
        client = new OkHttpClient.Builder()
                .cache(cache)
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

    public static void init(Context context) {
        if(singletonInstance==null) {
            singletonInstance =  new MyOkHttpClient(context);
            Log.d(MyOkHttpClient.class.getSimpleName(), "init(): success");
        }
    }

//    public static MyOkHttpClient getInstance(Context context) {
//        init(context);
//        return singletonInstance;
//    }


    public static OkHttpClient getClient() {
        return client;
    }

}// end MyOkHttpClient
