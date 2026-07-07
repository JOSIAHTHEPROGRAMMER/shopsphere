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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes catalog endpoints for browsing and administering products.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

        private final ProductService productService;

        /**
         * Lists products using the supplied filtering and pagination criteria.
         *
         * @param active    optional availability filter
         * @param keyword   search text for the catalog
         * @param category  optional category filter
         * @param minPrice  minimum accepted price
         * @param maxPrice  maximum accepted price
         * @param inStock   optional stock availability filter
         * @param minStock  minimum stock threshold
         * @param page      zero-based page index
         * @param size      page size
         * @param sort      sort field
         * @param direction sort direction
         * @return the paged product catalog response
         */
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

        /**
         * Returns a single product for public catalog viewing.
         *
         * @param id the product identifier
         * @return the requested product payload
         */
        @GetMapping("/{id}")
        public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
                return ResponseEntity.ok(productService.getProductById(id));
        }

        /**
         * Creates a product using the admin-only catalog workflow.
         *
         * @param productReq the product payload to persist
         * @return a confirmation message
         */
        @PostMapping
        public ResponseEntity<String> createProduct(@RequestBody @Valid ProductRequest productReq) {
                productService.createProduct(productReq);
                return ResponseEntity.ok("Product created successfully");
        }

        /**
         * Updates an existing product through the admin catalog workflow.
         *
         * @param id         the product identifier
         * @param productReq the updated product values
         * @return a confirmation message
         */
        @PutMapping("/{id}")
        public ResponseEntity<String> updateProduct(
                        @PathVariable Long id,
                        @RequestBody @Valid ProductRequest productReq) {
                productService.updateProduct(id, productReq);
                return ResponseEntity.ok("Product updated successfully");
        }

        /**
         * Deletes a product from the catalog.
         *
         * @param id the product identifier
         * @return a confirmation message
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
                productService.deleteProductById(id);
                return ResponseEntity.ok("Product deleted successfully");
        }
}