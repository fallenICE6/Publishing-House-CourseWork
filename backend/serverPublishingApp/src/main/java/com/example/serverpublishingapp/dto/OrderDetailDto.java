package com.example.serverpublishingapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDto {

    public Long id;
    public String status;
    public String statusRu;
    public BigDecimal totalPrice;
    public LocalDateTime createdAt;

    public String fio;
    public String email;
    public String phone;

    public String serviceTitle;
    public Integer pages;
    public Integer quantity;

    public List<OrderMaterialRequest> materials;
    public List<OrderFileDto> files;

    public ReviewDto review;
}
