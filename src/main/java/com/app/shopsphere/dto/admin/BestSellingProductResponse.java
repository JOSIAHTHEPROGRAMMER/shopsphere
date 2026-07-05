package com.app.shopsphere.dto.admin;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BestSellingProductResponse {

    private String productId;
    private String productName;
    private Integer unitsSold;
    private BigDecimal revenue;
}