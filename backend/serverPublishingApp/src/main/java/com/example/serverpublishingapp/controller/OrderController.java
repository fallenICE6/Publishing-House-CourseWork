package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.CreateOrderRequest;
import com.example.serverpublishingapp.dto.OrderDto;
import com.example.serverpublishingapp.dto.OrderFullDto;
import com.example.serverpublishingapp.dto.UpdateOrderStatusRequest;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public OrderDto create(
            @RequestPart("order") CreateOrderRequest order,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal User user
    ) {
        return orderService.create(order, files, user);
    }

    @GetMapping("/my")
    public List<OrderFullDto> myOrders(@AuthenticationPrincipal User user) {
        return orderService.getMyOrders(user);
    }

    @GetMapping("/{id}")
    public OrderFullDto getOrderById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return orderService.getOrderById(id, user);
    }

    @GetMapping("/admin/all")
    public List<OrderFullDto> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return orderService.getAllOrders(search, status);
    }

    @PutMapping("/admin/{id}/status")
    public OrderFullDto updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User admin
    ) {
        return orderService.updateOrderStatus(id, request, admin);
    }

    @GetMapping("/admin/{id}")
    public OrderFullDto getOrderByIdAdmin(@PathVariable Long id) {
        return orderService.getOrderByIdAdmin(id);
    }
}
