package com.store.inventory;

import java.time.LocalDate;
import java.util.List;

public class SellOrder extends Order {

  private String customerName;

  public SellOrder(
    String orderId,
    String customerName,
    List<OrderItem> items,
    LocalDate orderDate
  ) {
    super(orderId, null, items, orderDate);
    this.customerName = customerName;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getSellOrderId() {
    return getOrderId();
  }
}
