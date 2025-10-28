#!/bin/bash

echo "Starting Inventory Management System GUI..."
echo ""
echo "Please ensure MongoDB is running on localhost:27017"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

# Run the GUI
mvn exec:java -Pgui

echo ""
echo "GUI application has closed."
