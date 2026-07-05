package com.app.shopsphere.controller;

import java.math.BigDecimal;

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

import com.app.shopsphere.dto.product.PagedResponse;
import com.app.shopsphere.dto.product.ProductRequest;
import com.app.shopsphere.dto.product.ProductResponse;
import com.app.shopsphere.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

        private final ProductService productService;

        @GetMapping
        public ResponseEntity<PagedResponse<ProductResponse>> getProducts(
                        @RequestParam(required = false) Boolean active,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) Boolean inStock,
                        @RequestParam(required = false) Integer minStock,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size,
                        @RequestParam(defaultValue = "id") String sort,
                        @RequestParam(defaultValue = "asc") String direction) {

                return ResponseEntity.ok(
                                productService.getProducts(
                                                active,
                                                keyword,
                                                category,
                                                minPrice,
                                                maxPrice,
                                                inStock,
                                                minStock,
                                                page,
                                                size,
                                                sort,
                                                direction));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
                return ResponseEntity.ok(productService.getProductById(id));
        }

        @PostMapping
        public ResponseEntity<String> createProduct(@RequestBody ProductRequest productReq) {
                productService.createProduct(productReq);
                return ResponseEntity.ok("Product created successfully");
        }

        @PutMapping("/{id}")
        public ResponseEntity<String> updateProduct(
                        @PathVariable Long id,
                        @RequestBody ProductRequest productReq) {
                productService.updateProduct(id, productReq);
                return ResponseEntity.ok("Product updated successfully");
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
                productService.deleteProductById(id);
                return ResponseEntity.ok("Product deleted successfully");
        }
}