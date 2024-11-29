package com.example.securitymerch.models;

public class Product {
    private String name;
    private int quantity;
    private String imageUrl;
    private String barcode;

    // Constructor vacío (requerido por Firestore)
    public Product(String name, int i, String category, String string, String barcode) {}

    // Constructor completo
    public Product(String name, int quantity, String imageUrl, String barcode) {
        this.name = name;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.barcode = barcode;
    }

    // Getters y Setters
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

}
