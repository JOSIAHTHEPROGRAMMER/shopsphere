package com.app.shopsphere.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.AdminDashboardResponse;
import com.app.shopsphere.dto.AdminRevenueResponse;
import com.app.shopsphere.dto.BestSellingProductResponse;
import com.app.shopsphere.dto.OrderResponse;
import com.app.shopsphere.dto.ProductResponse;
import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.service.AdminService;
import com.app.shopsphere.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/revenue")
    public ResponseEntity<AdminRevenueResponse> getRevenue(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {
        return ResponseEntity.ok(adminService.getRevenue(from, to));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<List<BestSellingProductResponse>> getBestSelling(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(adminService.getBestSelling(limit));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(adminService.getLowStock(threshold));
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderService.getRecentOrders(limit));
    }

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