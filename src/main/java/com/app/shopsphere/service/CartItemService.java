package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.cart.AddToCartRequest;
import com.app.shopsphere.dto.cart.CartResponse;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles cart lifecycle operations such as adding items, updating quantities,
 * and summarizing cart contents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {
        private final ProductRepository productRepository;
        private final CartItemRepository cartRepository;
        private final UserRepository userRepository;
        private static final BigDecimal TAX_RATE = new BigDecimal("0.07");

        /**
         * Adds one or more units of a product to the authenticated user's cart.
         *
         * @param userId  the current user identifier
         * @param cartReq the requested product and quantity
         * @throws ResourceNotFoundException  when the user or product does not exist
         * @throws ProductInactiveException   when the product is not available for sale
         * @throws InsufficientStockException when the requested quantity exceeds
         *                                    inventory
         */
        public void addToCart(String userId, AddToCartRequest cartReq) {

                Product product = productRepository.findById(cartReq.getProductId())
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Product not found with id: "
                                                                + cartReq.getProductId()));

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
                        prevCartItem.setPrice(
                                        product.getPrice().multiply(BigDecimal.valueOf(prevCartItem.getQuantity())));

                        cartRepository.save(prevCartItem);

                        log.info("Cart item updated for user {}: product {} quantity now {}",
                                        userId, product.getId(), prevCartItem.getQuantity());

                } else {

                        CartItem cart = new CartItem();

                        cart.setUser(user);
                        cart.setProduct(product);
                        cart.setQuantity(cartReq.getQuantity());
                        cart.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartReq.getQuantity())));

                        cartRepository.save(cart);

                        log.info("Product {} added to cart for user {}: quantity {}",
                                        product.getId(), userId, cartReq.getQuantity());
                }
        }

        /**
         * Removes a single product from the user's cart.
         *
         * @param userId    the current user identifier
         * @param productId the product to remove
         * @throws ResourceNotFoundException when the user, product, or cart entry is
         *                                   missing
         */
        public void deleteItemFromCart(String userId, Long productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                User user = userRepository.findById(Long.valueOf(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

                if (cartItem == null) {
                        throw new ResourceNotFoundException("Cart item not found for product id: " + productId);
                }

                cartRepository.delete(cartItem);

                log.info("Product {} removed from cart for user {}", productId, userId);
        }

        /**
         * Removes every cart item for the specified user.
         *
         * @param userId the current user identifier
         */
        public void clearCart(String userId) {

                userRepository.findById(Long.valueOf(userId))
                                .ifPresent(user -> {
                                        cartRepository.deleteByUser(user);
                                        log.info("Cart cleared for user {}", userId);
                                });
        }

        /**
         * Adds several products to the cart in sequence.
         *
         * @param userId       the current user identifier
         * @param cartRequests the list of requested cart additions
         */
        public void addMultipleToCart(String userId, List<AddToCartRequest> cartRequests) {

                for (AddToCartRequest cartRequest : cartRequests) {
                        addToCart(userId, cartRequest);
                }
        }

        /**
         * Builds a summary of the user's cart including totals and estimated tax.
         *
         * @param userId the current user identifier
         * @return the calculated cart summary
         * @throws ResourceNotFoundException when the user cannot be resolved
         */
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

        /**
         * Updates the quantity of an existing cart entry or removes it when the
         * quantity reaches zero.
         *
         * @param userId  the current user identifier
         * @param cartReq the new quantity and product identifier
         * @throws ResourceNotFoundException  when the user, product, or cart entry is
         *                                    missing
         * @throws ProductInactiveException   when the product is no longer available
         * @throws InsufficientStockException when the requested quantity exceeds
         *                                    inventory
         */
        public void updateCartItem(String userId, UpdateCartRequest cartReq) {

                User user = userRepository.findById(Long.valueOf(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                Long productId = cartReq.getProductId();

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                CartItem cartItem = cartRepository.findByUserAndProduct(user, product);

                if (cartItem == null) {
                        throw new ResourceNotFoundException("Cart item not found for product id: " + productId);
                }

                if (!product.getActive()) {
                        throw new ProductInactiveException("Product is not available: " + product.getName());
                }

                if (cartReq.getQuantity() <= 0) {
                        cartRepository.delete(cartItem);
                        log.info("Cart item deleted via zero-quantity update for user {}: product {}", userId,
                                        productId);
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

                log.info("Cart item quantity updated for user {}: product {} now {}", userId, productId,
                                cartReq.getQuantity());
        }

        /**
         * Returns the current cart contents filtered by the supplied product criteria.
         *
         * @param userId   the current user identifier
         * @param keyword  optional text filter
         * @param category optional category filter
         * @param minPrice optional minimum price filter
         * @param maxPrice optional maximum price filter
         * @return the cart entries that match the provided filters
         * @throws ResourceNotFoundException when the user cannot be resolved
         */
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
                                                || cart.getProduct().getName().toLowerCase()
                                                                .contains(keyword.toLowerCase())
                                                || cart.getProduct().getDescription().toLowerCase()
                                                                .contains(keyword.toLowerCase()))
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