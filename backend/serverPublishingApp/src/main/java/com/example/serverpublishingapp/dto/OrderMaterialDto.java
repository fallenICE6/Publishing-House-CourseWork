package com.example.serverpublishingapp.dto;

import java.math.BigDecimal;

public class OrderMaterialDto {

    private String name;
    private String category;
    private int quantity;
    private BigDecimal price;

    public OrderMaterialDto(String name, String category, int quantity, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
