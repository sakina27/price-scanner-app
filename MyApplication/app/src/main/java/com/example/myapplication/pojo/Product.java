package com.example.myapplication.pojo;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("Name")
    private String name;

    @SerializedName("Price")
    private String price;

    @SerializedName("Quantity")
    private String quantity;

    @SerializedName("Image URL")
    private String imageUrl;

    @SerializedName("Product URL")
    private String productURL;

    @SerializedName("Source")
    private String source;


    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }

    public String getImageUrl() { return imageUrl; }

    public String getProductURL() { return productURL; }

    public String getSource() { return source; }

}
