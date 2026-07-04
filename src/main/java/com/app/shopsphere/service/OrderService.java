package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.OrderItemResponse;
import com.app.shopsphere.dto.OrderResponse;
import com.app.shopsphere.dto.OrderStatsResponse;
import com.app.shopsphere.enum_values.OrderStatus;
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
    public boolean updateOrderStatus(Long id, OrderStatus newStatus) {

        Optional<Order> orderOpt = orderRepository.findById(id);

        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();

        Set<OrderStatus> allowedNextStatuses = ALLOWED_TRANSITIONS.get(order.getStatus());

        if (allowedNextStatuses == null || !allowedNextStatuses.contains(newStatus)) {
            return false;
        }

        if (newStatus == OrderStatus.CANCELLED) {
            restockOrderItems(order);
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        return true;
    }

    @Transactional
    public boolean cancelOrder(Long id) {
        return updateOrderStatus(id, OrderStatus.CANCELLED);
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
    public boolean createOrder(String userId) {

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            return false;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!product.getActive()) {
                return false;
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                return false;
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

        return true;
    }

    public Optional<OrderStatsResponse> getOrderStats(Long userId) {

        if (userRepository.findById(userId).isEmpty()) {
            return Optional.empty();
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

        return Optional.of(stats);
    }

    public List<OrderResponse> getRecentOrders(int limit) {

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());

        return orderRepository.findAll(pageable)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToOrderResponse);
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

    public boolean deleteOrder(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    orderRepository.delete(order);
                    return true;
                })
                .orElse(false);
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