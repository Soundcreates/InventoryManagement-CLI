# Inventory Management System

A comprehensive inventory management system built with Java, featuring both Command Line Interface (CLI) and Graphical User Interface (GUI) versions.

## 🎯 Features

- **Product Management**: Add, edit, delete, and search products
- **Supplier Management**: Manage supplier information
- **Purchase Orders**: Create and track purchase orders
- **Sell Orders**: Process sales and automatically update inventory
- **Inventory Reports**: Generate comprehensive inventory reports
- **MongoDB Integration**: Persistent data storage
- **Dual Interface**: Choose between CLI and modern GUI

## 📋 Prerequisites

- **Java 17 or higher**
- **Maven 3.6 or higher**
- **MongoDB** (running on localhost:27017)

## 🛠️ Setup

### 1. Clone and Navigate
```bash
cd InventoryManagementCLI
```

### 2. Start MongoDB
```bash
mongod --dbpath ./mongo-data/db/
```

### 3. Compile the Project
```bash
mvn compile
```

## 🚀 Running the Application

### GUI Version (Recommended)

**Option 1: Using the launcher script**
- Windows: Double-click `run-gui.bat`
- Linux/Mac: `./run-gui.sh`

**Option 2: Using Maven**
```bash
mvn exec:java -Pgui
```

### CLI Version

```bash
mvn exec:java -Dexec.mainClass="com.store.inventory.App"
```

## 🖥️ GUI Features

The GUI version provides a modern, user-friendly interface with:

- **Dashboard**: Overview with key statistics and metrics
- **Tabbed Interface**: Easy navigation between different sections
- **Product Management**: Visual forms for adding/editing products
- **Search Functionality**: Real-time product search
- **Order Management**: Interactive order creation
- **Professional Styling**: Clean, modern design

### GUI Usage Guide

1. **Dashboard Tab**: View inventory statistics and recent products
2. **Products Tab**: Manage your product inventory
3. **Suppliers Tab**: Add and manage suppliers
4. **Purchase Orders Tab**: Create orders from suppliers
5. **Sell Orders Tab**: Process customer sales

## 📱 CLI Features

The CLI version offers:

- Menu-driven interface
- Color-coded output
- Comprehensive error handling
- All core functionality available

### CLI Menu Options

1. Add Product
2. View Inventory
3. Update Product
4. Remove Product
5. Search Product
6. Generate Inventory Report
7. Add Supplier
8. View Suppliers
9. Create Order
10. View Orders
11. Create Sell Order
12. Exit

## 💾 Database

The application uses MongoDB with the following collections:
- `products`: Product inventory data
- `suppliers`: Supplier information
- `orders`: Purchase orders
- `sell_orders`: Sales orders

Database: `inventory_db`
Connection: `mongodb://localhost:27017`

## 🔧 Configuration

### MongoDB Connection
To change the database connection, edit:
```
src/main/java/com/store/inventory/service/InventoryService.java
```

### GUI Styling
Customize the GUI appearance by editing:
```
src/main/resources/styles.css
```

## 📂 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/store/inventory/
│   │       ├── gui/              # GUI application
│   │       ├── service/          # Business logic
│   │       ├── App.java          # CLI application
│   │       ├── Product.java
│   │       ├── Supplier.java
│   │       ├── Order.java
│   │       ├── OrderItem.java
│   │       └── SellOrder.java
│   └── resources/
│       └── styles.css            # GUI styling
├── run-gui.bat                   # Windows launcher
├── run-gui.sh                    # Unix launcher
└── GUI_README.md                 # Detailed GUI guide
```

## 🐛 Troubleshooting

### Common Issues

**MongoDB Connection Failed:**
- Ensure MongoDB is running on port 27017
- Check if MongoDB service is started

**JavaFX Issues (GUI):**
- Use Java 17 or higher
- The "Unsupported JavaFX configuration" warning is normal and can be ignored

**Build Issues:**
- Run `mvn clean install` to refresh dependencies
- Ensure Java and Maven are properly installed

### Performance Tips

- Use search functionality for large inventories
- Close application properly to ensure database connections are closed
- Both CLI and GUI share the same database

## 📝 Sample Workflow

1. **Start MongoDB**: `mongod --dbpath ./mongo-data/db/`
2. **Add Suppliers**: Create supplier records first
3. **Add Products**: Add products with supplier references
4. **Create Purchase Orders**: Order inventory from suppliers
5. **Process Sales**: Create sell orders for customers
6. **Monitor Dashboard**: Track inventory levels and statistics

## 🎉 Getting Started

1. Choose your preferred interface (GUI recommended for new users)
2. Add a few suppliers first
3. Add some products to your inventory
4. Try creating a purchase order
5. Process a sell order to see inventory updates
6. Explore the dashboard/reports

## 📞 Support

- Check the `GUI_README.md` for detailed GUI instructions
- Review troubleshooting section for common issues
- Ensure all prerequisites are properly installed

**Enjoy managing your inventory! 🚀📦**


mvn exec:java "-Dexec.mainClass=com.store.inventory.App"
