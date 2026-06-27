package com.app.shopsphere.dto;

import com.app.shopsphere.enum_values.UserRole;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String phoneNumber;
    private AddressDTO address;

}
