package com.app.shopsphere.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.cart.CartSeed;
import com.app.shopsphere.dto.cart.CartSeedItem;

import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.model.CartItem;
import com.app.shopsphere.model.Order;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.CartItemRepository;
import com.app.shopsphere.repository.OrderRepository;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Seeds the local development database with sample users, products, carts, and
 * orders.
 */
@Service
@RequiredArgsConstructor
public class SeedService {

    private final CartItemRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    private static final List<OrderStatus> SEED_ORDER_STATUSES = List.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED);

    private static final List<OrderStatus> PROGRESSION_CHAIN = List.of(
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED);

    /**
     * Loads sample users from the classpath seed data and stores them with hashed
     * passwords.
     *
     * @throws IOException when the seed file cannot be read
     */
    public void seedUsers() throws IOException {

        ClassPathResource resource = new ClassPathResource("seed/users.json");

        List<User> users = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<User>>() {
                });

        for (User user : users) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.saveAll(users);
    }

    /**
     * Loads sample products from the classpath seed data.
     *
     * @throws IOException when the seed file cannot be read
     */
    public void seedProducts() throws IOException {

        ClassPathResource resource = new ClassPathResource("seed/products.json");

        List<Product> products = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Product>>() {
                });

        productRepository.saveAll(products);
    }

    /**
     * Recreates cart seed data for the sample users and products.
     *
     * @throws IOException when the seed file cannot be read
     */
    @Transactional
    public void seedCarts() throws IOException {

        cartRepository.deleteAll();

        ClassPathResource resource = new ClassPathResource("seed/carts.json");

        List<CartSeed> cartSeeds = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<CartSeed>>() {
                });

        List<CartItem> cartItems = new ArrayList<>();

        for (CartSeed cartSeed : cartSeeds) {

            Optional<User> userOpt = userRepository.findById(cartSeed.getUserId());

            if (userOpt.isEmpty()) {
                continue;
            }

            User user = userOpt.get();

            for (CartSeedItem item : cartSeed.getItems()) {

                Optional<Product> productOpt = productRepository.findById(item.getProductId());

                if (productOpt.isEmpty()) {
                    continue;
                }

                Product product = productOpt.get();

                if (!product.getActive()) {
                    continue;
                }

                if (product.getStockQuantity() < item.getQuantity()) {
                    continue;
                }

                CartItem cartItem = new CartItem();

                cartItem.setUser(user);
                cartItem.setProduct(product);
                cartItem.setQuantity(item.getQuantity());
                cartItem.setPrice(
                        product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                cartItems.add(cartItem);
            }
        }

        cartRepository.saveAll(cartItems);
    }

    /**
     * Seeds a representative set of orders by converting the seeded carts into
     * completed order workflows.
     *
     * @throws IOException when the seed data cannot be read
     */
    @Transactional
    public void seedOrders() throws IOException {

        orderRepository.deleteAll();

        seedCarts();

        List<Long> userIds = cartRepository.findAll()
                .stream()
                .map(cartItem -> cartItem.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        for (Long userId : userIds) {
            orderService.createOrder(String.valueOf(userId));
        }

        List<Order> createdOrders = orderRepository.findAll()
                .stream()
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId()))
                .collect(Collectors.toList());

        for (int i = 0; i < createdOrders.size(); i++) {

            OrderStatus targetStatus = SEED_ORDER_STATUSES.get(i % SEED_ORDER_STATUSES.size());

            advanceOrderToStatus(createdOrders.get(i).getId(), targetStatus);
        }
    }

    private void advanceOrderToStatus(Long orderId, OrderStatus targetStatus) {

        if (targetStatus == OrderStatus.PENDING) {
            return;
        }

        if (targetStatus == OrderStatus.CANCELLED) {
            orderService.cancelOrder(orderId);
            return;
        }

        for (OrderStatus step : PROGRESSION_CHAIN) {

            orderService.updateOrderStatus(orderId, step);

            if (step == targetStatus) {
                break;
            }
        }
    }
}