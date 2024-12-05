package com.example.securitymerch.models;

public class Product {

    private String name;
    private int quantity;
    private String imageUrl;
    private String barcode;
    private String userId;

    // Constructor vacío requerido para Firestore
    public Product() {
    }

    public Product(String name, int quantity, String imageUrl, String barcode, String userId) {
        this.name = name;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.barcode = barcode;
        this.userId = userId;
    }

    // Métodos getter y setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
