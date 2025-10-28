# Inventory Management System - GUI Version

## 🎉 Welcome to Your Awesome GUI Inventory Management System!

This is a JavaFX-based graphical user interface for the Inventory Management System. It provides a modern, user-friendly interface to manage your inventory, suppliers, and orders.

## 🚀 Features

- **Dashboard**: Overview of your inventory with key statistics
- **Product Management**: Add, edit, delete, and search products
- **Supplier Management**: Manage your suppliers
- **Purchase Orders**: Create and track purchase orders
- **Sell Orders**: Process sales and automatically update inventory
- **Real-time Updates**: All data is synchronized with MongoDB
- **Modern UI**: Clean, intuitive interface with professional styling

## 📋 Prerequisites

1. **Java 17 or higher**
2. **Maven 3.6 or higher**
3. **MongoDB** (running on localhost:27017)
4. **JavaFX** (included in dependencies)

## 🛠️ Installation & Setup

### 1. Ensure MongoDB is Running
```bash
# Start MongoDB service
mongod
```

### 2. Navigate to Project Directory
```bash
cd InventoryManagementCLI
```

### 3. Install Dependencies
```bash
mvn clean install
```

## ▶️ Running the GUI Application

### Method 1: Using Maven JavaFX Plugin (Recommended)
```bash
mvn javafx:run
```

### Method 2: Using Maven Exec Plugin
```bash
mvn exec:java -Dexec.mainClass="com.store.inventory.gui.InventoryGUI"
```

### Method 3: Compile and Run Manually
```bash
# Compile
mvn compile

# Run with module path (if you have JavaFX installed separately)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes com.store.inventory.gui.InventoryGUI
```

## 🎯 How to Use

### Dashboard Tab
- View key inventory statistics
- Total products, quantities, and values
- Low stock alerts (items with quantity < 10)
- Quick overview of recent products

### Products Tab
- **Add Product**: Click "Add Product" and fill in the details
- **Edit Product**: Select a product and click "Edit Product"
- **Delete Product**: Select a product and click "Delete Product"
- **Search**: Use the search box to find products by name, SKU, or description

### Suppliers Tab
- **Add Supplier**: Click "Add Supplier" and enter supplier information
- **View Suppliers**: Browse all registered suppliers
- **Delete Supplier**: Select and delete suppliers (coming soon)

### Purchase Orders Tab
- **Create Order**: Click "Create Order" to make a new purchase order
- Add multiple items to each order
- Orders automatically update inventory when created

### Sell Orders Tab
- **Create Sell Order**: Click "Create Sell Order" to process a sale
- System checks stock availability
- Inventory is automatically updated when items are sold

## 🎨 UI Features

- **Tabbed Interface**: Easy navigation between different sections
- **Modal Dialogs**: Clean forms for adding/editing data
- **Data Tables**: Sortable columns with proper formatting
- **Real-time Updates**: All changes reflect immediately
- **Professional Styling**: Modern CSS-based design

## 🔧 Configuration

### Database Connection
The application connects to MongoDB at `mongodb://localhost:27017` by default.
To change this, modify the connection string in:
```
src/main/java/com/store/inventory/service/InventoryService.java
```

### Styling
The GUI uses a custom CSS file located at:
```
src/main/resources/styles.css
```

## 📊 Data Flow

1. **Products**: Stored in `inventory_db.products` collection
2. **Suppliers**: Stored in `inventory_db.suppliers` collection
3. **Orders**: Stored in `inventory_db.orders` collection
4. **Sell Orders**: Stored in `inventory_db.sell_orders` collection

## 🐛 Troubleshooting

### Common Issues

**"CSS file not found" message:**
- This is normal and won't affect functionality
- The app will use default JavaFX styling

**MongoDB connection failed:**
- Ensure MongoDB is running on localhost:27017
- Check if the database service is started

**JavaFX modules not found:**
- Make sure you're using Java 17 or higher
- Use the Maven JavaFX plugin (Method 1) which handles modules automatically

**Application won't start:**
- Verify all dependencies are installed: `mvn clean install`
- Check Java version: `java --version`
- Ensure JavaFX dependencies are properly downloaded

### Performance Tips

- The application loads all data at startup
- Use the search functionality for large inventories
- Refresh buttons update data from the database
- Close the application properly to ensure database connections are closed

## 🔄 Switching Between CLI and GUI

You can still run the original CLI version:
```bash
mvn exec:java -Dexec.mainClass="com.store.inventory.App"
```

Both versions use the same database, so your data is shared between them!

## 🎉 Enjoy Your New GUI!

You now have a fully functional, modern inventory management system with a graphical interface. The GUI provides all the functionality of the CLI version with a much more user-friendly experience.

**Happy Inventory Managing! 🚀📦**

---

*Need help? The GUI is intuitive, but if you run into issues, check the troubleshooting section above or review the code in the `gui` package.*