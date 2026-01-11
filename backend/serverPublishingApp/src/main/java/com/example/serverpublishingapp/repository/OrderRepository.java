package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o WHERE " +
            "CAST(o.id AS string) = :search OR " +
            "LOWER(o.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.user.middleName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Order> searchByOrderIdOrFullName(@Param("search") String search);
}
