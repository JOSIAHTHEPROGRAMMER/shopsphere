package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.CartRequest;
import com.app.shopsphere.dto.CartResponse;
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

    public boolean addToCart(String userId, CartRequest cartReq) {

        Optional<Product> productOpt = productRepository.findById(cartReq.getProductId());

        if (productOpt.isEmpty()) {
            return false;
        }

        Product product = productOpt.get();

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