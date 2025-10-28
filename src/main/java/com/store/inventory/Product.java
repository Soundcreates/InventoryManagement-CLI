package com.store.inventory;

public class Product {
    private String sku;
    private String name;
    private String description;
    private int quantity;
    private double price;
    private String supplierId;
    private String dateReceived;

    public Product(String sku, String name, String description, int quantity, double price, String supplierId,
            String dateReceived) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.supplierId = supplierId;
        this.dateReceived = dateReceived;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getDateReceived() {
        return dateReceived;
    }

    @Override
    public String toString() {
        return String.format(
                "Product{SKU='%s', name='%s', description='%s', quantity=%d, price=%.2f, supplierId='%s', dateReceived='%s'}",
                sku, name, description, quantity, price, supplierId, dateReceived);
    }
}