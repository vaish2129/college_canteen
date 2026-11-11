CREATE DATABASE IF NOT EXISTS college_canteen_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE college_canteen_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(200) NOT NULL,
  email VARCHAR(200),
  phone VARCHAR(25),
  role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_user_role CHECK (role IN ('STUDENT','ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  department VARCHAR(100),
  year_level VARCHAR(50),
  student_id VARCHAR(100),
  CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admins (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL UNIQUE,
  designation VARCHAR(100),
  CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS food_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL UNIQUE,
  category VARCHAR(100) NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  stock_quantity INT NOT NULL DEFAULT 0,
  image_url VARCHAR(1000),
  description VARCHAR(1000),
  preparation_time_minutes INT NOT NULL DEFAULT 10,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_food_available (available, name),
  CONSTRAINT chk_food_price CHECK (price >= 0),
  CONSTRAINT chk_food_stock CHECK (stock_quantity >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id VARCHAR(50) NOT NULL UNIQUE,
  student_id INT NOT NULL,
  student_name VARCHAR(200) NOT NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  payment_method VARCHAR(30),
  payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_orders_student_date (student_id, created_at),
  INDEX idx_orders_status_date (status, created_at),
  CONSTRAINT fk_order_student FOREIGN KEY (student_id) REFERENCES users(id),
  CONSTRAINT chk_order_total CHECK (total_amount >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  food_id INT NOT NULL,
  food_name VARCHAR(200) NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10,2) NOT NULL,
  INDEX idx_order_items_order (order_id),
  INDEX idx_order_items_food (food_id),
  UNIQUE KEY uq_order_food (order_id, food_id),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
  CONSTRAINT chk_order_item_price CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  payment_method VARCHAR(30),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  amount DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_payment_order (order_id),
  CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  food_id INT NOT NULL UNIQUE,
  food_name VARCHAR(200) NOT NULL,
  stock_quantity INT NOT NULL DEFAULT 0,
  minimum_threshold INT NOT NULL DEFAULT 5,
  low_stock_alert BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_inventory_food FOREIGN KEY (food_id) REFERENCES food_items(id) ON DELETE CASCADE,
  CONSTRAINT chk_inventory_stock CHECK (stock_quantity >= 0),
  CONSTRAINT chk_inventory_threshold CHECK (minimum_threshold >= 0)
) ENGINE=InnoDB;
