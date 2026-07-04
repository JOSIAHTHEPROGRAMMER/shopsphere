package com.app.shopsphere.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.app.shopsphere.enum_values.UserRole;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;

    public boolean isAdmin(Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);

        return userOpt.isPresent() && userOpt.get().getRole() == UserRole.ADMIN;
    }
}