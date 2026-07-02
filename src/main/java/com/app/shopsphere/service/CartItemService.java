package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.CartRequest;
import com.app.shopsphere.dto.CartResponse;
import com.app.shopsphere.dto.CartSummary;
import com.app.shopsphere.model.CartItem;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.CartItemRepository;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartRepository;
    private final UserRepository userRepository;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.07");

    public boolean addToCart(String userId, CartRequest cartReq) {

        Optional<Product> productOpt = productRepository.findById(cartReq.getProductId());

        if (productOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

        if (!product.getActive()) {
            return false;
        }

        if (product.getStockQuantity() < cartReq.getQuantity())
            return false;

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if (userOpt.isEmpty())
            return false;

        User user = userOpt.get();

        CartItem prevCartItem = cartRepository.findByUserAndProduct(user, product);

        if (prevCartItem != null) {
            prevCartItem.setQuantity(prevCartItem.getQuantity() + cartReq.getQuantity());
            prevCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(prevCartItem.getQuantity())));

            cartRepository.save(prevCartItem);

        } else {

            CartItem cart = new CartItem();

            cart.setUser(user);
            cart.setProduct(product);
            cart.setQuantity(cartReq.getQuantity());
            cart.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartReq.getQuantity())));

            cartRepository.save(cart);

        }
        return true;
    }

    public boolean deleteItemFromCart(String userId, Long productId) {

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Product product = productOpt.get();

        CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

        if (cartItem == null) {
            return false;
        }

        cartRepository.delete(cartItem);
        return true;
    }

    public void clearCart(String userId) {

        userRepository.findById(Long.valueOf(userId))
                .ifPresent(user -> cartRepository.deleteByUser(user));
    }

    public boolean addMultipleToCart(
            String userId,
            List<CartRequest> cartRequests) {

        for (CartRequest cartRequest : cartRequests) {

            if (!addToCart(userId, cartRequest)) {
                return false;
            }
        }

        return true;
    }

    public Optional<CartSummary> getCartSummary(String userId) {

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        List<CartItem> cartItems = cartRepository.findByUserId(Long.valueOf(userId));

        int totalItems = cartItems.stream()
                .mapToInt(item -> {
                    Integer qty = item.getQuantity();
                    return qty != null ? qty : 0;
                })
                .sum();

        int totalUniqueItems = cartItems.size();

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal estimatedTax = subtotal
                .multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal
                .add(estimatedTax)
                .setScale(2, RoundingMode.HALF_UP);

        CartSummary summary = new CartSummary();

        summary.setTotalItems(totalItems);
        summary.setTotalUniqueItems(totalUniqueItems);
        summary.setSubtotal(subtotal);
        summary.setEstimatedTax(estimatedTax);
        summary.setTotal(total);

        return Optional.of(summary);
    }

    public boolean updateCartItem(
            String userId,
            CartRequest cartReq) {

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

        if (userOpt.isEmpty()) {
            return false;
        }
        Long productId = cartReq.getProductId();

        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Product product = productOpt.get();

        CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

        if (cartItem == null) {
            return false;
        }

        if (!product.getActive()) {
            return false;
        }

        if (cartReq.getQuantity() <= 0) {

            cartRepository.delete(cartItem);
            return true;
        }

        if (cartReq.getQuantity() > product.getStockQuantity()) {
            return false;
        }

        cartItem.setQuantity(cartReq.getQuantity());

        cartItem.setPrice(
                product.getPrice().multiply(
                        BigDecimal.valueOf(cartReq.getQuantity())));

        cartRepository.save(cartItem);

        return true;
    }

    public List<CartResponse> getCart(
            String userId,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

        if (userOpt.isEmpty()) {
            return List.of();
        }

        return cartRepository.findByUserId(Long.valueOf(userId))
                .stream()
                .filter(cart -> keyword == null || keyword.isBlank()
                        || cart.getProduct().getName().toLowerCase().contains(keyword.toLowerCase())
                        || cart.getProduct().getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .filter(cart -> category == null || category.isBlank()
                        || cart.getProduct().getCategory().equalsIgnoreCase(category))
                .filter(cart -> minPrice == null
                        || cart.getProduct().getPrice().compareTo(minPrice) >= 0)
                .filter(cart -> maxPrice == null
                        || cart.getProduct().getPrice().compareTo(maxPrice) <= 0)
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
    }

    private CartResponse mapToCartResponse(CartItem cartItem) {

        CartResponse res = new CartResponse();

        res.setProductId(cartItem.getProduct().getId());
        res.setProductName(cartItem.getProduct().getName());
        res.setImageUrl(cartItem.getProduct().getImageUrl());

        res.setQuantity(cartItem.getQuantity());

        res.setUnitPrice(cartItem.getProduct().getPrice());
        res.setTotalPrice(cartItem.getPrice());

        return res;
    }

}