package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.cart.CartRequest;
import com.app.shopsphere.dto.cart.CartResponse;
import com.app.shopsphere.dto.cart.CartSummary;
import com.app.shopsphere.exception.InsufficientStockException;
import com.app.shopsphere.exception.ProductInactiveException;
import com.app.shopsphere.exception.ResourceNotFoundException;
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

    public void addToCart(String userId, CartRequest cartReq) {

        Product product = productRepository.findById(cartReq.getProductId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with id: " + cartReq.getProductId()));

        if (!product.getActive()) {
            throw new ProductInactiveException("Product is not available: " + product.getName());
        }

        if (product.getStockQuantity() < cartReq.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

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
    }

    public void deleteItemFromCart(String userId, Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found for product id: " + productId);
        }

        cartRepository.delete(cartItem);
    }

    public void clearCart(String userId) {

        userRepository.findById(Long.valueOf(userId))
                .ifPresent(user -> cartRepository.deleteByUser(user));
    }

    public void addMultipleToCart(String userId, List<CartRequest> cartRequests) {

        for (CartRequest cartRequest : cartRequests) {
            addToCart(userId, cartRequest);
        }
    }

    public CartSummary getCartSummary(String userId) {

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

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

        return summary;
    }

    public void updateCartItem(String userId, CartRequest cartReq) {

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Long productId = cartReq.getProductId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Cart item not found for product id: " + productId);
        }

        if (!product.getActive()) {
            throw new ProductInactiveException("Product is not available: " + product.getName());
        }

        if (cartReq.getQuantity() <= 0) {
            cartRepository.delete(cartItem);
            return;
        }

        if (cartReq.getQuantity() > product.getStockQuantity()) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        cartItem.setQuantity(cartReq.getQuantity());

        cartItem.setPrice(
                product.getPrice().multiply(
                        BigDecimal.valueOf(cartReq.getQuantity())));

        cartRepository.save(cartItem);
    }

    public List<CartResponse> getCart(
            String userId,
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return cartRepository.findByUserId(user.getId())
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