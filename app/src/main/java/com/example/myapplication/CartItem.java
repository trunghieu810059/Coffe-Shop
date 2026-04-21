package com.example.myapplication;

public class CartItem {
    public String name;
    public String size;
    public String topping;
    public int quantity;

    public int basePrice;
    public int unitPrice;
    public int totalPrice;

    public CartItem(String name, String size, String topping, int quantity, int basePrice) {
        this.name = name;
        this.size = size;
        this.topping = topping;
        this.quantity = quantity;
        this.basePrice = basePrice;

        recalculatePrice();
    }

    public void recalculatePrice() {
        int sizeExtra = "L".equalsIgnoreCase(size) ? 5000 : 0;
        int toppingExtra = "Không".equalsIgnoreCase(topping) ? 0 : 3000;

        this.unitPrice = this.basePrice + sizeExtra + toppingExtra;
        this.totalPrice = this.unitPrice * this.quantity;
    }

    public void updateTotalPrice() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}