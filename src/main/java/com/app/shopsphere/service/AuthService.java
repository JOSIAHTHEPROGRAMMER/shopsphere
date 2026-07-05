package com.app.shopsphere.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.user.LoginRequest;
import com.app.shopsphere.dto.user.LoginResponse;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Optional<LoginResponse> login(LoginRequest loginReq) {

        Optional<User> userOpt = userRepository.findByEmail(loginReq.getEmail());

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            return Optional.empty();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        return Optional.of(new LoginResponse(token, String.valueOf(user.getId()), user.getRole()));
    }
}