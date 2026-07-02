package com.app.shopsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.shopsphere.model.CartItem;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    CartItem findByUserAndProduct(User user, Product product);

    CartItem findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUser(User user);

    void deleteByUserAndProduct(User user, Product product);
}