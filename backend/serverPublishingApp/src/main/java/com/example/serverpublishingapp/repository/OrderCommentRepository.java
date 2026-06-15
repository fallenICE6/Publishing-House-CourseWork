package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.OrderComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderCommentRepository extends JpaRepository<OrderComment, Long> {
    List<OrderComment> findByOrderIdOrderByCreatedAtAsc(Long orderId);
    List<OrderComment> findByOrderIdAndIsSystemFalseOrderByCreatedAtAsc(Long orderId);
}