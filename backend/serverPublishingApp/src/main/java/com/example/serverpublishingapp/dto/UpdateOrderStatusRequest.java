package com.example.serverpublishingapp.dto;

import com.example.serverpublishingapp.entity.Order;

public class UpdateOrderStatusRequest {
    private String status;

    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Order.Status getStatusAsEnum() {
        if (status == null) {
            throw new RuntimeException("Статус не может быть null");
        }

        try {
            return Order.Status.valueOf(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Неизвестный статус: " + status +
                    ". Допустимые значения: created, under_review, editing, ready_for_print, completed, canceled");
        }
    }
}