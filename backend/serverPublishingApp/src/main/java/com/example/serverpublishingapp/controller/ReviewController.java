package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.CreateReviewRequest;
import com.example.serverpublishingapp.dto.OrderForReviewDto;
import com.example.serverpublishingapp.dto.OrderFullDto;
import com.example.serverpublishingapp.dto.ReviewDto;
import com.example.serverpublishingapp.service.OrderService;
import com.example.serverpublishingapp.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final OrderService orderService;

    public ReviewController(ReviewService reviewService, OrderService orderService) {
        this.reviewService = reviewService;
        this.orderService = orderService;
    }

    @GetMapping("/pending-orders")
    public ResponseEntity<List<OrderForReviewDto>> getOrdersForReview(Authentication authentication) {
        String username = authentication.getName();
        List<OrderForReviewDto> orders = reviewService.getOrdersAvailableForReview(username);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/order-details/{orderId}")
    public ResponseEntity<OrderFullDto> getOrderDetailsForReview(@PathVariable Long orderId) {
        OrderFullDto order = orderService.getOrderByIdAdmin(orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ReviewDto> createReview(
            @PathVariable Long orderId,
            @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ReviewDto review = reviewService.createReview(orderId, username, request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewDto>> getMyReviews(Authentication authentication) {
        String username = authentication.getName();
        List<ReviewDto> reviews = reviewService.getReviewsByReviewer(username);
        return ResponseEntity.ok(reviews);
    }

}