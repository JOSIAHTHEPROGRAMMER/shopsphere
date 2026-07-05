package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.order.OrderItemResponse;
import com.app.shopsphere.dto.order.OrderResponse;
import com.app.shopsphere.dto.order.OrderStatsResponse;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartRepository;
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of());

    @Transactional
    public void updateOrderStatus(Long id, OrderStatus newStatus) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        Set<OrderStatus> allowedNextStatuses = ALLOWED_TRANSITIONS.get(order.getStatus());

        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            throw new OrderStatusException(
                    "Cannot transition order from " + order.getStatus() + " to " + newStatus);
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restockOrderItems(order);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        updateOrderStatus(id, OrderStatus.CANCELLED);
    }

    private void restockOrderItems(Order order) {

        for (OrderItem orderItem : order.getItems()) {

            Product product = orderItem.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity() + orderItem.getQuantity());

            productRepository.save(product);
        }
    }

    @Transactional
    public void createOrder(String userId) {

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!product.getActive()) {
                throw new ProductInactiveException("Product is not available: " + product.getName());
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            totalPrice = totalPrice.add(cartItem.getPrice());
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(totalPrice);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);
            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity());

            productRepository.save(product);
        }

        order.setItems(orderItems);

        orderRepository.save(order);

        cartRepository.deleteAll(cartItems);
    }

    public OrderStatsResponse getOrderStats(Long userId) {

        if (userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        int ordersPlaced = orders.size();

        int cancelledOrders = (int) orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .count();

        List<Order> completedOrders = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        BigDecimal moneySpent = completedOrders.stream()
                .map(order -> order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal averageOrder = completedOrders.isEmpty()
                ? BigDecimal.ZERO
                : moneySpent.divide(BigDecimal.valueOf(completedOrders.size()), 2, RoundingMode.HALF_UP);

        LocalDateTime lastOrderDate = orders.stream()
                .map(order -> order.getCreatedAt())
                .filter(createdAt -> createdAt != null)
                .max((a, b) -> a.compareTo(b))
                .orElse(null);

        OrderStatsResponse stats = new OrderStatsResponse();

        stats.setOrdersPlaced(ordersPlaced);
        stats.setMoneySpent(moneySpent);
        stats.setAverageOrder(averageOrder);
        stats.setCancelledOrders(cancelledOrders);
        stats.setLastOrderDate(lastOrderDate);

        return stats;
    }

    @SuppressWarnings("null")
    public List<OrderResponse> getRecentOrders(int limit) {

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc(Order::getCreatedAt)));

        return orderRepository.findAll(pageable)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToOrderResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByUserAndStatus(Long userId, OrderStatus status) {
        return orderRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        orderRepository.delete(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {

        OrderResponse res = new OrderResponse();

        res.setId(String.valueOf(order.getId()));
        res.setUserId(String.valueOf(order.getUser().getId()));
        res.setTotalPrice(order.getTotalPrice());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        res.setItems(items);

        return res;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {

        OrderItemResponse res = new OrderItemResponse();

        res.setId(orderItem.getId());
        res.setProductId(String.valueOf(orderItem.getProduct().getId()));
        res.setProductName(orderItem.getProduct().getName());

        res.setQuantity(orderItem.getQuantity());

        res.setUnitPrice(orderItem.getUnitPrice());

        res.setSubtotal(
                orderItem.getUnitPrice()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())));

        res.setImageUrl(orderItem.getProduct().getImageUrl());

        return res;
    }
}