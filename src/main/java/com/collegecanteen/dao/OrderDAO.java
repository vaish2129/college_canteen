package com.collegecanteen.dao;

import com.collegecanteen.model.Cart;
import com.collegecanteen.model.CartItem;
import com.collegecanteen.model.Order;
import com.collegecanteen.model.OrderItem;
import com.collegecanteen.model.User;
import com.collegecanteen.util.DBUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderDAO {
    public String generateOrderId() { return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }

    public int placeOrder(User student, Cart cart, String paymentMethod) {
        if (student == null || cart == null || cart.isEmpty()) throw new IllegalArgumentException("Cart is empty or student missing");
        if (!"CASH".equals(paymentMethod) && !"DEMO_ONLINE".equals(paymentMethod)) throw new IllegalArgumentException("Invalid payment method");
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                List<OrderItem> items = new ArrayList<>();
                String foodSql = "SELECT name, price, stock_quantity, available FROM food_items WHERE id = ? FOR UPDATE";
                try (PreparedStatement food = conn.prepareStatement(foodSql)) {
                    for (CartItem cartItem : cart.getItems()) {
                        if (cartItem.getQuantity() < 1) throw new IllegalArgumentException("Invalid item quantity");
                        food.setInt(1, cartItem.getFoodId());
                        try (ResultSet rs = food.executeQuery()) {
                            if (!rs.next() || !rs.getBoolean("available") || rs.getInt("stock_quantity") < cartItem.getQuantity())
                                throw new IllegalArgumentException("Not enough stock for " + cartItem.getFoodName());
                            BigDecimal price = rs.getBigDecimal("price"); total = total.add(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                            OrderItem item = new OrderItem(); item.setFoodId(cartItem.getFoodId()); item.setFoodName(rs.getString("name")); item.setQuantity(cartItem.getQuantity()); item.setPrice(price); items.add(item);
                        }
                    }
                }
                String orderSql = "INSERT INTO orders (order_id, student_id, student_name, status, payment_method, payment_status, total_amount) VALUES (?, ?, ?, 'PENDING', ?, 'PENDING', ?)";
                int orderPrimaryId;
                try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, generateOrderId()); ps.setInt(2, student.getId()); ps.setString(3, student.getFullName()); ps.setString(4, paymentMethod); ps.setBigDecimal(5, total); ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) { if (!keys.next()) throw new SQLException("Order ID not generated"); orderPrimaryId = keys.getInt(1); }
                }
                String itemSql = "INSERT INTO order_items (order_id, food_id, food_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
                String stockSql = "UPDATE food_items SET stock_quantity = stock_quantity - ?, available = (stock_quantity > 0) WHERE id = ? AND stock_quantity >= ?";
                try (PreparedStatement item = conn.prepareStatement(itemSql); PreparedStatement stock = conn.prepareStatement(stockSql)) {
                    for (OrderItem oi : items) {
                        item.setInt(1, orderPrimaryId); item.setInt(2, oi.getFoodId()); item.setString(3, oi.getFoodName()); item.setInt(4, oi.getQuantity()); item.setBigDecimal(5, oi.getPrice()); item.addBatch();
                        stock.setInt(1, oi.getQuantity()); stock.setInt(2, oi.getFoodId()); stock.setInt(3, oi.getQuantity());
                        if (stock.executeUpdate() != 1) throw new IllegalArgumentException("Stock changed; please refresh your cart.");
                    }
                    item.executeBatch();
                }
                try (PreparedStatement payment = conn.prepareStatement("INSERT INTO payments (order_id, payment_method, status, amount) VALUES (?, ?, 'PENDING', ?)")) {
                    payment.setInt(1, orderPrimaryId); payment.setString(2, paymentMethod); payment.setBigDecimal(3, total); payment.executeUpdate();
                }
                try (PreparedStatement inventory = conn.prepareStatement("UPDATE inventory_items i JOIN food_items f ON f.id = i.food_id SET i.food_name = f.name, i.stock_quantity = f.stock_quantity, i.low_stock_alert = (f.stock_quantity < i.minimum_threshold) WHERE i.food_id = ?")) {
                    for (OrderItem oi : items) { inventory.setInt(1, oi.getFoodId()); inventory.addBatch(); }
                    inventory.executeBatch();
                }
                conn.commit(); return orderPrimaryId;
            } catch (SQLException | RuntimeException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { throw new RuntimeException("Unable to place order", e); }
    }

    public List<Order> getOrdersByStudent(int studentId) { return queryOrders("SELECT * FROM orders WHERE student_id = ? ORDER BY created_at DESC", studentId); }
    public List<Order> getAllOrders() { return queryOrders("SELECT * FROM orders ORDER BY created_at DESC", null); }
    private List<Order> queryOrders(String sql, Integer studentId) {
        List<Order> result = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (studentId != null) ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(mapOrder(rs, conn)); }
        } catch (SQLException e) { throw new RuntimeException("Error retrieving orders", e); }
        return result;
    }
    public Order findById(int id) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE id = ?")) {
            ps.setInt(1, id); try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapOrder(rs, conn) : null; }
        } catch (SQLException e) { throw new RuntimeException("Error retrieving order", e); }
    }
    public boolean updateOrderStatus(int id, String status) {
        if ("CANCELLED".equals(status)) return cancelOrder(id);
        if (!List.of("PENDING", "ACCEPTED", "PREPARING", "READY", "COMPLETED").contains(status)) return false;
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affected;
                try (PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ?, payment_status = CASE WHEN ? = 'COMPLETED' AND payment_method = 'CASH' THEN 'PAID' ELSE payment_status END WHERE id = ? AND status <> 'CANCELLED'")) {
                    ps.setString(1, status); ps.setString(2, status); ps.setInt(3, id); affected = ps.executeUpdate();
                }
                if (affected == 0) { conn.rollback(); return false; }
                if ("COMPLETED".equals(status)) {
                    try (PreparedStatement payment = conn.prepareStatement("UPDATE payments p JOIN orders o ON o.id = p.order_id SET p.status = 'PAID' WHERE o.id = ? AND o.payment_method = 'CASH'")) {
                        payment.setInt(1, id); payment.executeUpdate();
                    }
                }
                conn.commit(); return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { throw new RuntimeException("Error updating order status", e); }
    }
    public boolean cancelOrder(int id) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement update = conn.prepareStatement("UPDATE orders SET status = 'CANCELLED' WHERE id = ? AND status IN ('PENDING','ACCEPTED')")) {
                    update.setInt(1, id); if (update.executeUpdate() == 0) { conn.rollback(); return false; }
                }
                String restore = "UPDATE food_items f JOIN order_items oi ON oi.food_id = f.id SET f.stock_quantity = f.stock_quantity + oi.quantity, f.available = TRUE WHERE oi.order_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(restore)) { ps.setInt(1, id); ps.executeUpdate(); }
                try (PreparedStatement inventory = conn.prepareStatement("UPDATE inventory_items i JOIN food_items f ON f.id = i.food_id JOIN order_items oi ON oi.food_id = f.id SET i.stock_quantity = f.stock_quantity, i.low_stock_alert = (f.stock_quantity < i.minimum_threshold) WHERE oi.order_id = ?")) { inventory.setInt(1, id); inventory.executeUpdate(); }
                conn.commit(); return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) { throw new RuntimeException("Error cancelling order", e); }
    }

    public Map<String, Object> getSalesAnalytics() {
        Map<String, Object> result = new HashMap<>();
        String summary = "SELECT COUNT(*) total_orders, COALESCE(SUM(total_amount),0) total_revenue, COALESCE(AVG(total_amount),0) avg_order_value FROM orders WHERE status = 'COMPLETED'";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(summary); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) { result.put("totalOrders", rs.getInt("total_orders")); result.put("totalRevenue", rs.getBigDecimal("total_revenue")); result.put("averageOrderValue", rs.getBigDecimal("avg_order_value")); }
            List<Map<String,Object>> popular = new ArrayList<>();
            try (PreparedStatement p = conn.prepareStatement("SELECT oi.food_name, SUM(oi.quantity) sold_count FROM order_items oi JOIN orders o ON o.id=oi.order_id WHERE o.status='COMPLETED' GROUP BY oi.food_name ORDER BY sold_count DESC LIMIT 5"); ResultSet r = p.executeQuery()) {
                while (r.next()) { Map<String,Object> item = new HashMap<>(); item.put("name",r.getString("food_name")); item.put("count",r.getInt("sold_count")); popular.add(item); }
            }
            result.put("popularItems", popular);
            List<Map<String,Object>> daily = new ArrayList<>();
            try (PreparedStatement p = conn.prepareStatement("SELECT DATE(created_at) sale_date, SUM(total_amount) revenue FROM orders WHERE status='COMPLETED' AND created_at >= CURRENT_DATE - INTERVAL 6 DAY GROUP BY DATE(created_at) ORDER BY sale_date"); ResultSet r = p.executeQuery()) {
                while (r.next()) { Map<String,Object> day = new HashMap<>(); day.put("date",r.getDate("sale_date").toString()); day.put("revenue",r.getBigDecimal("revenue")); daily.add(day); }
            }
            result.put("dailySales", daily);
        } catch (SQLException e) { throw new RuntimeException("Error calculating analytics", e); }
        return result;
    }
    public int getEstimatedPreparationTime(int studentId) {
        try (Connection conn=DBUtil.getConnection(); PreparedStatement ps=conn.prepareStatement("SELECT COALESCE(MAX(fi.preparation_time_minutes),0) pending_minutes FROM orders o JOIN order_items oi ON oi.order_id=o.id JOIN food_items fi ON fi.id=oi.food_id WHERE o.student_id=? AND o.status IN ('PENDING','ACCEPTED','PREPARING')")) {
            ps.setInt(1,studentId); try(ResultSet rs=ps.executeQuery()){return rs.next()?rs.getInt("pending_minutes"):0;}
        } catch(SQLException e){throw new RuntimeException("Error calculating preparation time",e);}
    }
    public List<Map<String,Object>> getLowStockItems() {
        List<Map<String,Object>> list=new ArrayList<>();
        String sql="SELECT food_id, food_name, stock_quantity, minimum_threshold FROM inventory_items WHERE stock_quantity < minimum_threshold OR low_stock_alert=TRUE ORDER BY stock_quantity";
        try(Connection conn=DBUtil.getConnection();PreparedStatement ps=conn.prepareStatement(sql);ResultSet rs=ps.executeQuery()){
            while(rs.next()){Map<String,Object> item=new HashMap<>();item.put("foodId",rs.getInt("food_id"));item.put("foodName",rs.getString("food_name"));item.put("stockQuantity",rs.getInt("stock_quantity"));item.put("threshold",rs.getInt("minimum_threshold"));list.add(item);}
        }catch(SQLException e){throw new RuntimeException("Error loading low stock items",e);} return list;
    }
    private Order mapOrder(ResultSet rs, Connection conn) throws SQLException {
        Order order=new Order(); order.setId(rs.getInt("id")); order.setOrderId(rs.getString("order_id")); order.setStudentId(rs.getInt("student_id")); order.setStudentName(rs.getString("student_name")); order.setStatus(rs.getString("status")); order.setPaymentMethod(rs.getString("payment_method")); order.setPaymentStatus(rs.getString("payment_status")); order.setTotalAmount(rs.getBigDecimal("total_amount"));
        Timestamp ts=rs.getTimestamp("created_at"); if(ts!=null) order.setCreatedAt(ts.toLocalDateTime());
        List<OrderItem> items=new ArrayList<>();
        try(PreparedStatement ps=conn.prepareStatement("SELECT id,order_id,food_id,food_name,quantity,price FROM order_items WHERE order_id=?")){ps.setInt(1,order.getId());try(ResultSet ir=ps.executeQuery()){while(ir.next()){OrderItem item=new OrderItem();item.setId(ir.getInt("id"));item.setOrderId(ir.getInt("order_id"));item.setFoodId(ir.getInt("food_id"));item.setFoodName(ir.getString("food_name"));item.setQuantity(ir.getInt("quantity"));item.setPrice(ir.getBigDecimal("price"));items.add(item);}}}
        order.setItems(items); return order;
    }
}


