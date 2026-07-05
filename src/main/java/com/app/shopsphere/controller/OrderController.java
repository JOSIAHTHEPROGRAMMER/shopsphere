package com.app.shopsphere.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.order.OrderResponse;
import com.app.shopsphere.dto.order.OrderStatsResponse;
import com.app.shopsphere.dto.order.OrderStatusUpdateRequest;
import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.security.SecurityUtil;
import com.app.shopsphere.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder() {

        String userId = String.valueOf(SecurityUtil.getCurrentUserId());

        boolean created = orderService.createOrder(userId);

        return created
                ? ResponseEntity.ok("Order created successfully")
                : ResponseEntity.badRequest().body("Failed to create order");
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status) {

        Long userId = SecurityUtil.getCurrentUserId();

        return status != null
                ? ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, status))
                : ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<OrderStatsResponse> getMyOrderStats() {

        Long userId = SecurityUtil.getCurrentUserId();

        return orderService.getOrderStats(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest statusReq) {

        boolean updated = orderService.updateOrderStatus(id, statusReq.getStatus());

        return updated
                ? ResponseEntity.ok("Order status updated successfully")
                : ResponseEntity.badRequest().body("Invalid status transition or order not found");
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {

        Optional<OrderResponse> orderOpt = orderService.getOrderById(id);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderResponse order = orderOpt.get();

        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isOwner = order.getUserId().equals(String.valueOf(currentUserId));
        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only cancel your own orders");
        }

        boolean cancelled = orderService.cancelOrder(id);

        return cancelled
                ? ResponseEntity.ok("Order cancelled successfully")
                : ResponseEntity.badRequest().body("Order cannot be cancelled");
    }
}