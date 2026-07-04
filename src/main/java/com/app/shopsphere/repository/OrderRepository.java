package com.app.shopsphere.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    long countByStatus(OrderStatus status);

    List<Order> findByStatusNot(OrderStatus status);

    List<Order> findByStatusNotAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);
}