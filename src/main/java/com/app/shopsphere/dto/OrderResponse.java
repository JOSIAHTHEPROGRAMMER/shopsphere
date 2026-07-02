package com.app.shopsphere.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.shopsphere.enum_values.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {

    private String id;

    private String userId;

    private BigDecimal totalPrice;

    private OrderStatus status;

    private List<OrderItemResponse> items;

    private LocalDateTime createdAt;
}