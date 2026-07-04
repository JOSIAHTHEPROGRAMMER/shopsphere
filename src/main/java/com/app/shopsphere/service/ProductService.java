package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.PagedResponse;
import com.app.shopsphere.dto.ProductRequest;
import com.app.shopsphere.dto.ProductResponse;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.specification.ProductSpecification;

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

    public PagedResponse<ProductResponse> getProducts(
            Boolean active,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Integer minStock,
            int page,
            int size,
            String sort,
            String direction) {

        Specification<Product> spec = Specification.where(ProductSpecification.hasActive(active))
                .and(ProductSpecification.hasKeyword(keyword))
                .and(ProductSpecification.hasCategory(category))
                .and(ProductSpecification.hasMinPrice(minPrice))
                .and(ProductSpecification.hasMaxPrice(maxPrice))
                .and(ProductSpecification.hasInStock(inStock))
                .and(ProductSpecification.hasMinStock(minStock));

        Sort sortObj = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        PagedResponse<ProductResponse> response = new PagedResponse<>();

        response.setContent(
                productPage.getContent()
                        .stream()
                        .map(this::mapToProductResponse)
                        .collect(Collectors.toList()));

        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setLast(productPage.isLast());

        return response;
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