package com.example.myapplication;

public class Product {
    public String docId;
    public String name;
    public String description;
    public String category;
    public int price;
    public int imageResId;
    public float rating;
    public int sold;
    public int quantity;
    public boolean isFavorite;
    public String imageUrl = "";
    public Product() {
    }

    public Product(String name, int price, int imageResId) {
        this.docId = "";
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.description = "";
        this.category = "";
        this.rating = 0;
        this.sold = 0;
        this.quantity = 1;
        this.isFavorite = false;
    }

    public Product(String docId, String name, String description, String category,
                   int price, int imageResId, float rating, int sold) {
        this.docId = docId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.imageResId = imageResId;
        this.rating = rating;
        this.sold = sold;
        this.quantity = 1;
        this.isFavorite = false;
    }
}