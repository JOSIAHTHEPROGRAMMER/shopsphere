package com.app.shopsphere.controller;

import java.util.List;

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
import com.app.shopsphere.exception.ForbiddenException;
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
        orderService.createOrder(userId);
        return ResponseEntity.ok("Order created successfully");
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
        return ResponseEntity.ok(orderService.getOrderStats(userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest statusReq) {

        orderService.updateOrderStatus(id, statusReq.getStatus());
        return ResponseEntity.ok("Order status updated successfully");
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {

        OrderResponse order = orderService.getOrderById(id);

        Long currentUserId = SecurityUtil.getCurrentUserId();
        boolean isOwner = order.getUserId().equals(String.valueOf(currentUserId));
        boolean isAdmin = SecurityUtil.hasRole("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You can only cancel your own orders");
        }

        orderService.cancelOrder(id);
        return ResponseEntity.ok("Order cancelled successfully");
    }
}