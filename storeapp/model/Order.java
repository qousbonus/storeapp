package com.example.storeapp.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private String customerName;
    private String phone;
    private String address;
    private String comment;
    private String createdAt;
    private double total;
    private String status;
    private List<OrderLine> lines = new ArrayList<>();

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public List<OrderLine> getLines() { return lines; }
    public void setLines(List<OrderLine> lines) { this.lines = lines; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
