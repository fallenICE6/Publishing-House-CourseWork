package com.example.serverpublishingapp.dto;


import java.util.List;

public class CreateOrderRequest {
    private Long serviceId;
    private Integer pages;
    private Integer quantity;
    private List<OrderMaterialRequest> materials;
    private String email;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<OrderMaterialRequest> getMaterials() {
        return materials;
    }

    public void setMaterials(List<OrderMaterialRequest> materials) {
        this.materials = materials;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

