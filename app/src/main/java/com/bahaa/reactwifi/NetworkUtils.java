package com.bahaa.reactwifi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtils {

    private static NetworkUtils sInstance;
    private NetworkConnectionRequests mNetworkRequests;
    private static final String BASE_URL = "http://192.168.4.1/";


    private NetworkUtils() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new
                OkHttpClient.Builder().addInterceptor(interceptor).readTimeout(50, TimeUnit.SECONDS)
                .connectTimeout(50, TimeUnit.SECONDS).readTimeout(50, TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        mNetworkRequests = retrofit.create(NetworkConnectionRequests.class);

    }

    public   static NetworkUtils getsInstance() {
        if (sInstance == null)
            sInstance = new NetworkUtils();
        return sInstance;
    }

    public NetworkConnectionRequests getNetworkRequests() {
        return mNetworkRequests;
    }

}
