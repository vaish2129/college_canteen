package com.collegecanteen.dao;

import com.collegecanteen.util.DBUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public void initialize() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(100) UNIQUE NOT NULL, "
                + "password VARCHAR(255) NOT NULL, "
                + "full_name VARCHAR(200) NOT NULL, "
                + "email VARCHAR(200), "
                + "phone VARCHAR(25), "
                + "role VARCHAR(20) NOT NULL DEFAULT 'STUDENT'"
                + ");"
                + "CREATE TABLE IF NOT EXISTS students ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "user_id INT NOT NULL UNIQUE, "
                + "department VARCHAR(100), "
                + "year_level VARCHAR(50), "
                + "student_id VARCHAR(100), "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ");"
                + "CREATE TABLE IF NOT EXISTS admins ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "user_id INT NOT NULL UNIQUE, "
                + "designation VARCHAR(100), "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ");"
                + "CREATE TABLE IF NOT EXISTS food_items ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(200) NOT NULL, "
                + "category VARCHAR(100) NOT NULL, "
                + "price DECIMAL(10,2) NOT NULL, "
                + "stock_quantity INT NOT NULL DEFAULT 0, "
                + "image_url VARCHAR(255), "
                + "description TEXT, "
                + "preparation_time_minutes INT DEFAULT 10, "
                + "available BOOLEAN DEFAULT TRUE"
                + ");"
                + "CREATE TABLE IF NOT EXISTS orders ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "order_id VARCHAR(50) UNIQUE NOT NULL, "
                + "student_id INT NOT NULL, "
                + "student_name VARCHAR(200) NOT NULL, "
                + "status VARCHAR(30) NOT NULL DEFAULT 'PENDING', "
                + "payment_method VARCHAR(30), "
                + "payment_status VARCHAR(30) DEFAULT 'PENDING', "
                + "total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (student_id) REFERENCES users(id)" 
                + ");"
                + "CREATE TABLE IF NOT EXISTS order_items ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "order_id INT NOT NULL, "
                + "food_id INT NOT NULL, "
                + "food_name VARCHAR(200) NOT NULL, "
                + "quantity INT NOT NULL, "
                + "price DECIMAL(10,2) NOT NULL, "
                + "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE"
                + ");"
                + "CREATE TABLE IF NOT EXISTS payments ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "order_id INT NOT NULL, "
                + "payment_method VARCHAR(30), "
                + "status VARCHAR(30) DEFAULT 'PENDING', "
                + "amount DECIMAL(10,2) NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE"
                + ");"
                + "CREATE TABLE IF NOT EXISTS inventory_items ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "food_id INT NOT NULL UNIQUE, "
                + "food_name VARCHAR(200), "
                + "stock_quantity INT NOT NULL DEFAULT 0, "
                + "minimum_threshold INT NOT NULL DEFAULT 5, "
                + "low_stock_alert BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (food_id) REFERENCES food_items(id) ON DELETE CASCADE"
                + ");"
                + "INSERT INTO users (username, password, full_name, email, phone, role) VALUES ('admin', 'admin123', 'Canteen Administrator', 'admin@college.edu', '9876543210', 'ADMIN') ON DUPLICATE KEY UPDATE username = VALUES(username);"
                + "INSERT IGNORE INTO admins (user_id, designation) SELECT id, 'Head Canteen Supervisor' FROM users WHERE username = 'admin';";

        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) stmt.execute(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
