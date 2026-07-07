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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes order endpoints for checkout, history, and status updates.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order from the authenticated user's current cart.
     *
     * @return a confirmation message
     */
    @PostMapping
    public ResponseEntity<String> createOrder() {

        String userId = String.valueOf(SecurityUtil.getCurrentUserId());
        orderService.createOrder(userId);
        return ResponseEntity.ok("Order created successfully");
    }

    /**
     * Returns the current user's order history, optionally filtered by status.
     *
     * @param status optional order status filter
     * @return the matching orders for the authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status) {

        Long userId = SecurityUtil.getCurrentUserId();

        return status != null
                ? ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, status))
                : ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    /**
     * Returns aggregate statistics for the authenticated user's order history.
     *
     * @return the user's order summary data
     */
    @GetMapping("/me/stats")
    public ResponseEntity<OrderStatsResponse> getMyOrderStats() {

        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrderStats(userId));
    }

    /**
     * Updates an order status when the caller has administrative access.
     *
     * @param id        the order identifier
     * @param statusReq the new order status value
     * @return a confirmation message
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody @Valid OrderStatusUpdateRequest statusReq) {

        orderService.updateOrderStatus(id, statusReq.getStatus());
        return ResponseEntity.ok("Order status updated successfully");
    }

    /**
     * Cancels an order when the caller owns it or has admin access.
     *
     * @param id the order identifier
     * @return a confirmation message
     */
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