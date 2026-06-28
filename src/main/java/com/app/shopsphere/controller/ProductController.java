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

import com.app.shopsphere.dto.ProductRequest;
import com.app.shopsphere.dto.ProductResponse;
import com.app.shopsphere.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return ResponseEntity.ok(
                productService.getProducts(
                        active,
                        keyword,
                        category,
                        minPrice,
                        maxPrice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {

        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest productReq) {

        boolean created = productService.createProduct(productReq);

        return created
                ? ResponseEntity.ok("Product created successfully")
                : ResponseEntity.badRequest().body("Failed to create product");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest productReq) {

        boolean updated = productService.updateProduct(id, productReq);

        return updated
                ? ResponseEntity.ok("Product updated successfully")
                : ResponseEntity.badRequest().body("Failed to update product");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {

        boolean deleted = productService.deleteProductById(id);

        return deleted
                ? ResponseEntity.ok("Product deleted successfully")
                : ResponseEntity.badRequest().body("Failed to delete product");
    }
}