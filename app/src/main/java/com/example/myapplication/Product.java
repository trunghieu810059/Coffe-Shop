package com.example.myapplication;

public class Product {
    public String docId;
    public String name;
    public String description;
    public String category;
    public int price;

    public int imageResId;
    public String imageName;
    public String imageUrl;

    public float rating;
    public int sold;

    public int quantity;
    public boolean isFavorite;

    public Product() {
        this.docId = "";
        this.name = "";
        this.description = "";
        this.category = "";
        this.price = 0;
        this.imageResId = 0;
        this.imageName = "";
        this.imageUrl = "";
        this.rating = 0f;
        this.sold = 0;
        this.quantity = 1;
        this.isFavorite = false;
    }

    public Product(String docId,
                   String name,
                   String description,
                   String category,
                   int price,
                   int imageResId,
                   String imageName,
                   String imageUrl,
                   float rating,
                   int sold) {
        this.docId = docId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageResId = imageResId;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.sold = sold;
        this.quantity = 1;
        this.isFavorite = false;
    }

    public Product(String name, int price, int imageResId) {
        this();
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }
}