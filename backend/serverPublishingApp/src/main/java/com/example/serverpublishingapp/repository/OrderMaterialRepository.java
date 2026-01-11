package com.example.serverpublishingapp.repository;

import com.example.serverpublishingapp.entity.OrderMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMaterialRepository extends JpaRepository<OrderMaterial, Long> { }
