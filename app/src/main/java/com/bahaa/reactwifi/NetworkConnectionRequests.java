package com.bahaa.reactwifi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

interface NetworkConnectionRequests {

    @GET("wi?s1=FiberHGW_TP839C_2.4GHz&p1=Pjq4vbyP&save=")
    Call<ResponseData> getData();


}
