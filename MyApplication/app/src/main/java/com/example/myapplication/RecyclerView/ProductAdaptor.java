package com.example.myapplication.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.pojo.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdaptor extends RecyclerView.Adapter<ProductAdaptor.ViewHolder> {
    private final Context context;
    private final List<Product> productList;

    public ProductAdaptor(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.zeptoTag.setText(product.getSource());
        holder.quantity.setText(product.getQuantity());

        // Format prices properly using currency formatter
        Double price = product.getPrice() != null ? Double.valueOf(product.getPrice()) : 0.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        holder.productPrice.setText("Price: " + currencyFormat.format(price));

        // Load image with Glide for smooth rendering
        Glide.with(context)
                .load(product.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .fallback(R.drawable.default_image)) // In case URL is null
                .into(holder.productImage);

        // Open product URL in browser when clicked
        holder.itemView.setOnClickListener(v -> {
            String url = product.getProductURL();
            if (!TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Invalid product URL", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Buy Now button click
        holder.buyButton.setOnClickListener(v -> {
            String url = product.getProductURL();
            if (!TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Invalid product URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView productName, productPrice, quantity, zeptoTag;
        final ImageView productImage;
        final Button buyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            quantity = itemView.findViewById(R.id.quantity);
            productImage = itemView.findViewById(R.id.productImage);
            zeptoTag = itemView.findViewById(R.id.zeptoTag);
            buyButton = itemView.findViewById(R.id.buyButton);
        }
    }
}
