package com.app.shopsphere.dto;

import com.app.shopsphere.enum_values.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    private OrderStatus status;
}