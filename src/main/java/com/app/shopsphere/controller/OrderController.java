package com.app.shopsphere.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.OrderResponse;
import com.app.shopsphere.dto.OrderStatsResponse;
import com.app.shopsphere.dto.OrderStatusUpdateRequest;
import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(
            @RequestHeader("User-ID") String userId) {

        boolean created = orderService.createOrder(userId);

        return created
                ? ResponseEntity.ok("Order created successfully")
                : ResponseEntity.badRequest().body("Failed to create order");
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestHeader("User-ID") Long userId,
            @RequestParam(required = false) OrderStatus status) {

        return status != null
                ? ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, status))
                : ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status) {

        if (userId != null && status != null) {
            return ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, status));
        }

        if (userId != null) {
            return ResponseEntity.ok(orderService.getOrdersByUser(userId));
        }

        if (status != null) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/status")
    public ResponseEntity<String> updateOrderStatus(
            @RequestHeader("Order-ID") Long id,
            @RequestBody OrderStatusUpdateRequest statusReq) {

        boolean updated = orderService.updateOrderStatus(id, statusReq.getStatus());

        return updated
                ? ResponseEntity.ok("Order status updated successfully")
                : ResponseEntity.badRequest().body("Invalid status transition or order not found");
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long id) {

        boolean cancelled = orderService.cancelOrder(id);

        return cancelled
                ? ResponseEntity.ok("Order cancelled successfully")
                : ResponseEntity.badRequest().body("Order cannot be cancelled");
    }

    @GetMapping("/me/stats")
    public ResponseEntity<OrderStatsResponse> getMyOrderStats(
            @RequestHeader("User-ID") String userId) {

        return orderService.getOrderStats(Long.valueOf(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}