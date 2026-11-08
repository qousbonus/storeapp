package com.example.storeapp.model;

public class Review {
    private int userId;
    private int productId;
    private String text;
    private int rating;

    public Review(int userId, int productId, String text, int rating) {
        this.userId = userId;
        this.productId = productId;
        this.text = text;
        this.rating = rating;
    }

    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public String getText() { return text; }
    public int getRating() { return rating; }
}