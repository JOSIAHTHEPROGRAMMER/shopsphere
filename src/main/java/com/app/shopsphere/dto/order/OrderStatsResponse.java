package com.app.shopsphere.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderStatsResponse {

    private Integer ordersPlaced;
    private BigDecimal moneySpent;
    private BigDecimal averageOrder;
    private Integer cancelledOrders;
    private LocalDateTime lastOrderDate;
}