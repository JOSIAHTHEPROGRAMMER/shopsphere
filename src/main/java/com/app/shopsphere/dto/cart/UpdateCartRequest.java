package com.app.shopsphere.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}