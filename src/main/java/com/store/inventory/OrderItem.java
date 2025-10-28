package com.store.inventory;

public class OrderItem {
  private String sku;
  private int quantity;

  public OrderItem(String sku, int quantity) {
    this.sku = sku;
    this.quantity = quantity;
  }

  public String getSku() {
    return sku;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String toString() {
    return String.format("OrderItem{sku='%s', quantity=%d}", sku, quantity);
  }
}