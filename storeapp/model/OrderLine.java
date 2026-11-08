package com.example.storeapp.model;

public class OrderLine {
    private String productName;
    private int quantity;
    private double unitPrice;
    private int productId;

    public OrderLine() {}

    public OrderLine(int productId,String productName, int quantity, double unitPrice) {
        this.productName = productName;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

}
