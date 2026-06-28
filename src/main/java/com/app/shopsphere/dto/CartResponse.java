package com.app.shopsphere.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartResponse {

    private Long productId;
    private String productName;
    private String imageUrl;

    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}