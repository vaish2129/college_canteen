package com.collegecanteen.model;

public class InventoryItem {
    private int id;
    private int foodId;
    private String foodName;
    private int stockQuantity;
    private int minimumThreshold;
    private boolean lowStockAlert;

    public InventoryItem() {
    }

    public InventoryItem(int id, int foodId, String foodName, int stockQuantity, int minimumThreshold, boolean lowStockAlert) {
        this.id = id;
        this.foodId = foodId;
        this.foodName = foodName;
        this.stockQuantity = stockQuantity;
        this.minimumThreshold = minimumThreshold;
        this.lowStockAlert = lowStockAlert;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinimumThreshold() {
        return minimumThreshold;
    }

    public void setMinimumThreshold(int minimumThreshold) {
        this.minimumThreshold = minimumThreshold;
    }

    public boolean isLowStockAlert() {
        return lowStockAlert;
    }

    public void setLowStockAlert(boolean lowStockAlert) {
        this.lowStockAlert = lowStockAlert;
    }
}
