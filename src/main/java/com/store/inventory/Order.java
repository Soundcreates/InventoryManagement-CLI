package com.store.inventory;

import java.time.LocalDate;
import java.util.List;

public class Order {
  private String orderId;
  private String supplierId;
  private List<OrderItem> items;
  private LocalDate orderDate;

  public Order(String orderId, String supplierId, List<OrderItem> items, LocalDate orderDate) {
    this.orderId = orderId;
    this.supplierId = supplierId;
    this.items = items;
    this.orderDate = orderDate;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getSupplierId() {
    return supplierId;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  @Override
  public String toString() {
    return String.format("Order{orderId='%s', supplierId='%s', items=%s, orderDate=%s}",
        orderId, supplierId, items, orderDate);
  }
}
