package com.store.inventory;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import java.time.LocalDate;
import java.util.*;
import org.bson.Document;

public class App {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> productCollection;
    private static MongoCollection<Document> supplierCollection;
    private static MongoCollection<Document> orderCollection;

    private static Map<String, Product> inventory = new HashMap<>();
    private static Map<String, Supplier> suppliers = new HashMap<>();
    private static List<Order> orders = new ArrayList<>();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) {
        try {
            initializeMongoDB();
            loadDataFromMongo();
            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println(
                    ANSI_PURPLE +
                        "\n---- Inventory Management System ----" +
                        ANSI_RESET
                );
                System.out.println("1. Add Product");
                System.out.println("2. View Inventory");
                System.out.println("3. Update Product");
                System.out.println("4. Remove Product");
                System.out.println("5. Search Product");
                System.out.println("6. Generate Inventory Report");
                System.out.println("7. Add Supplier");
                System.out.println("8. View Suppliers");
                System.out.println("9. Create Order");
                System.out.println("10. View Orders");
                System.out.println("11. Create Sell Order");
                System.out.println("12. Exit" + ANSI_RESET);
                System.out.print(ANSI_CYAN + "Choose an option: " + ANSI_RESET);

                try {
                    int option = scanner.nextInt();
                    scanner.nextLine();
                    switch (option) {
                        case 1:
                            addProduct(scanner);
                            break;
                        case 2:
                            viewInventory();
                            break;
                        case 3:
                            updateProduct(scanner);
                            break;
                        case 4:
                            removeProduct(scanner);
                            break;
                        case 5:
                            searchProduct(scanner);
                            break;
                        case 6:
                            generateInventoryReport();
                            break;
                        case 7:
                            addSupplier(scanner);
                            break;
                        case 8:
                            viewSuppliers();
                            break;
                        case 9:
                            createOrder(scanner);
                            break;
                        case 10:
                            viewOrders();
                            break;
                        case 11:
                            createSellOrder(scanner);
                            break;
                        case 12:
                            running = false;
                            break;
                        default:
                            System.out.println(
                                ANSI_RED +
                                    "Invalid option! Try again." +
                                    ANSI_RESET
                            );
                    }
                } catch (InputMismatchException e) {
                    System.out.println(
                        ANSI_RED +
                            "Invalid input. Please enter a number." +
                            ANSI_RESET
                    );
                    scanner.nextLine();
                }
            }

            closeMongoDB();
            scanner.close();
        } catch (Exception e) {
            System.err.println(
                ANSI_RED +
                    "An unexpected error occurred: " +
                    e.getMessage() +
                    ANSI_RESET
            );
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void initializeMongoDB() {
        try {
            mongoClient = MongoClients.create("mongodb://localhost:27017");
            database = mongoClient.getDatabase("inventory_db");
            productCollection = database.getCollection("products");
            supplierCollection = database.getCollection("suppliers");
            orderCollection = database.getCollection("orders");
            System.out.println("Connected to MongoDB successfully");
        } catch (Exception e) {
            System.err.println(
                "Failed to connect to MongoDB: " + e.getMessage()
            );
            System.err.println(
                "Please ensure MongoDB is running on localhost:27017"
            );
            throw new RuntimeException("MongoDB connection failed", e);
        }
    }

    private static void loadDataFromMongo() {
        loadProductsFromMongo();
        loadSuppliersFromMongo();
        loadOrdersFromMongo();
    }

    private static void loadProductsFromMongo() {
        FindIterable<Document> documents = productCollection.find();
        for (Document doc : documents) {
            String sku = doc.getString("_id");
            String name = doc.getString("name");
            String description = doc.getString("description");
            int quantity = doc.getInteger("quantity");
            double price = doc.getDouble("price");
            String supplierId = doc.getString("supplierId");
            String dateReceived = doc.getString("dateReceived");

            Product product = new Product(
                sku,
                name,
                description,
                quantity,
                price,
                supplierId,
                dateReceived
            );
            inventory.put(sku, product);
        }
    }

    private static void loadSuppliersFromMongo() {
        FindIterable<Document> documents = supplierCollection.find();
        for (Document doc : documents) {
            String id = doc.getString("_id");
            String name = doc.getString("name");
            String contact = doc.getString("contact");

            Supplier supplier = new Supplier(id, name, contact);
            suppliers.put(id, supplier);
        }
    }

    private static void loadOrdersFromMongo() {
        FindIterable<Document> documents = orderCollection.find();
        for (Document doc : documents) {
            String orderId = doc.getString("_id");
            String supplierId = doc.getString("supplierId");
            List<Document> itemDocs = doc.getList("items", Document.class);
            List<OrderItem> items = new ArrayList<>();
            for (Document itemDoc : itemDocs) {
                String sku = itemDoc.getString("sku");
                int quantity = itemDoc.getInteger("quantity");
                items.add(new OrderItem(sku, quantity));
            }
            LocalDate orderDate = LocalDate.parse(doc.getString("orderDate"));

            Order order = new Order(orderId, supplierId, items, orderDate);
            orders.add(order);
        }
    }

    private static void closeMongoDB() {
        mongoClient.close();
    }

    private static void addProduct(Scanner scanner) {
        try {
            System.out.print("Enter SKU: ");
            String sku = scanner.nextLine();
            System.out.print("Enter product name: ");
            String name = scanner.nextLine();
            System.out.print("Enter product description: ");
            String description = scanner.nextLine();
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());
            if (quantity < 0) {
                throw new IllegalArgumentException(
                    "Quantity cannot be negative."
                );
            }
            System.out.print("Enter price: ");
            double price = Double.parseDouble(scanner.nextLine());
            if (price < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }
            System.out.print("Enter supplier ID: ");
            String supplierId = scanner.nextLine();
            System.out.print("Enter date received (YYYY-MM-DD): ");
            String dateReceived = scanner.nextLine();

            Product product = new Product(
                sku,
                name,
                description,
                quantity,
                price,
                supplierId,
                dateReceived
            );
            inventory.put(sku, product);
            saveProductToMongo(product);
            System.out.println("Product added to inventory.");
        } catch (NumberFormatException e) {
            System.out.println(
                ANSI_RED +
                    "Invalid input. Please enter a valid number." +
                    ANSI_RESET
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET
            );
        }
    }

    private static void viewInventory() {
        System.out.println(ANSI_BLUE + "\n---- Inventory ----" + ANSI_RESET);

        System.out.printf(
            "%-10s %-20s %-10s %-10s %-15s %-15s%n",
            "SKU",
            "Name",
            "Quantity",
            "Price",
            "Supplier ID",
            "Date Received"
        );
        System.out.println(String.join("", Collections.nCopies(85, "-")));
        for (Product product : inventory.values()) {
            System.out.printf(
                "%-10s %-20s %-10d Rs. %-9.2f %-15s %-15s%n",
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                product.getSupplierId(),
                product.getDateReceived()
            );
        }
    }

    private static void updateProduct(Scanner scanner) {
        try {
            System.out.print("Enter product SKU: ");
            String sku = scanner.nextLine();
            if (inventory.containsKey(sku)) {
                Product product = inventory.get(sku);
                System.out.println("Current product details: " + product);

                System.out.print(
                    "Enter new quantity (or press enter to skip): "
                );
                String input = scanner.nextLine();
                if (!input.isEmpty()) {
                    int newQuantity = Integer.parseInt(input);
                    if (newQuantity < 0) {
                        throw new IllegalArgumentException(
                            "Quantity cannot be negative."
                        );
                    }
                    product.setQuantity(newQuantity);
                }

                System.out.print("Enter new price (or press enter to skip): ");
                input = scanner.nextLine();
                if (!input.isEmpty()) {
                    double newPrice = Double.parseDouble(input);
                    if (newPrice < 0) {
                        throw new IllegalArgumentException(
                            "Price cannot be negative."
                        );
                    }
                    product.setPrice(newPrice);
                }

                updateProductInMongo(product);
                System.out.println("Product updated.");
            } else {
                System.out.println("Product not found in the inventory.");
            }
        } catch (NumberFormatException e) {
            System.out.println(
                ANSI_RED +
                    "Invalid input. Please enter a valid number." +
                    ANSI_RESET
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET
            );
        }
    }

    private static void removeProduct(Scanner scanner) {
        System.out.print("Enter product SKU to remove: ");
        String sku = scanner.nextLine();
        if (inventory.remove(sku) != null) {
            removeProductFromMongo(sku);
            System.out.println("Product removed from inventory.");
        } else {
            System.out.println("Product not found in the inventory.");
        }
    }

    private static void searchProduct(Scanner scanner) {
        System.out.print("Enter product SKU to search: ");
        String sku = scanner.nextLine();
        Product product = inventory.get(sku);
        if (product != null) {
            System.out.println("Product found: " + product);
        } else {
            System.out.println("Product not found in the inventory.");
        }
    }

    private static void generateInventoryReport() {
        System.out.println(
            ANSI_YELLOW + "\n---- Inventory Report ----" + ANSI_RESET
        );
        System.out.printf(
            "%-10s %-20s %-10s %-10s %-15s %-15s %-15s%n",
            "SKU",
            "Name",
            "Quantity",
            "Price",
            "Value",
            "Supplier ID",
            "Date Received"
        );
        System.out.println(String.join("", Collections.nCopies(100, "-")));
        int totalItems = 0;
        double totalValue = 0;
        for (Product product : inventory.values()) {
            double productValue = product.getQuantity() * product.getPrice();
            totalItems += product.getQuantity();
            totalValue += productValue;
            System.out.printf(
                "%-10s %-20s %-10d Rs. %-9.2f Rs. %-14.2f %-15s %-15s%n",
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                productValue,
                product.getSupplierId(),
                product.getDateReceived()
            );
        }
        System.out.println(String.join("", Collections.nCopies(100, "-")));
        System.out.println(
            ANSI_GREEN + "Total number of items: " + totalItems + ANSI_RESET
        );
        System.out.println(
            ANSI_GREEN +
                "Total inventory value: Rs. " +
                String.format("%.2f", totalValue) +
                ANSI_RESET
        );
    }

    private static void addSupplier(Scanner scanner) {
        System.out.print("Enter supplier ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter supplier name: ");
        String name = scanner.nextLine();
        System.out.print("Enter supplier contact: ");
        String contact = scanner.nextLine();

        Supplier supplier = new Supplier(id, name, contact);
        suppliers.put(id, supplier);
        saveSupplierToMongo(supplier);
        System.out.println("Supplier added.");
    }

    private static void viewSuppliers() {
        System.out.println(ANSI_BLUE + "\n---- Suppliers ----" + ANSI_RESET);
        System.out.printf("%-15s %-20s %-20s%n", "ID", "Name", "Contact");
        System.out.println(String.join("", Collections.nCopies(55, "-")));
        for (Supplier supplier : suppliers.values()) {
            System.out.printf(
                "%-15s %-20s %-20s%n",
                supplier.getId(),
                supplier.getName(),
                supplier.getContact()
            );
        }
    }

    private static void createOrder(Scanner scanner) {
        try {
            System.out.print("Enter order ID: ");
            String orderId = scanner.nextLine();
            System.out.print("Enter supplier ID: ");
            String supplierId = scanner.nextLine();

            List<OrderItem> items = new ArrayList<>();
            boolean addingItems = true;
            while (addingItems) {
                System.out.print("Enter product SKU (or 'done' to finish): ");
                String sku = scanner.nextLine();
                if (sku.equalsIgnoreCase("done")) {
                    addingItems = false;
                } else if (inventory.containsKey(sku)) {
                    System.out.print("Enter quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    if (quantity <= 0) {
                        throw new IllegalArgumentException(
                            "Quantity must be positive."
                        );
                    }
                    items.add(new OrderItem(sku, quantity));

                    // Update inventory
                    Product product = inventory.get(sku);
                    product.setQuantity(product.getQuantity() + quantity);
                    updateProductInMongo(product);
                } else {
                    System.out.println(
                        ANSI_RED + "Product not found. Try again." + ANSI_RESET
                    );
                }
            }

            Order order = new Order(
                orderId,
                supplierId,
                items,
                LocalDate.now()
            );
            orders.add(order);
            saveOrderToMongo(order);
            System.out.println(
                ANSI_GREEN + "Order created and inventory updated." + ANSI_RESET
            );
        } catch (NumberFormatException e) {
            System.out.println(
                ANSI_RED +
                    "Invalid input. Please enter a valid number." +
                    ANSI_RESET
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET
            );
        }
    }

    private static void viewOrders() {
        System.out.println(ANSI_BLUE + "\n---- Orders ----" + ANSI_RESET);
        for (Order order : orders) {
            System.out.printf("Order ID: %s%n", order.getOrderId());
            System.out.printf("Supplier ID: %s%n", order.getSupplierId());
            System.out.printf("Order Date: %s%n", order.getOrderDate());
            System.out.println("Items:");
            System.out.printf(
                "  %-10s %-20s %-10s %-10s %-10s%n",
                "SKU",
                "Name",
                "Quantity",
                "Price",
                "Total"
            );
            System.out.println(
                "  " + String.join("", Collections.nCopies(20, "-"))
            );
            double orderTotal = 0;
            for (OrderItem item : order.getItems()) {
                Product product = inventory.get(item.getSku());
                double itemTotal = item.getQuantity() * product.getPrice();
                orderTotal += itemTotal;
                System.out.printf(
                    "  %-10s %-20s %-10d Rs. %-9.2f Rs. %-9.2f%n",
                    item.getSku(),
                    product.getName(),
                    item.getQuantity(),
                    product.getPrice(),
                    itemTotal
                );
            }
            System.out.println(String.join("", Collections.nCopies(20, "-")));
            System.out.println(
                ANSI_GREEN +
                    "  Total Order Value: Rs. " +
                    String.format("%.2f", orderTotal) +
                    ANSI_RESET
            );
            System.out.println();
        }
    }

    private static void saveProductToMongo(Product product) {
        Document doc = new Document("_id", product.getSku())
            .append("name", product.getName())
            .append("description", product.getDescription())
            .append("quantity", product.getQuantity())
            .append("price", product.getPrice())
            .append("supplierId", product.getSupplierId())
            .append("dateReceived", product.getDateReceived());
        try {
            productCollection.insertOne(doc);
        } catch (com.mongodb.MongoWriteException e) {
            if (
                e.getError().getCategory() ==
                com.mongodb.ErrorCategory.DUPLICATE_KEY
            ) {
                System.out.println(
                    ANSI_RED +
                        "Error: Product with SKU " +
                        product.getSku() +
                        " already exists in the database." +
                        ANSI_RESET
                );
            } else {
                System.out.println(
                    ANSI_RED +
                        "Error saving product to database: " +
                        e.getMessage() +
                        ANSI_RESET
                );
            }
        }
    }

    private static void updateProductInMongo(Product product) {
        Document doc = new Document("quantity", product.getQuantity()).append(
            "price",
            product.getPrice()
        );
        productCollection.updateOne(
            Filters.eq("_id", product.getSku()),
            new Document("$set", doc)
        );
    }

    private static void removeProductFromMongo(String sku) {
        productCollection.deleteOne(Filters.eq("_id", sku));
    }

    private static void saveSupplierToMongo(Supplier supplier) {
        Document doc = new Document("_id", supplier.getId())
            .append("name", supplier.getName())
            .append("contact", supplier.getContact());
        try {
            supplierCollection.insertOne(doc);
        } catch (com.mongodb.MongoWriteException e) {
            if (
                e.getError().getCategory() ==
                com.mongodb.ErrorCategory.DUPLICATE_KEY
            ) {
                System.out.println(
                    ANSI_RED +
                        "Error: Supplier with ID " +
                        supplier.getId() +
                        " already exists in the database." +
                        ANSI_RESET
                );
            } else {
                System.out.println(
                    ANSI_RED +
                        "Error saving supplier to database: " +
                        e.getMessage() +
                        ANSI_RESET
                );
            }
        }
    }

    private static void saveOrderToMongo(Order order) {
        List<Document> itemDocuments = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Document itemDoc = new Document("sku", item.getSku()).append(
                "quantity",
                item.getQuantity()
            );
            itemDocuments.add(itemDoc);
        }

        Document doc = new Document("_id", order.getOrderId())
            .append("supplierId", order.getSupplierId())
            .append("items", itemDocuments)
            .append("orderDate", order.getOrderDate().toString());
        try {
            orderCollection.insertOne(doc);
        } catch (com.mongodb.MongoWriteException e) {
            if (
                e.getError().getCategory() ==
                com.mongodb.ErrorCategory.DUPLICATE_KEY
            ) {
                System.out.println(
                    ANSI_RED +
                        "Error: Order with ID " +
                        order.getOrderId() +
                        " already exists in the database." +
                        ANSI_RESET
                );
            } else {
                System.out.println(
                    ANSI_RED +
                        "Error saving order to database: " +
                        e.getMessage() +
                        ANSI_RESET
                );
            }
        }
    }

    private static void createSellOrder(Scanner scanner) {
        try {
            System.out.print("Enter sell order ID: ");
            String orderId = scanner.nextLine();
            System.out.print("Enter customer name: ");
            String customerName = scanner.nextLine();

            List<OrderItem> items = new ArrayList<>();
            boolean addingItems = true;
            while (addingItems) {
                System.out.print("Enter product SKU (or 'done' to finish): ");
                String sku = scanner.nextLine();
                if (sku.equalsIgnoreCase("done")) {
                    addingItems = false;
                } else if (inventory.containsKey(sku)) {
                    System.out.print("Enter quantity: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    if (quantity <= 0) {
                        throw new IllegalArgumentException(
                            "Quantity must be positive."
                        );
                    }
                    Product product = inventory.get(sku);
                    if (product.getQuantity() >= quantity) {
                        items.add(new OrderItem(sku, quantity));
                        product.setQuantity(product.getQuantity() - quantity);
                        updateProductInMongo(product);
                    } else {
                        System.out.println(
                            ANSI_RED +
                                "Insufficient stock. Available: " +
                                product.getQuantity() +
                                ANSI_RESET
                        );
                    }
                } else {
                    System.out.println("Product not found. Try again.");
                }
            }

            if (!items.isEmpty()) {
                SellOrder sellOrder = new SellOrder(
                    orderId,
                    customerName,
                    items,
                    LocalDate.now()
                );
                // Note: SellOrder is saved separately, not in the orders list
                saveSellOrderToMongo(sellOrder);
                System.out.println(
                    ANSI_GREEN + "Sell order created." + ANSI_RESET
                );
            } else {
                System.out.println(
                    ANSI_YELLOW +
                        "No items added to the sell order." +
                        ANSI_RESET
                );
            }
        } catch (NumberFormatException e) {
            System.out.println(
                ANSI_RED +
                    "Invalid input. Please enter a valid number." +
                    ANSI_RESET
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                ANSI_RED + "Error: " + e.getMessage() + ANSI_RESET
            );
        }
    }

    private static void saveSellOrderToMongo(SellOrder sellOrder) {
        List<Document> itemDocuments = new ArrayList<>();
        for (OrderItem item : sellOrder.getItems()) {
            Document itemDoc = new Document("sku", item.getSku()).append(
                "quantity",
                item.getQuantity()
            );
            itemDocuments.add(itemDoc);
        }

        // Use a separate collection for sell orders
        MongoCollection<Document> sellOrderCollection = database.getCollection(
            "sell_orders"
        );
        Document doc = new Document("_id", sellOrder.getOrderId())
            .append("customerName", sellOrder.getCustomerName())
            .append("items", itemDocuments)
            .append("sellDate", sellOrder.getOrderDate().toString());
        try {
            sellOrderCollection.insertOne(doc);
        } catch (com.mongodb.MongoWriteException e) {
            if (
                e.getError().getCategory() ==
                com.mongodb.ErrorCategory.DUPLICATE_KEY
            ) {
                System.out.println(
                    ANSI_RED +
                        "Error: Sell order with ID " +
                        sellOrder.getOrderId() +
                        " already exists in the database." +
                        ANSI_RESET
                );
            } else {
                System.out.println(
                    ANSI_RED +
                        "Error saving sell order to database: " +
                        e.getMessage() +
                        ANSI_RESET
                );
            }
        }
    }
}
