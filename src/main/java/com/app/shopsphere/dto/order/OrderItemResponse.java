package com.app.shopsphere.dto.order;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemResponse {

    private Long id;

    private String productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private String imageUrl;
}