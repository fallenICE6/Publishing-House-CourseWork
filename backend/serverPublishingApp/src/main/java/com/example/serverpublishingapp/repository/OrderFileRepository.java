package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.OrderFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderFileRepository extends JpaRepository<OrderFile, Long> { }