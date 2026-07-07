package com.app.shopsphere.dto.cart;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BatchAddToCartRequest {

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<AddToCartRequest> items;
}