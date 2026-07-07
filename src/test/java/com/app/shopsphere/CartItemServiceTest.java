package com.app.shopsphere;

import java.math.BigDecimal;
import java.util.List;
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

import com.app.shopsphere.dto.cart.AddToCartRequest;
import com.app.shopsphere.dto.cart.CartSummary;
import com.app.shopsphere.dto.cart.UpdateCartRequest;
import com.app.shopsphere.exception.InsufficientStockException;
import com.app.shopsphere.exception.ProductInactiveException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.CartItem;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.CartItemRepository;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.service.CartItemService;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartItemService cartItemService;

    private User user;
    private Product activeProduct;
    private Product inactiveProduct;
    private AddToCartRequest addRequest;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);

        activeProduct = new Product();
        activeProduct.setId(2L);
        activeProduct.setName("Active Product");
        activeProduct.setPrice(new BigDecimal("25.00"));
        activeProduct.setStockQuantity(10);
        activeProduct.setActive(true);

        inactiveProduct = new Product();
        inactiveProduct.setId(3L);
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setPrice(new BigDecimal("15.00"));
        inactiveProduct.setStockQuantity(10);
        inactiveProduct.setActive(false);

        addRequest = new AddToCartRequest();
        addRequest.setProductId(2L);
        addRequest.setQuantity(2);
    }

    @Test
    void addToCart_createsNewCartItem_whenNoneExists() {

        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(null);

        cartItemService.addToCart("1", addRequest);

        verify(cartRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_mergesQuantity_whenCartItemAlreadyExists() {

        CartItem existing = new CartItem();
        existing.setUser(user);
        existing.setProduct(activeProduct);
        existing.setQuantity(3);
        existing.setPrice(new BigDecimal("75.00"));

        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(existing);

        cartItemService.addToCart("1", addRequest);

        assertThat(existing.getQuantity()).isEqualTo(5);
        verify(cartRepository, times(1)).save(existing);
    }

    @Test
    void addToCart_throwsResourceNotFound_whenProductMissing() {

        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.addToCart("1", addRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_throwsProductInactive_whenProductIsInactive() {

        addRequest.setProductId(3L);
        when(productRepository.findById(3L)).thenReturn(Optional.of(inactiveProduct));

        assertThatThrownBy(() -> cartItemService.addToCart("1", addRequest))
                .isInstanceOf(ProductInactiveException.class);

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_throwsInsufficientStock_whenQuantityExceedsStock() {

        addRequest.setQuantity(999);
        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));

        assertThatThrownBy(() -> cartItemService.addToCart("1", addRequest))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_throwsResourceNotFound_whenUserMissing() {

        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.addToCart("1", addRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void deleteItemFromCart_deletesSuccessfully_whenCartItemExists() {

        CartItem existing = new CartItem();
        existing.setUser(user);
        existing.setProduct(activeProduct);

        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(existing);

        cartItemService.deleteItemFromCart("1", 2L);

        verify(cartRepository, times(1)).delete(existing);
    }

    @Test
    void deleteItemFromCart_throwsResourceNotFound_whenCartItemMissing() {

        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(null);

        assertThatThrownBy(() -> cartItemService.deleteItemFromCart("1", 2L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cartRepository, never()).delete(any(CartItem.class));
    }

    @Test
    void updateCartItem_deletesItem_whenQuantityIsZero() {

        CartItem existing = new CartItem();
        existing.setUser(user);
        existing.setProduct(activeProduct);
        existing.setQuantity(2);

        UpdateCartRequest updateRequest = new UpdateCartRequest();
        updateRequest.setProductId(2L);
        updateRequest.setQuantity(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(existing);

        cartItemService.updateCartItem("1", updateRequest);

        verify(cartRepository, times(1)).delete(existing);
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_throwsInsufficientStock_whenQuantityExceedsStock() {

        CartItem existing = new CartItem();
        existing.setUser(user);
        existing.setProduct(activeProduct);
        existing.setQuantity(2);

        UpdateCartRequest updateRequest = new UpdateCartRequest();
        updateRequest.setProductId(2L);
        updateRequest.setQuantity(999);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(activeProduct));
        when(cartRepository.findByUserAndProduct(user, activeProduct)).thenReturn(existing);

        assertThatThrownBy(() -> cartItemService.updateCartItem("1", updateRequest))
                .isInstanceOf(InsufficientStockException.class);

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void getCartSummary_calculatesTotalsCorrectly() {

        CartItem item1 = new CartItem();
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("50.00"));

        CartItem item2 = new CartItem();
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("25.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(item1, item2));

        CartSummary summary = cartItemService.getCartSummary("1");

        assertThat(summary.getTotalItems()).isEqualTo(3);
        assertThat(summary.getTotalUniqueItems()).isEqualTo(2);
        assertThat(summary.getSubtotal()).isEqualByComparingTo("75.00");
        assertThat(summary.getEstimatedTax()).isEqualByComparingTo("5.25");
        assertThat(summary.getTotal()).isEqualByComparingTo("80.25");
    }

    @Test
    void getCartSummary_throwsResourceNotFound_whenUserMissing() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.getCartSummary("1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}