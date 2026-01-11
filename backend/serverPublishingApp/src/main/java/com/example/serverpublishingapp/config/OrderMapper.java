package com.example.serverpublishingapp.config;

import com.example.serverpublishingapp.dto.OrderDto;
import com.example.serverpublishingapp.entity.Order;
import org.springframework.stereotype.Component;
import com.example.serverpublishingapp.entity.OrderFile;


@Component
public class OrderMapper {

    public OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setServiceName(order.getService().getTitle());
        dto.setPages(order.getPages());
        dto.setQuantity(order.getQuantity());
        dto.setTotalPrice(order.getTotalPrice());

        dto.setFileUrls(
                order.getFiles().stream()
                        .map(OrderFile::getFilePath)
                        .toList()
        );

        return dto;
    }
}
