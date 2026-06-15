package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.Order;
import com.example.serverpublishingapp.entity.Review;
import com.example.serverpublishingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);

    List<Review> findByReviewer(User reviewer);

    List<Review> findByReviewerUsername(String username);

    @Query("SELECT o FROM Order o WHERE o.status = 'under_review' ORDER BY o.createdAt DESC")
    List<Order> findOrdersForReview();
}
