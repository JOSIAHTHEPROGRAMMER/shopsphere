package com.app.shopsphere.dto.order;

import com.app.shopsphere.enum_values.OrderStatus;

import lombok.Data;

@Data
public class OrderStatusUpdateRequest {

    private OrderStatus status;
}