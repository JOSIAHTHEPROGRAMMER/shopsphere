package com.app.shopsphere.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String category;
    private Boolean active;
}