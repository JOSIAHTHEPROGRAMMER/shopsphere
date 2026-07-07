package com.app.shopsphere.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.user.LoginRequest;
import com.app.shopsphere.dto.user.LoginResponse;
import com.app.shopsphere.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes authentication endpoints for signing in and obtaining a JWT.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a signed access token.
     *
     * @param loginReq the submitted credentials
     * @return the authentication response with the generated JWT
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginReq) {
        return ResponseEntity.ok(authService.login(loginReq));
    }
}