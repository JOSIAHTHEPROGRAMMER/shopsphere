package com.app.shopsphere.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.app.shopsphere.model.Product;

public class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> hasActive(Boolean active) {
        return (root, query, cb) -> active == null
                ? null
                : cb.equal(root.get("active"), active);
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern));
        };
    }

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> category == null || category.isBlank()
                ? null
                : cb.equal(cb.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? null
                : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> hasInStock(Boolean inStock) {
        return (root, query, cb) -> {

            if (inStock == null) {
                return null;
            }

            return inStock
                    ? cb.greaterThan(root.get("stockQuantity"), 0)
                    : cb.equal(root.get("stockQuantity"), 0);
        };
    }

    public static Specification<Product> hasMinStock(Integer minStock) {
        return (root, query, cb) -> minStock == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("stockQuantity"), minStock);
    }
}