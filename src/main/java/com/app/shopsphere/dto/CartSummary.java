package com.app.shopsphere.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartSummary {

    private Integer totalItems;
    private Integer totalUniqueItems;
    private BigDecimal subtotal;
    private BigDecimal estimatedTax;
    private BigDecimal total;
}