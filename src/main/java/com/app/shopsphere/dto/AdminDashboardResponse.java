package com.app.shopsphere.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal revenue;
    private long productsInStock;
    private long outOfStock;
    private long lowStock;
}