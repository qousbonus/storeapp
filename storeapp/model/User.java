package com.example.storeapp.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private Role role;
    private List<Integer> favoriteProductIds = new ArrayList<>();
    private List<CartItem> cart = new ArrayList<>();

    public User() {}

    public User(int id, String username, String passwordHash, String fullName, String email, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<Integer> getFavoriteProductIds() { return favoriteProductIds; }
    public void setFavoriteProductIds(List<Integer> favoriteProductIds) { this.favoriteProductIds = favoriteProductIds; }
    public List<CartItem> getCart() { return cart; }
    public void setCart(List<CartItem> cart) { this.cart = cart; }
}
