@echo off
echo Starting Inventory Management System GUI...
echo.
echo Please ensure MongoDB is running on localhost:27017
echo.
mvn exec:java -Pgui
pause
