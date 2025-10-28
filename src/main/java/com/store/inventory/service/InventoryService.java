package com.store.inventory.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.store.inventory.Order;
import com.store.inventory.OrderItem;
import com.store.inventory.Product;
import com.store.inventory.SellOrder;
import com.store.inventory.Supplier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;

public class InventoryService {

  private static InventoryService instance;
  private MongoClient mongoClient;
  private MongoDatabase database;
  private MongoCollection<Document> productCollection;
  private MongoCollection<Document> supplierCollection;
  private MongoCollection<Document> orderCollection;
  private MongoCollection<Document> sellOrderCollection;

  private List<Product> inventory;
  private List<Supplier> suppliers;
  private List<Order> orders;
  private List<SellOrder> sellOrders;

  private InventoryService() {
    initializeMongoDB();
    loadDataFromMongo();
  }

  public static InventoryService getInstance() {
    if (instance == null) {
      instance = new InventoryService();
    }
    return instance;
  }

  private void initializeMongoDB() {
    try {
      mongoClient = MongoClients.create("mongodb://localhost:27017");
      database = mongoClient.getDatabase("inventory_db");
      productCollection = database.getCollection("products");
      supplierCollection = database.getCollection("suppliers");
      orderCollection = database.getCollection("orders");
      sellOrderCollection = database.getCollection("sell_orders");
    } catch (Exception e) {
      System.err.println("Failed to connect to MongoDB: " + e.getMessage());
    }
  }

  private void loadDataFromMongo() {
    inventory = new ArrayList<>();
    suppliers = new ArrayList<>();
    orders = new ArrayList<>();
    sellOrders = new ArrayList<>();

    loadProductsFromMongo();
    loadSuppliersFromMongo();
    loadOrdersFromMongo();
    loadSellOrdersFromMongo();
  }

  private void loadProductsFromMongo() {
    if (productCollection != null) {
      for (Document doc : productCollection.find()) {
        Product product = new Product(
          doc.getString("sku"),
          doc.getString("name"),
          doc.getString("description"),
          doc.getInteger("quantity", 0),
          doc.getDouble("price"),
          doc.getString("supplierId"),
          doc.getString("dateReceived")
        );
        inventory.add(product);
      }
    }
  }

  private void loadSuppliersFromMongo() {
    if (supplierCollection != null) {
      for (Document doc : supplierCollection.find()) {
        Supplier supplier = new Supplier(
          doc.getString("id"),
          doc.getString("name"),
          doc.getString("contact")
        );
        suppliers.add(supplier);
      }
    }
  }

  private void loadOrdersFromMongo() {
    if (orderCollection != null) {
      for (Document doc : orderCollection.find()) {
        List<OrderItem> items = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Document> itemDocs = (List<Document>) doc.get("items");
        if (itemDocs != null) {
          for (Document item : itemDocs) {
            items.add(
              new OrderItem(
                item.getString("sku"),
                item.getInteger("quantity", 0)
              )
            );
          }
        }

        Order order = new Order(
          doc.getString("orderId"),
          doc.getString("supplierId"),
          items,
          java.time.LocalDate.parse(doc.getString("orderDate"))
        );
        orders.add(order);
      }
    }
  }

  private void loadSellOrdersFromMongo() {
    if (sellOrderCollection != null) {
      for (Document doc : sellOrderCollection.find()) {
        List<OrderItem> items = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Document> itemDocs = (List<Document>) doc.get("items");
        if (itemDocs != null) {
          for (Document item : itemDocs) {
            items.add(
              new OrderItem(
                item.getString("sku"),
                item.getInteger("quantity", 0)
              )
            );
          }
        }

        SellOrder sellOrder = new SellOrder(
          doc.getString("sellOrderId"),
          doc.getString("customerName"),
          items,
          java.time.LocalDate.parse(doc.getString("sellDate"))
        );
        sellOrders.add(sellOrder);
      }
    }
  }

  // Product operations
  public void addProduct(Product product) {
    inventory.add(product);
    saveProductToMongo(product);
  }

  public List<Product> getAllProducts() {
    return new ArrayList<>(inventory);
  }

  public Optional<Product> findProductBySku(String sku) {
    return inventory
      .stream()
      .filter(p -> p.getSku().equals(sku))
      .findFirst();
  }

  public void updateProduct(String sku, int newQuantity, double newPrice) {
    Optional<Product> productOpt = findProductBySku(sku);
    if (productOpt.isPresent()) {
      Product product = productOpt.get();
      product.setQuantity(newQuantity);
      product.setPrice(newPrice);
      updateProductInMongo(product);
    }
  }

  public boolean removeProduct(String sku) {
    boolean removed = inventory.removeIf(p -> p.getSku().equals(sku));
    if (removed) {
      removeProductFromMongo(sku);
    }
    return removed;
  }

  public List<Product> searchProducts(String searchTerm) {
    return inventory
      .stream()
      .filter(
        p ->
          p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
          p.getSku().toLowerCase().contains(searchTerm.toLowerCase()) ||
          p.getDescription().toLowerCase().contains(searchTerm.toLowerCase())
      )
      .toList();
  }

  // Supplier operations
  public void addSupplier(Supplier supplier) {
    suppliers.add(supplier);
    saveSupplierToMongo(supplier);
  }

  public List<Supplier> getAllSuppliers() {
    return new ArrayList<>(suppliers);
  }

  public Optional<Supplier> findSupplierById(String id) {
    return suppliers
      .stream()
      .filter(s -> s.getId().equals(id))
      .findFirst();
  }

  // Order operations
  public void addOrder(Order order) {
    orders.add(order);
    saveOrderToMongo(order);
  }

  public List<Order> getAllOrders() {
    return new ArrayList<>(orders);
  }

  // Sell Order operations
  public void addSellOrder(SellOrder sellOrder) {
    sellOrders.add(sellOrder);
    saveSellOrderToMongo(sellOrder);

    // Update inventory quantities
    for (var item : sellOrder.getItems()) {
      Optional<Product> productOpt = findProductBySku(item.getSku());
      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        int newQuantity = product.getQuantity() - item.getQuantity();
        product.setQuantity(Math.max(0, newQuantity));
        updateProductInMongo(product);
      }
    }
  }

  public List<SellOrder> getAllSellOrders() {
    return new ArrayList<>(sellOrders);
  }

  // MongoDB operations
  private void saveProductToMongo(Product product) {
    if (productCollection != null) {
      Document doc = new Document("sku", product.getSku())
        .append("name", product.getName())
        .append("description", product.getDescription())
        .append("quantity", product.getQuantity())
        .append("price", product.getPrice())
        .append("supplierId", product.getSupplierId())
        .append("dateReceived", product.getDateReceived());
      productCollection.insertOne(doc);
    }
  }

  private void updateProductInMongo(Product product) {
    if (productCollection != null) {
      Document filter = new Document("sku", product.getSku());
      Document update = new Document(
        "$set",
        new Document("quantity", product.getQuantity()).append(
          "price",
          product.getPrice()
        )
      );
      productCollection.updateOne(filter, update);
    }
  }

  private void removeProductFromMongo(String sku) {
    if (productCollection != null) {
      productCollection.deleteOne(new Document("sku", sku));
    }
  }

  private void saveSupplierToMongo(Supplier supplier) {
    if (supplierCollection != null) {
      Document doc = new Document("id", supplier.getId())
        .append("name", supplier.getName())
        .append("contact", supplier.getContact());
      supplierCollection.insertOne(doc);
    }
  }

  private void saveOrderToMongo(Order order) {
    if (orderCollection != null) {
      List<Document> items = new ArrayList<>();
      for (var item : order.getItems()) {
        items.add(
          new Document("sku", item.getSku()).append(
            "quantity",
            item.getQuantity()
          )
        );
      }

      Document doc = new Document("orderId", order.getOrderId())
        .append("supplierId", order.getSupplierId())
        .append("orderDate", order.getOrderDate().toString())
        .append("items", items);
      orderCollection.insertOne(doc);
    }
  }

  private void saveSellOrderToMongo(SellOrder sellOrder) {
    if (sellOrderCollection != null) {
      List<Document> items = new ArrayList<>();
      for (var item : sellOrder.getItems()) {
        items.add(
          new Document("sku", item.getSku()).append(
            "quantity",
            item.getQuantity()
          )
        );
      }

      Document doc = new Document("sellOrderId", sellOrder.getOrderId())
        .append("customerName", sellOrder.getCustomerName())
        .append("sellDate", sellOrder.getOrderDate().toString())
        .append("items", items);
      sellOrderCollection.insertOne(doc);
    }
  }

  public void close() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  public int getTotalProducts() {
    return inventory.size();
  }

  public int getTotalQuantity() {
    return inventory.stream().mapToInt(Product::getQuantity).sum();
  }

  public double getTotalValue() {
    return inventory
      .stream()
      .mapToDouble(p -> p.getPrice() * p.getQuantity())
      .sum();
  }

  public int getLowStockCount(int threshold) {
    return (int) inventory
      .stream()
      .filter(p -> p.getQuantity() < threshold)
      .count();
  }
}
