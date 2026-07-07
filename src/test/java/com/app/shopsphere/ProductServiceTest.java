package com.app.shopsphere;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.app.shopsphere.dto.product.ProductRequest;
import com.app.shopsphere.dto.product.ProductResponse;
import com.app.shopsphere.exception.BadRequestException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.service.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest validRequest;
    private Product existingProduct;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {

        validRequest = new ProductRequest();
        validRequest.setName("Test Product");
        validRequest.setDescription("A product for testing");
        validRequest.setPrice(new BigDecimal("29.99"));
        validRequest.setStockQuantity(10);
        validRequest.setCategory("Test");
        validRequest.setActive(true);

        existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Existing Product");
        existingProduct.setDescription("Already in the DB");
        existingProduct.setPrice(new BigDecimal("49.99"));
        existingProduct.setStockQuantity(5);
        existingProduct.setCategory("Test");
        existingProduct.setActive(true);
    }

    @Test
    void createProduct_savesSuccessfully_whenNameIsUnique() {

        when(productRepository.existsByName(validRequest.getName())).thenReturn(false);

        productService.createProduct(validRequest);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_throwsBadRequest_whenNameAlreadyExists() {

        when(productRepository.existsByName(validRequest.getName())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_returnsProduct_whenFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getName()).isEqualTo("Existing Product");
    }

    @Test
    void getProductById_throwsResourceNotFound_whenMissing() {

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateProduct_updatesFields_whenProductExists() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        ProductRequest updateReq = new ProductRequest();
        updateReq.setName("Updated Name");
        updateReq.setDescription("Updated description");
        updateReq.setPrice(new BigDecimal("19.99"));
        updateReq.setStockQuantity(20);
        updateReq.setCategory("Updated");
        updateReq.setActive(false);

        productService.updateProduct(1L, updateReq);

        assertThat(existingProduct.getName()).isEqualTo("Updated Name");
        assertThat(existingProduct.getPrice()).isEqualByComparingTo("19.99");
        assertThat(existingProduct.getActive()).isFalse();
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void updateProduct_throwsResourceNotFound_whenProductMissing() {

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(999L, validRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProductById_deletesSuccessfully_whenProductExists() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        productService.deleteProductById(1L);

        verify(productRepository, times(1)).delete(existingProduct);
    }

    @Test
    void deleteProductById_throwsResourceNotFound_whenProductMissing() {

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }
}