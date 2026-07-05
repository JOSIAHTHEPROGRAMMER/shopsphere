package com.app.shopsphere.dto.cart;

import lombok.Data;

@Data
public class CartRequest {
    private Long productId;
    private Integer quantity;
}
