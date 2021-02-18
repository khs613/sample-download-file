package com.khs.sample_download_file.server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(RetrofitAPI.MOCK_SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
}
