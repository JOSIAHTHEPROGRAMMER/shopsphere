package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.ProductRequest;
import com.app.shopsphere.dto.ProductResponse;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public boolean createProduct(ProductRequest productReq) {

        Product product = new Product();
        updateProductFromRequest(product, productReq);

        if (product.getName() == null || product.getName().isBlank() ||
                product.getPrice() == null ||
                product.getStockQuantity() == null) {
            return false;
        }

        if (productRepository.existsByName(product.getName())) {
            return false;
        }

        productRepository.save(product);
        return true;
    }

    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToProductResponse);
    }

    public boolean updateProduct(Long id, ProductRequest updatedProductReq) {

        return productRepository.findById(id)
                .map(product -> {
                    updateProductFromRequest(product, updatedProductReq);
                    productRepository.save(product);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteProductById(Long id) {

        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return true;
                })
                .orElse(false);
    }

    public List<ProductResponse> getProducts(
            Boolean active,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository.findAll()
                .stream()
                .filter(product -> active == null || product.getActive().equals(active))
                .filter(product -> keyword == null || keyword.isBlank()
                        || product.getName().toLowerCase().contains(keyword.toLowerCase())
                        || product.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .filter(product -> category == null || category.isBlank()
                        || product.getCategory().equalsIgnoreCase(category))
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private void updateProductFromRequest(Product product, ProductRequest productReq) {

        product.setName(productReq.getName());
        product.setDescription(productReq.getDescription());
        product.setPrice(productReq.getPrice());
        product.setStockQuantity(productReq.getStockQuantity());
        product.setImageUrl(productReq.getImageUrl());
        product.setCategory(productReq.getCategory());

        if (productReq.getActive() != null) {
            product.setActive(productReq.getActive());
        }
    }

    private ProductResponse mapToProductResponse(Product product) {

        ProductResponse res = new ProductResponse();

        res.setId(String.valueOf(product.getId()));
        res.setName(product.getName());
        res.setDescription(product.getDescription());
        res.setPrice(product.getPrice());
        res.setStockQuantity(product.getStockQuantity());
        res.setImageUrl(product.getImageUrl());
        res.setCategory(product.getCategory());
        res.setActive(product.getActive());

        return res;
    }
}