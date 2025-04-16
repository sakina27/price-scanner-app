package com.example.myapplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS) // Increase connection timeout
                    .readTimeout(120, TimeUnit.SECONDS)    // Increase read timeout
                    .writeTimeout(120, TimeUnit.SECONDS)   // Increase write timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.39.217:8080/") // Use the correct base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
