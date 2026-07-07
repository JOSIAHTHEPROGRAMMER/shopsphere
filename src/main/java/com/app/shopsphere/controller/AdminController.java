package com.app.shopsphere.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.admin.AdminDashboardResponse;
import com.app.shopsphere.dto.admin.AdminRevenueResponse;
import com.app.shopsphere.dto.admin.BestSellingProductResponse;
import com.app.shopsphere.dto.order.OrderResponse;
import com.app.shopsphere.dto.product.ProductResponse;
import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.service.AdminService;
import com.app.shopsphere.service.OrderService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes administrative reporting endpoints for store oversight.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    /**
     * Returns the aggregated dashboard metrics used by administrators.
     *
     * @return the dashboard summary payload
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    /**
     * Returns revenue metrics for the requested time range.
     *
     * @param from optional start date
     * @param to   optional end date
     * @return the revenue summary response
     */
    @GetMapping("/revenue")
    public ResponseEntity<AdminRevenueResponse> getRevenue(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        return ResponseEntity.ok(adminService.getRevenue(from, to));
    }

    /**
     * Returns the highest volume products for sales reporting.
     *
     * @param limit the maximum number of products to include
     * @return the ranked sales summary
     */
    @GetMapping("/best-selling")
    public ResponseEntity<List<BestSellingProductResponse>> getBestSelling(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getBestSelling(limit));
    }

    /**
     * Returns products that are at or below the supplied stock threshold.
     *
     * @param threshold the stock threshold to evaluate
     * @return the low inventory products
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(adminService.getLowStock(threshold));
    }

    /**
     * Returns the most recently created orders for admin review.
     *
     * @param limit the maximum number of orders to include
     * @return the recent order list
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderService.getRecentOrders(limit));
    }

    /**
     * Returns orders optionally filtered by a user or an order status.
     *
     * @param userId optional user filter
     * @param status optional status filter
     * @return the matching orders
     */
    @GetMapping("/orders")
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
}