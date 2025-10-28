package com.store.inventory.gui;

import com.store.inventory.Order;
import com.store.inventory.OrderItem;
import com.store.inventory.Product;
import com.store.inventory.SellOrder;
import com.store.inventory.Supplier;
import com.store.inventory.service.InventoryService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class InventoryGUI extends Application {

  private InventoryService inventoryService;
  private Stage primaryStage;

  // Tables
  private TableView<Product> productTable;
  private TableView<Supplier> supplierTable;
  private TableView<Order> orderTable;
  private TableView<SellOrder> sellOrderTable;

  // Observable lists
  private ObservableList<Product> productData;
  private ObservableList<Supplier> supplierData;
  private ObservableList<Order> orderData;
  private ObservableList<SellOrder> sellOrderData;

  // Dashboard labels
  private Label totalProductsLabel;
  private Label totalQuantityLabel;
  private Label totalValueLabel;
  private Label lowStockLabel;

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.inventoryService = InventoryService.getInstance();

    primaryStage.setTitle("Inventory Management System - GUI");
    primaryStage.setMaximized(true);

    TabPane mainTabPane = createMainTabPane();

    Scene scene = new Scene(mainTabPane, 1200, 800);
    try {
      scene
        .getStylesheets()
        .add(getClass().getResource("/styles.css").toExternalForm());
    } catch (Exception e) {
      // CSS file not found, continue without styling
      System.out.println("CSS file not found, using default styling");
    }

    primaryStage.setScene(scene);
    primaryStage.show();

    // Handle application close
    primaryStage.setOnCloseRequest(e -> {
      inventoryService.close();
      Platform.exit();
    });

    // Load initial data
    refreshAllData();
  }

  private TabPane createMainTabPane() {
    TabPane tabPane = new TabPane();

    // Dashboard Tab
    Tab dashboardTab = new Tab("Dashboard");
    dashboardTab.setClosable(false);
    dashboardTab.setContent(createDashboardPane());

    // Products Tab
    Tab productsTab = new Tab("Products");
    productsTab.setClosable(false);
    productsTab.setContent(createProductsPane());

    // Suppliers Tab
    Tab suppliersTab = new Tab("Suppliers");
    suppliersTab.setClosable(false);
    suppliersTab.setContent(createSuppliersPane());

    // Orders Tab
    Tab ordersTab = new Tab("Purchase Orders");
    ordersTab.setClosable(false);
    ordersTab.setContent(createOrdersPane());

    // Sell Orders Tab
    Tab sellOrdersTab = new Tab("Sell Orders");
    sellOrdersTab.setClosable(false);
    sellOrdersTab.setContent(createSellOrdersPane());

    tabPane
      .getTabs()
      .addAll(
        dashboardTab,
        productsTab,
        suppliersTab,
        ordersTab,
        sellOrdersTab
      );

    return tabPane;
  }

  private VBox createDashboardPane() {
    VBox dashboardPane = new VBox(20);
    dashboardPane.setPadding(new Insets(20));

    // Title
    Label titleLabel = new Label("Inventory Dashboard");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
    titleLabel.setTextFill(Color.DARKBLUE);

    // Statistics Cards
    GridPane statsGrid = new GridPane();
    statsGrid.setHgap(20);
    statsGrid.setVgap(20);
    statsGrid.setAlignment(Pos.CENTER);

    // Total Products Card
    VBox totalProductsCard = createStatsCard("Total Products", "0");
    totalProductsLabel = (Label) ((VBox) totalProductsCard
        .getChildren()
        .get(1)).getChildren().get(0);

    // Total Quantity Card
    VBox totalQuantityCard = createStatsCard("Total Quantity", "0");
    totalQuantityLabel = (Label) ((VBox) totalQuantityCard
        .getChildren()
        .get(1)).getChildren().get(0);

    // Total Value Card
    VBox totalValueCard = createStatsCard("Total Value", "$0.00");
    totalValueLabel = (Label) ((VBox) totalValueCard
        .getChildren()
        .get(1)).getChildren().get(0);

    // Low Stock Card
    VBox lowStockCard = createStatsCard("Low Stock Items", "0");
    lowStockLabel = (Label) ((VBox) lowStockCard
        .getChildren()
        .get(1)).getChildren().get(0);

    statsGrid.add(totalProductsCard, 0, 0);
    statsGrid.add(totalQuantityCard, 1, 0);
    statsGrid.add(totalValueCard, 0, 1);
    statsGrid.add(lowStockCard, 1, 1);

    // Recent Activity Section
    Label recentLabel = new Label("Recent Products");
    recentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

    TableView<Product> recentProductsTable = createProductTable();
    recentProductsTable.setPrefHeight(300);

    Button refreshButton = new Button("Refresh Dashboard");
    refreshButton.setOnAction(e -> refreshDashboard());
    refreshButton.getStyleClass().add("primary-button");

    dashboardPane
      .getChildren()
      .addAll(
        titleLabel,
        statsGrid,
        recentLabel,
        recentProductsTable,
        refreshButton
      );

    return dashboardPane;
  }

  private VBox createStatsCard(String title, String value) {
    VBox card = new VBox(10);
    card.setPadding(new Insets(20));
    card.setAlignment(Pos.CENTER);
    card.getStyleClass().add("stats-card");
    card.setPrefSize(200, 120);

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
    titleLabel.setTextFill(Color.GRAY);

    VBox valueContainer = new VBox();
    valueContainer.setAlignment(Pos.CENTER);
    Label valueLabel = new Label(value);
    valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    valueLabel.setTextFill(Color.DARKBLUE);
    valueContainer.getChildren().add(valueLabel);

    card.getChildren().addAll(titleLabel, valueContainer);

    return card;
  }

  private BorderPane createProductsPane() {
    BorderPane productPane = new BorderPane();

    // Product table
    productTable = createProductTable();
    productData = FXCollections.observableArrayList();
    productTable.setItems(productData);

    // Search box
    HBox searchBox = new HBox(10);
    searchBox.setPadding(new Insets(10));
    TextField searchField = new TextField();
    searchField.setPromptText("Search products...");
    Button searchButton = new Button("Search");
    searchButton.setOnAction(e -> searchProducts(searchField.getText()));
    Button clearButton = new Button("Clear");
    clearButton.setOnAction(e -> {
      searchField.clear();
      refreshProductTable();
    });
    searchBox
      .getChildren()
      .addAll(new Label("Search:"), searchField, searchButton, clearButton);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setPadding(new Insets(10));

    Button addButton = new Button("Add Product");
    Button editButton = new Button("Edit Product");
    Button deleteButton = new Button("Delete Product");
    Button refreshButton = new Button("Refresh");

    addButton.setOnAction(e -> showAddProductDialog());
    editButton.setOnAction(e -> showEditProductDialog());
    deleteButton.setOnAction(e -> deleteSelectedProduct());
    refreshButton.setOnAction(e -> refreshProductTable());

    buttonBox
      .getChildren()
      .addAll(addButton, editButton, deleteButton, refreshButton);

    VBox topBox = new VBox(searchBox, buttonBox);
    productPane.setTop(topBox);
    productPane.setCenter(productTable);

    return productPane;
  }

  private BorderPane createSuppliersPane() {
    BorderPane supplierPane = new BorderPane();

    // Supplier table
    supplierTable = createSupplierTable();
    supplierData = FXCollections.observableArrayList();
    supplierTable.setItems(supplierData);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setPadding(new Insets(10));

    Button addButton = new Button("Add Supplier");
    Button deleteButton = new Button("Delete Supplier");
    Button refreshButton = new Button("Refresh");

    addButton.setOnAction(e -> showAddSupplierDialog());
    deleteButton.setOnAction(e -> deleteSelectedSupplier());
    refreshButton.setOnAction(e -> refreshSupplierTable());

    buttonBox.getChildren().addAll(addButton, deleteButton, refreshButton);

    supplierPane.setTop(buttonBox);
    supplierPane.setCenter(supplierTable);

    return supplierPane;
  }

  private BorderPane createOrdersPane() {
    BorderPane orderPane = new BorderPane();

    // Order table
    orderTable = createOrderTable();
    orderData = FXCollections.observableArrayList();
    orderTable.setItems(orderData);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setPadding(new Insets(10));

    Button addButton = new Button("Create Order");
    Button refreshButton = new Button("Refresh");

    addButton.setOnAction(e -> showCreateOrderDialog());
    refreshButton.setOnAction(e -> refreshOrderTable());

    buttonBox.getChildren().addAll(addButton, refreshButton);

    orderPane.setTop(buttonBox);
    orderPane.setCenter(orderTable);

    return orderPane;
  }

  private BorderPane createSellOrdersPane() {
    BorderPane sellOrderPane = new BorderPane();

    // Sell Order table
    sellOrderTable = createSellOrderTable();
    sellOrderData = FXCollections.observableArrayList();
    sellOrderTable.setItems(sellOrderData);

    // Buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setPadding(new Insets(10));

    Button addButton = new Button("Create Sell Order");
    Button refreshButton = new Button("Refresh");

    addButton.setOnAction(e -> showCreateSellOrderDialog());
    refreshButton.setOnAction(e -> refreshSellOrderTable());

    buttonBox.getChildren().addAll(addButton, refreshButton);

    sellOrderPane.setTop(buttonBox);
    sellOrderPane.setCenter(sellOrderTable);

    return sellOrderPane;
  }

  private TableView<Product> createProductTable() {
    TableView<Product> table = new TableView<>();

    TableColumn<Product, String> skuCol = new TableColumn<>("SKU");
    skuCol.setCellValueFactory(new PropertyValueFactory<>("sku"));
    skuCol.setPrefWidth(100);

    TableColumn<Product, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(150);

    TableColumn<Product, String> descCol = new TableColumn<>("Description");
    descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    descCol.setPrefWidth(200);

    TableColumn<Product, Integer> qtyCol = new TableColumn<>("Quantity");
    qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    qtyCol.setPrefWidth(100);

    TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    priceCol.setPrefWidth(100);

    TableColumn<Product, String> supplierCol = new TableColumn<>("Supplier ID");
    supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
    supplierCol.setPrefWidth(120);

    TableColumn<Product, String> dateCol = new TableColumn<>("Date Received");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("dateReceived"));
    dateCol.setPrefWidth(120);

    table
      .getColumns()
      .addAll(skuCol, nameCol, descCol, qtyCol, priceCol, supplierCol, dateCol);

    return table;
  }

  private TableView<Supplier> createSupplierTable() {
    TableView<Supplier> table = new TableView<>();

    TableColumn<Supplier, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
    idCol.setPrefWidth(100);

    TableColumn<Supplier, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(200);

    TableColumn<Supplier, String> contactCol = new TableColumn<>("Contact");
    contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
    contactCol.setPrefWidth(200);

    table.getColumns().addAll(idCol, nameCol, contactCol);

    return table;
  }

  private TableView<Order> createOrderTable() {
    TableView<Order> table = new TableView<>();

    TableColumn<Order, String> idCol = new TableColumn<>("Order ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
    idCol.setPrefWidth(120);

    TableColumn<Order, String> supplierCol = new TableColumn<>("Supplier ID");
    supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
    supplierCol.setPrefWidth(120);

    TableColumn<Order, LocalDate> dateCol = new TableColumn<>("Order Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
    dateCol.setPrefWidth(120);

    TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
    itemsCol.setCellValueFactory(cellData -> {
      List<OrderItem> items = cellData.getValue().getItems();
      return new javafx.beans.property.SimpleStringProperty(
        items.size() + " items"
      );
    });
    itemsCol.setPrefWidth(100);

    table.getColumns().addAll(idCol, supplierCol, dateCol, itemsCol);

    return table;
  }

  private TableView<SellOrder> createSellOrderTable() {
    TableView<SellOrder> table = new TableView<>();

    TableColumn<SellOrder, String> idCol = new TableColumn<>("Sell Order ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
    idCol.setPrefWidth(120);

    TableColumn<SellOrder, String> customerCol = new TableColumn<>(
      "Customer Name"
    );
    customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
    customerCol.setPrefWidth(150);

    TableColumn<SellOrder, LocalDate> dateCol = new TableColumn<>("Sell Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
    dateCol.setPrefWidth(120);

    TableColumn<SellOrder, String> itemsCol = new TableColumn<>("Items");
    itemsCol.setCellValueFactory(cellData -> {
      List<OrderItem> items = cellData.getValue().getItems();
      return new javafx.beans.property.SimpleStringProperty(
        items.size() + " items"
      );
    });
    itemsCol.setPrefWidth(100);

    table.getColumns().addAll(idCol, customerCol, dateCol, itemsCol);

    return table;
  }

  private void showAddProductDialog() {
    Dialog<Product> dialog = new Dialog<>();
    dialog.setTitle("Add New Product");
    dialog.setHeaderText("Enter product details:");

    ButtonType addButtonType = new ButtonType(
      "Add",
      ButtonBar.ButtonData.OK_DONE
    );
    dialog
      .getDialogPane()
      .getButtonTypes()
      .addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField skuField = new TextField();
    TextField nameField = new TextField();
    TextField descField = new TextField();
    TextField qtyField = new TextField();
    TextField priceField = new TextField();
    ComboBox<String> supplierCombo = new ComboBox<>();

    // Populate supplier combo
    List<Supplier> suppliers = inventoryService.getAllSuppliers();
    for (Supplier supplier : suppliers) {
      supplierCombo.getItems().add(supplier.getId());
    }

    grid.add(new Label("SKU:"), 0, 0);
    grid.add(skuField, 1, 0);
    grid.add(new Label("Name:"), 0, 1);
    grid.add(nameField, 1, 1);
    grid.add(new Label("Description:"), 0, 2);
    grid.add(descField, 1, 2);
    grid.add(new Label("Quantity:"), 0, 3);
    grid.add(qtyField, 1, 3);
    grid.add(new Label("Price:"), 0, 4);
    grid.add(priceField, 1, 4);
    grid.add(new Label("Supplier:"), 0, 5);
    grid.add(supplierCombo, 1, 5);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          String sku = skuField.getText();
          String name = nameField.getText();
          String description = descField.getText();
          int quantity = Integer.parseInt(qtyField.getText());
          double price = Double.parseDouble(priceField.getText());
          String supplierId = supplierCombo.getValue();
          String dateReceived = LocalDate.now().toString();

          if (sku.isEmpty() || name.isEmpty()) {
            showAlert("Error", "SKU and Name are required fields.");
            return null;
          }

          return new Product(
            sku,
            name,
            description,
            quantity,
            price,
            supplierId,
            dateReceived
          );
        } catch (NumberFormatException e) {
          showAlert(
            "Error",
            "Please enter valid numbers for quantity and price."
          );
          return null;
        }
      }
      return null;
    });

    Optional<Product> result = dialog.showAndWait();
    result.ifPresent(product -> {
      inventoryService.addProduct(product);
      refreshProductTable();
      refreshDashboard();
    });
  }

  private void showEditProductDialog() {
    Product selectedProduct = productTable
      .getSelectionModel()
      .getSelectedItem();
    if (selectedProduct == null) {
      showAlert("No Selection", "Please select a product to edit.");
      return;
    }

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Edit Product");
    dialog.setHeaderText("Edit product details:");

    ButtonType saveButtonType = new ButtonType(
      "Save",
      ButtonBar.ButtonData.OK_DONE
    );
    dialog
      .getDialogPane()
      .getButtonTypes()
      .addAll(saveButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField qtyField = new TextField(
      String.valueOf(selectedProduct.getQuantity())
    );
    TextField priceField = new TextField(
      String.valueOf(selectedProduct.getPrice())
    );

    grid.add(new Label("SKU:"), 0, 0);
    grid.add(new Label(selectedProduct.getSku()), 1, 0);
    grid.add(new Label("Name:"), 0, 1);
    grid.add(new Label(selectedProduct.getName()), 1, 1);
    grid.add(new Label("Quantity:"), 0, 2);
    grid.add(qtyField, 1, 2);
    grid.add(new Label("Price:"), 0, 3);
    grid.add(priceField, 1, 3);

    dialog.getDialogPane().setContent(grid);

    Optional<ButtonType> result = dialog.showAndWait();
    if (result.isPresent() && result.get() == saveButtonType) {
      try {
        int quantity = Integer.parseInt(qtyField.getText());
        double price = Double.parseDouble(priceField.getText());

        inventoryService.updateProduct(
          selectedProduct.getSku(),
          quantity,
          price
        );
        refreshProductTable();
        refreshDashboard();
      } catch (NumberFormatException e) {
        showAlert(
          "Error",
          "Please enter valid numbers for quantity and price."
        );
      }
    }
  }

  private void deleteSelectedProduct() {
    Product selectedProduct = productTable
      .getSelectionModel()
      .getSelectedItem();
    if (selectedProduct == null) {
      showAlert("No Selection", "Please select a product to delete.");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirm Delete");
    alert.setHeaderText("Delete Product");
    alert.setContentText(
      "Are you sure you want to delete product: " +
        selectedProduct.getName() +
        "?"
    );

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      inventoryService.removeProduct(selectedProduct.getSku());
      refreshProductTable();
      refreshDashboard();
    }
  }

  private void showAddSupplierDialog() {
    Dialog<Supplier> dialog = new Dialog<>();
    dialog.setTitle("Add New Supplier");
    dialog.setHeaderText("Enter supplier details:");

    ButtonType addButtonType = new ButtonType(
      "Add",
      ButtonBar.ButtonData.OK_DONE
    );
    dialog
      .getDialogPane()
      .getButtonTypes()
      .addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField idField = new TextField();
    TextField nameField = new TextField();
    TextField contactField = new TextField();

    grid.add(new Label("ID:"), 0, 0);
    grid.add(idField, 1, 0);
    grid.add(new Label("Name:"), 0, 1);
    grid.add(nameField, 1, 1);
    grid.add(new Label("Contact:"), 0, 2);
    grid.add(contactField, 1, 2);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        String id = idField.getText();
        String name = nameField.getText();
        String contact = contactField.getText();

        if (id.isEmpty() || name.isEmpty() || contact.isEmpty()) {
          showAlert("Error", "All fields are required.");
          return null;
        }

        return new Supplier(id, name, contact);
      }
      return null;
    });

    Optional<Supplier> result = dialog.showAndWait();
    result.ifPresent(supplier -> {
      inventoryService.addSupplier(supplier);
      refreshSupplierTable();
    });
  }

  private void deleteSelectedSupplier() {
    Supplier selectedSupplier = supplierTable
      .getSelectionModel()
      .getSelectedItem();
    if (selectedSupplier == null) {
      showAlert("No Selection", "Please select a supplier to delete.");
      return;
    }

    showAlert(
      "Not Implemented",
      "Supplier deletion is not implemented in the service yet."
    );
  }

  private void showCreateOrderDialog() {
    Dialog<Order> dialog = new Dialog<>();
    dialog.setTitle("Create Purchase Order");
    dialog.setHeaderText("Enter order details:");

    ButtonType createButtonType = new ButtonType(
      "Create",
      ButtonBar.ButtonData.OK_DONE
    );
    dialog
      .getDialogPane()
      .getButtonTypes()
      .addAll(createButtonType, ButtonType.CANCEL);

    VBox content = new VBox(10);
    content.setPadding(new Insets(20));

    TextField orderIdField = new TextField();
    ComboBox<String> supplierCombo = new ComboBox<>();

    // Populate supplier combo
    List<Supplier> suppliers = inventoryService.getAllSuppliers();
    for (Supplier supplier : suppliers) {
      supplierCombo.getItems().add(supplier.getId());
    }

    // Order items section
    VBox itemsSection = new VBox(5);
    Label itemsLabel = new Label("Order Items:");
    ListView<String> itemsList = new ListView<>();
    List<OrderItem> orderItems = new ArrayList<>();

    HBox addItemBox = new HBox(5);
    ComboBox<String> productCombo = new ComboBox<>();
    TextField qtyField = new TextField();
    qtyField.setPromptText("Quantity");
    Button addItemButton = new Button("Add Item");

    // Populate product combo
    List<Product> products = inventoryService.getAllProducts();
    for (Product product : products) {
      productCombo.getItems().add(product.getSku() + " - " + product.getName());
    }

    addItemButton.setOnAction(e -> {
      String selectedProduct = productCombo.getValue();
      String qtyText = qtyField.getText();

      if (selectedProduct != null && !qtyText.isEmpty()) {
        try {
          String sku = selectedProduct.split(" - ")[0];
          int quantity = Integer.parseInt(qtyText);

          OrderItem item = new OrderItem(sku, quantity);
          orderItems.add(item);
          itemsList.getItems().add(sku + " x " + quantity);

          productCombo.setValue(null);
          qtyField.clear();
        } catch (NumberFormatException ex) {
          showAlert("Error", "Please enter a valid quantity.");
        }
      }
    });

    addItemBox.getChildren().addAll(productCombo, qtyField, addItemButton);
    itemsSection.getChildren().addAll(itemsLabel, itemsList, addItemBox);

    content
      .getChildren()
      .addAll(
        new Label("Order ID:"),
        orderIdField,
        new Label("Supplier:"),
        supplierCombo,
        itemsSection
      );

    dialog.getDialogPane().setContent(content);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == createButtonType) {
        String orderId = orderIdField.getText();
        String supplierId = supplierCombo.getValue();

        if (orderId.isEmpty() || supplierId == null || orderItems.isEmpty()) {
          showAlert(
            "Error",
            "Please fill all fields and add at least one item."
          );
          return null;
        }

        return new Order(orderId, supplierId, orderItems, LocalDate.now());
      }
      return null;
    });

    Optional<Order> result = dialog.showAndWait();
    result.ifPresent(order -> {
      inventoryService.addOrder(order);
      refreshOrderTable();
      refreshProductTable();
      refreshDashboard();
    });
  }

  private void showCreateSellOrderDialog() {
    Dialog<SellOrder> dialog = new Dialog<>();
    dialog.setTitle("Create Sell Order");
    dialog.setHeaderText("Enter sell order details:");

    ButtonType createButtonType = new ButtonType(
      "Create",
      ButtonBar.ButtonData.OK_DONE
    );
    dialog
      .getDialogPane()
      .getButtonTypes()
      .addAll(createButtonType, ButtonType.CANCEL);

    VBox content = new VBox(10);
    content.setPadding(new Insets(20));

    TextField orderIdField = new TextField();
    TextField customerField = new TextField();

    // Order items section
    VBox itemsSection = new VBox(5);
    Label itemsLabel = new Label("Sell Items:");
    ListView<String> itemsList = new ListView<>();
    List<OrderItem> orderItems = new ArrayList<>();

    HBox addItemBox = new HBox(5);
    ComboBox<String> productCombo = new ComboBox<>();
    TextField qtyField = new TextField();
    qtyField.setPromptText("Quantity");
    Button addItemButton = new Button("Add Item");

    // Populate product combo
    List<Product> products = inventoryService.getAllProducts();
    for (Product product : products) {
      productCombo
        .getItems()
        .add(
          product.getSku() +
            " - " +
            product.getName() +
            " (Available: " +
            product.getQuantity() +
            ")"
        );
    }

    addItemButton.setOnAction(e -> {
      String selectedProduct = productCombo.getValue();
      String qtyText = qtyField.getText();

      if (selectedProduct != null && !qtyText.isEmpty()) {
        try {
          String sku = selectedProduct.split(" - ")[0];
          int quantity = Integer.parseInt(qtyText);

          // Check if enough stock is available
          Optional<Product> product = inventoryService.findProductBySku(sku);
          if (product.isPresent() && product.get().getQuantity() >= quantity) {
            OrderItem item = new OrderItem(sku, quantity);
            orderItems.add(item);
            itemsList.getItems().add(sku + " x " + quantity);

            productCombo.setValue(null);
            qtyField.clear();
          } else {
            showAlert("Error", "Not enough stock available for this product.");
          }
        } catch (NumberFormatException ex) {
          showAlert("Error", "Please enter a valid quantity.");
        }
      }
    });

    addItemBox.getChildren().addAll(productCombo, qtyField, addItemButton);
    itemsSection.getChildren().addAll(itemsLabel, itemsList, addItemBox);

    content
      .getChildren()
      .addAll(
        new Label("Sell Order ID:"),
        orderIdField,
        new Label("Customer Name:"),
        customerField,
        itemsSection
      );

    dialog.getDialogPane().setContent(content);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == createButtonType) {
        String orderId = orderIdField.getText();
        String customerName = customerField.getText();

        if (
          orderId.isEmpty() || customerName.isEmpty() || orderItems.isEmpty()
        ) {
          showAlert(
            "Error",
            "Please fill all fields and add at least one item."
          );
          return null;
        }

        return new SellOrder(
          orderId,
          customerName,
          orderItems,
          LocalDate.now()
        );
      }
      return null;
    });

    Optional<SellOrder> result = dialog.showAndWait();
    result.ifPresent(sellOrder -> {
      inventoryService.addSellOrder(sellOrder);
      refreshSellOrderTable();
      refreshProductTable();
      refreshDashboard();
    });
  }

  private void searchProducts(String searchTerm) {
    if (searchTerm.isEmpty()) {
      refreshProductTable();
    } else {
      List<Product> searchResults = inventoryService.searchProducts(searchTerm);
      productData.clear();
      productData.addAll(searchResults);
    }
  }

  private void refreshAllData() {
    refreshProductTable();
    refreshSupplierTable();
    refreshOrderTable();
    refreshSellOrderTable();
    refreshDashboard();
  }

  private void refreshProductTable() {
    productData.clear();
    productData.addAll(inventoryService.getAllProducts());
  }

  private void refreshSupplierTable() {
    supplierData.clear();
    supplierData.addAll(inventoryService.getAllSuppliers());
  }

  private void refreshOrderTable() {
    orderData.clear();
    orderData.addAll(inventoryService.getAllOrders());
  }

  private void refreshSellOrderTable() {
    sellOrderData.clear();
    sellOrderData.addAll(inventoryService.getAllSellOrders());
  }

  private void refreshDashboard() {
    totalProductsLabel.setText(
      String.valueOf(inventoryService.getTotalProducts())
    );
    totalQuantityLabel.setText(
      String.valueOf(inventoryService.getTotalQuantity())
    );
    totalValueLabel.setText(
      String.format("$%.2f", inventoryService.getTotalValue())
    );
    lowStockLabel.setText(
      String.valueOf(inventoryService.getLowStockCount(10))
    );
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
