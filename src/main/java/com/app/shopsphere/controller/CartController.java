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

import com.app.shopsphere.dto.cart.AddToCartRequest;
import com.app.shopsphere.dto.cart.BatchAddToCartRequest;
import com.app.shopsphere.dto.cart.CartResponse;
import com.app.shopsphere.dto.cart.CartSummary;
import com.app.shopsphere.dto.cart.UpdateCartRequest;
import com.app.shopsphere.security.SecurityUtil;
import com.app.shopsphere.service.CartItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes cart endpoints for authenticated shoppers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

        private final CartItemService cartService;

        /**
         * Adds a single product to the authenticated user's cart.
         *
         * @param cartReq the requested product and quantity
         * @return a confirmation message
         */
        @PostMapping
        public ResponseEntity<String> addToCart(@RequestBody @Valid AddToCartRequest cartReq) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                cartService.addToCart(userId, cartReq);
                return ResponseEntity.ok("product added to cart successfully");
        }

        /**
         * Removes one product from the authenticated user's cart.
         *
         * @param productId the product to remove
         * @return an empty response when the removal succeeds
         */
        @DeleteMapping("/items/{productId}")
        public ResponseEntity<Void> removeFromCart(@PathVariable Long productId) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                cartService.deleteItemFromCart(userId, productId);
                return ResponseEntity.noContent().build();
        }

        /**
         * Returns the current cart contents for the authenticated user.
         *
         * @param keyword  optional product search text
         * @param category optional category filter
         * @param minPrice optional lower price bound
         * @param maxPrice optional upper price bound
         * @return the matching cart entries
         */
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

        /**
         * Updates the quantity of an existing cart item.
         *
         * @param cartReq the updated quantity and product identifier
         * @return a confirmation message
         */
        @PutMapping("/items")
        public ResponseEntity<String> updateCartItem(@RequestBody @Valid UpdateCartRequest cartReq) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                cartService.updateCartItem(userId, cartReq);
                return ResponseEntity.ok("Cart updated successfully");
        }

        /**
         * Clears the entire cart for the authenticated user.
         *
         * @return an empty response when the cart is cleared
         */
        @DeleteMapping
        public ResponseEntity<Void> clearCart() {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                cartService.clearCart(userId);
                return ResponseEntity.noContent().build();
        }

        /**
         * Adds several products to the cart in one request.
         *
         * @param batchReq the requested batch of cart additions
         * @return a confirmation message
         */
        @PostMapping("/batch")
        public ResponseEntity<String> addMultipleToCart(@RequestBody @Valid BatchAddToCartRequest batchReq) {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                cartService.addMultipleToCart(userId, batchReq.getItems());
                return ResponseEntity.ok("Products added successfully");
        }

        /**
         * Returns a summary of the current cart totals and estimated tax.
         *
         * @return the computed cart summary
         */
        @GetMapping("/summary")
        public ResponseEntity<CartSummary> getCartSummary() {

                String userId = String.valueOf(SecurityUtil.getCurrentUserId());
                return ResponseEntity.ok(cartService.getCartSummary(userId));
        }

}