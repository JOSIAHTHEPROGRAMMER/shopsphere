package com.app.shopsphere.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.user.LoginRequest;
import com.app.shopsphere.dto.user.LoginResponse;
import com.app.shopsphere.exception.UnauthorizedException;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.security.JwtUtil;
import com.app.shopsphere.security.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Authenticates users and issues signed JWTs for the protected API surface.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Validates the supplied credentials and returns a signed authentication token.
     *
     * @param loginReq the submitted email and password values
     * @return a response containing the JWT and the authenticated user identifier
     * @throws UnauthorizedException when the credentials do not match an existing
     *                               account
     */
    public LoginResponse login(LoginRequest loginReq) {

        User user = userRepository.findByEmail(loginReq.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: no account for email {}", LogMaskUtil.maskEmail(loginReq.getEmail()));
                    return new UnauthorizedException("Invalid email or password");
                });

        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            log.warn("Login failed: incorrect password for email {}", LogMaskUtil.maskEmail(loginReq.getEmail()));
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        log.info("User {} logged in successfully (role: {})", user.getId(), user.getRole());

        return new LoginResponse(token, String.valueOf(user.getId()), user.getRole());
    }
}