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

import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.exception.BadRequestException;
import com.app.shopsphere.exception.InsufficientStockException;
import com.app.shopsphere.exception.OrderStatusException;
import com.app.shopsphere.exception.ProductInactiveException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.CartItem;
import com.app.shopsphere.model.Order;
import com.app.shopsphere.model.OrderItem;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.CartItemRepository;
import com.app.shopsphere.repository.OrderRepository;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.service.OrderService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product activeProduct;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);

        activeProduct = new Product();
        activeProduct.setId(2L);
        activeProduct.setName("Test Product");
        activeProduct.setPrice(new BigDecimal("50.00"));
        activeProduct.setStockQuantity(10);
        activeProduct.setActive(true);

        pendingOrder = new Order();
        pendingOrder.setId(100L);
        pendingOrder.setUser(user);
        pendingOrder.setStatus(OrderStatus.PENDING);
        pendingOrder.setItems(List.of());
    }

    @Test
    void updateOrderStatus_succeeds_onValidTransition() {

        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        orderService.updateOrderStatus(100L, OrderStatus.CONFIRMED);

        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository, times(1)).save(pendingOrder);
    }

    @Test
    void updateOrderStatus_throwsOrderStatusException_onInvalidTransition() {

        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(100L, OrderStatus.DELIVERED))
                .isInstanceOf(OrderStatusException.class)
                .hasMessageContaining("PENDING")
                .hasMessageContaining("DELIVERED");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_throwsOrderStatusException_fromTerminalState() {

        pendingOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(100L, OrderStatus.CANCELLED))
                .isInstanceOf(OrderStatusException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_throwsResourceNotFound_whenOrderMissing() {

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, OrderStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelOrder_restocksInventory_whenCancellingConfirmedOrder() {

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(activeProduct);
        orderItem.setQuantity(3);

        pendingOrder.setStatus(OrderStatus.CONFIRMED);
        pendingOrder.setItems(List.of(orderItem));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(pendingOrder));

        orderService.cancelOrder(100L);

        assertThat(activeProduct.getStockQuantity()).isEqualTo(13);
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productRepository, times(1)).save(activeProduct);
    }

    @Test
    void createOrder_throwsBadRequest_whenCartIsEmpty() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder("1"))
                .isInstanceOf(BadRequestException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_throwsProductInactive_whenCartContainsInactiveProduct() {

        Product inactiveProduct = new Product();
        inactiveProduct.setId(3L);
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setPrice(new BigDecimal("10.00"));
        inactiveProduct.setStockQuantity(5);
        inactiveProduct.setActive(false);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(inactiveProduct);
        cartItem.setQuantity(1);
        cartItem.setPrice(new BigDecimal("10.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder("1"))
                .isInstanceOf(ProductInactiveException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_throwsInsufficientStock_whenQuantityExceedsStock() {

        CartItem cartItem = new CartItem();
        cartItem.setProduct(activeProduct);
        cartItem.setQuantity(999);
        cartItem.setPrice(new BigDecimal("50.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder("1"))
                .isInstanceOf(InsufficientStockException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_succeeds_andClearsCart_whenCartIsValid() {

        CartItem cartItem = new CartItem();
        cartItem.setProduct(activeProduct);
        cartItem.setQuantity(2);
        cartItem.setPrice(new BigDecimal("100.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        orderService.createOrder("1");

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).deleteAll(List.of(cartItem));
        assertThat(activeProduct.getStockQuantity()).isEqualTo(8);
    }
}