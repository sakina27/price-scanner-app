package com.example.myapplication.Interface;

import com.example.myapplication.pojo.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search")
    Call<List<Product>> getProducts(@Query("query") String query);
}
