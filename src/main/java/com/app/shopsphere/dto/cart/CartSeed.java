package com.app.shopsphere.dto.cart;

import java.util.List;

import lombok.Data;

@Data
public class CartSeed {

    private Long userId;

    private List<CartSeedItem> items;
}