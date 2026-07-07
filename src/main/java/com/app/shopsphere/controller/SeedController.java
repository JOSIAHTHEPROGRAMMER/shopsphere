package com.app.shopsphere.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.service.SeedService;

import lombok.RequiredArgsConstructor;

/**
 * Exposes seed endpoints for loading local development sample data.
 */
@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class SeedController {

    private final SeedService seedService;

    /**
     * Seeds sample users into the local database.
     *
     * @return a confirmation message or an error response
     */
    @PostMapping("/users")
    public ResponseEntity<String> seedUsers() {
        try {
            seedService.seedUsers();
            return ResponseEntity.ok("Users seeded successfully.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to seed users: " + e.getMessage());
        }
    }

    /**
     * Seeds sample products into the local database.
     *
     * @return a confirmation message or an error response
     */
    @PostMapping("/products")
    public ResponseEntity<String> seedProducts() {
        try {
            seedService.seedProducts();
            return ResponseEntity.ok("Products seeded successfully.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to seed products: " + e.getMessage());
        }
    }

    /**
     * Seeds sample cart contents for the local data set.
     *
     * @return a confirmation message or an error response
     */
    @PostMapping("/carts")
    public ResponseEntity<String> seedCarts() {

        try {
            seedService.seedCarts();
            return ResponseEntity.ok("Carts seeded successfully.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to seed carts: " + e.getMessage());
        }
    }

    /**
     * Seeds sample orders and order state transitions for local development.
     *
     * @return a confirmation message or an error response
     */
    @PostMapping("/orders")
    public ResponseEntity<String> seedOrders() {

        try {
            seedService.seedOrders();
            return ResponseEntity.ok("Orders seeded successfully.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to seed orders: " + e.getMessage());
        }
    }

}