package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Review {
    public String id;
    public String username;
    public String customerName;
    public String orderId;
    public double rating;
    public String comment;
    public Timestamp createdAt;
    public boolean isHidden;
    public String adminNote;
    public String adminReply;

    public Review() {
    }
}