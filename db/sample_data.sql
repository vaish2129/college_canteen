USE college_canteen_db;

-- Demo credentials are intentionally simple for a local classroom demonstration.
-- The first successful login upgrades these legacy seed passwords to PBKDF2 hashes.
INSERT IGNORE INTO users (username, password, full_name, email, phone, role) VALUES
('admin', 'admin123', 'Canteen Administrator', 'admin@college.edu', '9876543210', 'ADMIN'),
('student', 'student123', 'Aarav Sharma', 'aarav@college.edu', '9876501234', 'STUDENT');

INSERT IGNORE INTO admins (user_id, designation)
SELECT u.id, 'Head Canteen Supervisor' FROM users u WHERE u.username = 'admin';

INSERT IGNORE INTO students (user_id, department, year_level, student_id)
SELECT u.id, 'Computer Science', '2nd year', 'CSE2026001' FROM users u WHERE u.username = 'student';

INSERT IGNORE INTO food_items (name, category, price, stock_quantity, image_url, description, preparation_time_minutes, available) VALUES
('Masala Dosa', 'Breakfast', 65.00, 24, NULL, 'Crisp dosa with spiced potato filling and house chutneys.', 12, TRUE),
('Paneer Wrap', 'Lunch', 90.00, 18, NULL, 'Grilled paneer, crunchy vegetables, and mint yogurt.', 10, TRUE),
('Veg Fried Rice', 'Lunch', 80.00, 16, NULL, 'Wok-tossed rice with seasonal vegetables.', 14, TRUE),
('Samosa (2 pcs)', 'Snacks', 30.00, 32, NULL, 'Golden pastry filled with potato and warming spices.', 5, TRUE),
('Fresh Lime Soda', 'Drinks', 35.00, 25, NULL, 'Fresh lime, sparkling soda, and a pinch of salt.', 3, TRUE),
('Fruit Bowl', 'Healthy', 55.00, 3, NULL, 'A colorful bowl of seasonal cut fruit.', 4, TRUE),
('Filter Coffee', 'Drinks', 25.00, 40, NULL, 'South Indian filter coffee, freshly brewed.', 4, TRUE),
('Chocolate Muffin', 'Snacks', 45.00, 0, NULL, 'Soft chocolate muffin baked fresh this morning.', 2, FALSE);

INSERT INTO inventory_items (food_id, food_name, stock_quantity, minimum_threshold, low_stock_alert)
SELECT f.id, f.name, f.stock_quantity, 5, f.stock_quantity < 5 FROM food_items f
ON DUPLICATE KEY UPDATE food_name = VALUES(food_name), stock_quantity = VALUES(stock_quantity), low_stock_alert = VALUES(low_stock_alert);

-- A sample completed sale gives the analytics page a useful first-run example.
INSERT IGNORE INTO orders (order_id, student_id, student_name, status, payment_method, payment_status, total_amount, created_at)
SELECT 'ORD-DEMO-0001', u.id, u.full_name, 'COMPLETED', 'CASH', 'PAID', 65.00, CURRENT_TIMESTAMP - INTERVAL 1 HOUR
FROM users u WHERE u.username = 'student';

INSERT IGNORE INTO order_items (order_id, food_id, food_name, quantity, price)
SELECT o.id, f.id, f.name, 1, f.price FROM orders o
JOIN users u ON u.username = 'student' JOIN food_items f ON f.name = 'Masala Dosa'
WHERE o.order_id = 'ORD-DEMO-0001';

INSERT IGNORE INTO payments (order_id, payment_method, status, amount)
SELECT o.id, 'CASH', 'PAID', o.total_amount FROM orders o
WHERE o.order_id = 'ORD-DEMO-0001';