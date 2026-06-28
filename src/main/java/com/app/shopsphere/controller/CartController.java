package com.app.shopsphere.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.CartRequest;
import com.app.shopsphere.dto.CartResponse;
import com.app.shopsphere.service.CartItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

    private final CartItemService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("User-ID") String userId,
            @RequestBody CartRequest cartReq) {

        boolean created = cartService.addToCart(userId, cartReq);

        return created
                ? ResponseEntity.ok("product added to cart successfully")
                : ResponseEntity.badRequest().body("Product out of stock / not found or user noy found");
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("User-ID") String userId,
            @PathVariable Long productId) {

        boolean deleted = cartService.deleteItemFromCart(userId, productId);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CartResponse>> getCart(
            @RequestHeader("User-ID") String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return ResponseEntity.ok(
                cartService.getCart(
                        userId,
                        keyword,
                        category,
                        minPrice,
                        maxPrice));
    }

}