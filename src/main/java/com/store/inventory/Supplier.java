package com.store.inventory;

public class Supplier {
  private String id;
  private String name;
  private String contact;

  public Supplier(String id, String name, String contact) {
    this.id = id;
    this.name = name;
    this.contact = contact;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getContact() {
    return contact;
  }

  @Override
  public String toString() {
    return String.format("Supplier{id='%s', name='%s', contact='%s'}", id, name, contact);
  }
}