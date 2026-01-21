package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.dto.CreateReviewRequest;
import com.example.serverpublishingapp.dto.OrderFileDto;
import com.example.serverpublishingapp.dto.OrderForReviewDto;
import com.example.serverpublishingapp.dto.ReviewDto;
import com.example.serverpublishingapp.entity.Order;
import com.example.serverpublishingapp.entity.Review;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.repository.OrderRepository;
import com.example.serverpublishingapp.repository.ReviewRepository;
import com.example.serverpublishingapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository,
                         UserRepository userRepository, OrderService orderService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @Transactional
    public ReviewDto createReview(Long orderId, String reviewerUsername, CreateReviewRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!Order.Status.under_review.equals(order.getStatus())) {
            throw new RuntimeException("Order is not in 'under_review' status. Current status: " + order.getStatus());
        }

        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));


        if (reviewRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Review already exists for this order");
        }


        String decision = request.getDecision();
        if (decision == null || decision.trim().isEmpty()) {
            throw new RuntimeException("Decision is required");
        }


        String reviewStatus;
        String orderStatus;

        switch (decision.toLowerCase()) {
            case "approve":
                reviewStatus = "approved";
                orderStatus = "ready_for_print";
                break;
            case "reject":
                reviewStatus = "rejected";
                orderStatus = "canceled";
                break;
            case "revision":
                reviewStatus = "pending";
                orderStatus = "editing";
                break;
            default:
                throw new RuntimeException("Invalid decision. Must be: approve, reject, or revision");
        }


        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setComment(request.getComment());
        review.setStatus(reviewStatus);
        review.setOrderStatusAfterReview(orderStatus);
        review.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        updateOrderStatus(orderId, orderStatus);

        return convertToDTO(savedReview);
    }

    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Order.Status statusEnum;
        switch (newStatus.toLowerCase()) {
            case "editing":
                statusEnum = Order.Status.editing;
                break;
            case "ready_for_print":
                statusEnum = Order.Status.ready_for_print;
                break;
            case "canceled":
                statusEnum = Order.Status.canceled;
                break;
            default:
                throw new RuntimeException("Invalid order status: " + newStatus);
        }

        order.setStatus(statusEnum);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    public String translateReviewStatus(String status) {
        if (status == null) return "—";
        return switch (status.toLowerCase()) {
            case "pending" -> "На доработку";
            case "approved" -> "Одобрено";
            case "rejected" -> "Отклонено";
            default -> status;
        };
    }

    public String translateOrderStatus(String status) {
        if (status == null) return "—";
        return switch (status.toLowerCase()) {
            case "editing" -> "Редактируется";
            case "ready_for_print" -> "Готов к печати";
            case "canceled" -> "Отменён";
            default -> status;
        };
    }


    public List<OrderForReviewDto> getOrdersAvailableForReview(String reviewerUsername) {
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        List<Order> orders = orderRepository.findByStatus(Order.Status.under_review);

        return orders.stream()
                .map(order -> {
                    boolean hasReview = false;
                    if (order.getReview() != null) {
                        hasReview = order.getReview().getReviewer().getId().equals(reviewer.getId());
                    }

                    List<OrderFileDto> files = order.getFiles().stream()
                            .map(f -> new OrderFileDto(
                                    f.getId(),
                                    f.getFileName(),
                                    f.getFileType(),
                                    "/api/files/" + f.getId() + "/download"
                            ))
                            .collect(Collectors.toList());

                    String status = orderService.translateOrderStatus(order.getStatus().toString());

                    return new OrderForReviewDto(
                            order.getId(),
                            order.getUser().getFullName(),
                            order.getService().getTitle(),
                            order.getPages(),
                            order.getQuantity(),
                            order.getTotalPrice(),
                            order.getCreatedAt(),
                            files,
                            hasReview,
                            status
                    );
                })
                .collect(Collectors.toList());
    }

    private ReviewDto convertToDTO(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getOrder().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getFullName(),
                review.getComment(),
                review.getStatus(),
                translateOrderStatus(review.getOrderStatusAfterReview()),
                review.getCreatedAt()
        );
    }

    public List<ReviewDto> getReviewsByReviewer(String reviewerUsername) {
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        List<Review> reviews = reviewRepository.findByReviewer(reviewer);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}