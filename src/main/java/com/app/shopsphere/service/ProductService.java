package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.product.PagedResponse;
import com.app.shopsphere.dto.product.ProductRequest;
import com.app.shopsphere.dto.product.ProductResponse;
import com.app.shopsphere.exception.BadRequestException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.specification.ProductSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages catalog operations for products including creation, lookup, updates,
 * deletion, and filtered listing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Creates a new product after validating the incoming request and checking for
     * duplicate names.
     *
     * @param productReq the product payload to persist
     * @throws BadRequestException when the payload is invalid or the name already
     *                             exists
     */
    public void createProduct(ProductRequest productReq) {

        Product product = new Product();
        updateProductFromRequest(product, productReq);

        if (productRepository.existsByName(product.getName())) {
            throw new BadRequestException("A product with this name already exists");
        }

        productRepository.save(product);

        log.info("Product created: {} (id: {})", product.getName(), product.getId());
    }

    /**
     * Returns a single product DTO by identifier.
     *
     * @param id the product identifier
     * @return the product payload for the requested item
     * @throws ResourceNotFoundException when no product matches the identifier
     */
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToProductResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    /**
     * Applies an update to an existing product and persists the changed state.
     *
     * @param id                the product identifier to modify
     * @param updatedProductReq the new product values
     * @throws ResourceNotFoundException when no product matches the identifier
     */
    public void updateProduct(Long id, ProductRequest updatedProductReq) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        updateProductFromRequest(product, updatedProductReq);
        productRepository.save(product);

        log.info("Product updated: {} (id: {})", product.getName(), id);
    }

    /**
     * Removes a product from the catalog.
     *
     * @param id the product identifier to delete
     * @throws ResourceNotFoundException when no product matches the identifier
     */
    public void deleteProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        log.warn("Product deleted: {} (id: {})", product.getName(), id);
    }

    /**
     * Returns a paged view of products filtered by the available catalog criteria.
     *
     * @param active    optional filter for active products
     * @param keyword   text filter applied to the product name or description
     * @param category  optional category filter
     * @param minPrice  lower bound for the product price
     * @param maxPrice  upper bound for the product price
     * @param inStock   optional availability filter
     * @param minStock  minimum stock quantity threshold
     * @param page      zero-based page index
     * @param size      page size
     * @param sort      field used for sorting
     * @param direction sort direction, either asc or desc
     * @return a paged response containing mapped product DTOs
     */
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