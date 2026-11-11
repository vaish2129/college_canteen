package com.collegecanteen.dao;

import com.collegecanteen.model.FoodItem;
import com.collegecanteen.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {
    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items ORDER BY available DESC, name ASC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading menu", e);
        }
        return items;
    }

    public List<FoodItem> getAvailableFoodItems() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE available = true AND stock_quantity > 0 ORDER BY name ASC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading available menu", e);
        }
        return items;
    }

    public FoodItem findById(int id) {
        String sql = "SELECT * FROM food_items WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding food item", e);
        }
        return null;
    }

    public boolean addFoodItem(FoodItem item) {
        String sql = "INSERT INTO food_items (name, category, price, stock_quantity, image_url, description, preparation_time_minutes, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setInt(4, item.getStockQuantity());
            ps.setString(5, item.getImageUrl());
            ps.setString(6, item.getDescription());
            ps.setInt(7, item.getPreparationTimeMinutes());
            ps.setBoolean(8, item.isAvailable());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int foodId = generatedKeys.getInt(1);
                    ensureInventoryRecord(foodId, item.getName(), item.getStockQuantity());
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding food item", e);
        }
        return false;
    }

    public boolean updateFoodItem(FoodItem item) {
        String sql = "UPDATE food_items SET name = ?, category = ?, price = ?, stock_quantity = ?, image_url = ?, description = ?, preparation_time_minutes = ?, available = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setBigDecimal(3, item.getPrice());
            ps.setInt(4, item.getStockQuantity());
            ps.setString(5, item.getImageUrl());
            ps.setString(6, item.getDescription());
            ps.setInt(7, item.getPreparationTimeMinutes());
            ps.setBoolean(8, item.isAvailable());
            ps.setInt(9, item.getId());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                syncInventory(item.getId(), item.getName(), item.getStockQuantity());
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating food item", e);
        }
        return false;
    }

    public boolean deleteFoodItem(int id) {
        String sql = "DELETE FROM food_items WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting food item", e);
        }
    }

    public void updateStock(int foodId, int newQuantity) {
        String sql = "UPDATE food_items SET stock_quantity = ?, available = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setBoolean(2, newQuantity > 0);
            ps.setInt(3, foodId);
            ps.executeUpdate();
            syncInventory(foodId, findById(foodId).getName(), newQuantity);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating stock", e);
        }
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM food_items ORDER BY category";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading categories", e);
        }
        return categories;
    }

    public List<FoodItem> searchFood(String query) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + query.toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }
        return items;
    }

    public void ensureInventoryRecord(int foodId, String foodName, int stockQuantity) {
        String sql = "INSERT INTO inventory_items (food_id, food_name, stock_quantity, minimum_threshold, low_stock_alert) VALUES (?, ?, ?, 5, FALSE) ON DUPLICATE KEY UPDATE food_name = VALUES(food_name), stock_quantity = VALUES(stock_quantity)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodId);
            ps.setString(2, foodName);
            ps.setInt(3, stockQuantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Inventory sync failed", e);
        }
    }

    public void syncInventory(int foodId, String foodName, int stockQuantity) {
        String sql = "INSERT INTO inventory_items (food_id, food_name, stock_quantity, minimum_threshold, low_stock_alert) VALUES (?, ?, ?, 5, ?) ON DUPLICATE KEY UPDATE food_name = VALUES(food_name), stock_quantity = VALUES(stock_quantity), low_stock_alert = VALUES(low_stock_alert)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, foodId);
            ps.setString(2, foodName);
            ps.setInt(3, stockQuantity);
            ps.setBoolean(4, stockQuantity < 5);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Inventory sync failed", e);
        }
    }

    private FoodItem mapRow(ResultSet rs) throws SQLException {
        FoodItem food = new FoodItem();
        food.setId(rs.getInt("id"));
        food.setName(rs.getString("name"));
        food.setCategory(rs.getString("category"));
        food.setPrice(rs.getBigDecimal("price"));
        food.setStockQuantity(rs.getInt("stock_quantity"));
        food.setImageUrl(rs.getString("image_url"));
        food.setDescription(rs.getString("description"));
        food.setPreparationTimeMinutes(rs.getInt("preparation_time_minutes"));
        food.setAvailable(rs.getBoolean("available"));
        return food;
    }
}
