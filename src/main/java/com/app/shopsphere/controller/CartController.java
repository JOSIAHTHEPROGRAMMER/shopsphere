package com.app.shopsphere.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.CartRequest;
import com.app.shopsphere.dto.CartResponse;
import com.app.shopsphere.dto.CartSummary;
import com.app.shopsphere.security.SecurityUtil;
import com.app.shopsphere.service.CartItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

        private final CartItemService cartService;

        @PostMapping
        public ResponseEntity<String> addToCart(@RequestBody CartRequest cartReq) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                boolean created = cartService.addToCart(userId, cartReq);

                return created
                                ? ResponseEntity.ok("product added to cart successfully")
                                : ResponseEntity.badRequest()
                                                .body("Product out of stock / not found or user not found");
        }

        @DeleteMapping("/items/{productId}")
        public ResponseEntity<Void> removeFromCart(@PathVariable Long productId) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                boolean deleted = cartService.deleteItemFromCart(userId, productId);

                return deleted
                                ? ResponseEntity.noContent().build()
                                : ResponseEntity.notFound().build();
        }

        @GetMapping
        public ResponseEntity<List<CartResponse>> getCart(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                return ResponseEntity.ok(
                                cartService.getCart(
                                                userId,
                                                keyword,
                                                category,
                                                minPrice,
                                                maxPrice));
        }

        @PutMapping("/items")
        public ResponseEntity<String> updateCartItem(@RequestBody CartRequest cartReq) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                boolean updated = cartService.updateCartItem(userId, cartReq);

                return updated
                                ? ResponseEntity.ok("Cart updated successfully")
                                : ResponseEntity.badRequest().body("Failed to update cart");
        }

        @DeleteMapping
        public ResponseEntity<Void> clearCart() {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                cartService.clearCart(userId);
                return ResponseEntity.noContent().build();
        }

        @PostMapping("/batch")
        public ResponseEntity<String> addMultipleToCart(@RequestBody List<CartRequest> cartRequests) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                boolean added = cartService.addMultipleToCart(userId, cartRequests);

                return added
                                ? ResponseEntity.ok("Products added successfully")
                                : ResponseEntity.badRequest().body("Failed to add one or more products");
        }

        @GetMapping("/summary")
        public ResponseEntity<CartSummary> getCartSummary() {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());

                return cartService.getCartSummary(userId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

}