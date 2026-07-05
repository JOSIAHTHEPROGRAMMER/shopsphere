package com.app.shopsphere.dto;

import com.app.shopsphere.enum_values.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String userId;
    private UserRole role;
}