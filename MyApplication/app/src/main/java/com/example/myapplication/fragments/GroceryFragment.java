package com.example.myapplication.fragments;
import static android.content.ContentValues.TAG;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myapplication.Interface.ApiService;
import com.example.myapplication.R;
import com.example.myapplication.RecyclerView.ProductAdaptor;
import com.example.myapplication.RetrofitClient;
import com.example.myapplication.pojo.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GroceryFragment extends Fragment {

    private EditText searchBox;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProductAdaptor adapter;

    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groceries, container, false);

        searchBox = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        searchButton.setOnClickListener(v -> fetchProducts(searchBox.getText().toString()));

        return view;
    }

    private void fetchProducts(String query) {
        Log.d(TAG, "Fetching products for query: " + query);

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        Retrofit retrofit = RetrofitClient.getClient();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<List<Product>> call = apiService.getProducts(query);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                Log.d(TAG, "Response received: " + response.toString());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Response body: " + response.body());

                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (response.body() != null) {
                        adapter = new ProductAdaptor(getContext(), response.body());
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response unsuccessful. Code: " + response.code());
                    Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
