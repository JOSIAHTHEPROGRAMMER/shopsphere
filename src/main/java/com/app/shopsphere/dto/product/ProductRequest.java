package com.app.shopsphere.dto.product;

import java.math.BigDecimal;

import lombok.Data;

@Data

public class ProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String category;
    private Boolean active;
}