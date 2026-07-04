package com.app.shopsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.app.shopsphere.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

        boolean existsByName(String name);

        long countByStockQuantityGreaterThan(Integer stockQuantity);

        long countByStockQuantity(Integer stockQuantity);

        long countByStockQuantityBetween(Integer min, Integer max);

        List<Product> findByStockQuantityBetween(Integer min, Integer max);

        List<Product> findByActive(Boolean active);

        List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        String name,
                        String description,
                        String category);

        List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndDescriptionContainingIgnoreCaseOrActiveTrueAndCategoryContainingIgnoreCase(
                        String name,
                        String description,
                        String category);

}