package com.app.shopsphere.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AdminRevenueResponse {

    private BigDecimal totalRevenue;
    private int orderCount;
    private BigDecimal averageOrderValue;
    private LocalDateTime from;
    private LocalDateTime to;
}