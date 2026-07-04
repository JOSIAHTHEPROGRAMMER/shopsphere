package com.app.shopsphere.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.service.AdminAuthService;
import com.app.shopsphere.service.AdminService;
import com.app.shopsphere.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminAuthService adminAuthService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader("User-ID") String userId) {

        if (!adminAuthService.isAdmin(Long.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestHeader("User-ID") String userId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {

        if (!adminAuthService.isAdmin(Long.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        return ResponseEntity.ok(adminService.getRevenue(from, to));
    }

    @GetMapping("/best-selling")
    public ResponseEntity<?> getBestSelling(
            @RequestHeader("User-ID") String userId,
            @RequestParam(defaultValue = "10") int limit) {

        if (!adminAuthService.isAdmin(Long.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        return ResponseEntity.ok(adminService.getBestSelling(limit));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStock(
            @RequestHeader("User-ID") String userId,
            @RequestParam(defaultValue = "10") int threshold) {

        if (!adminAuthService.isAdmin(Long.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        return ResponseEntity.ok(adminService.getLowStock(threshold));
    }

    @GetMapping("/recent-orders")
    public ResponseEntity<?> getRecentOrders(
            @RequestHeader("User-ID") String userId,
            @RequestParam(defaultValue = "10") int limit) {

        if (!adminAuthService.isAdmin(Long.valueOf(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        return ResponseEntity.ok(orderService.getRecentOrders(limit));
    }
}