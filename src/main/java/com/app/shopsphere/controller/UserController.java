package com.app.shopsphere.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.user.UserRequest;
import com.app.shopsphere.dto.user.UserResponse;
import com.app.shopsphere.exception.ForbiddenException;
import com.app.shopsphere.security.SecurityUtil;
import com.app.shopsphere.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        if (!SecurityUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("Admin access required");
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {

        if (!isSelfOrAdmin(id)) {
            throw new ForbiddenException("You can only view your own account");
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest user) {

        if (!isSelfOrAdmin(id)) {
            throw new ForbiddenException("You can only update your own account");
        }

        userService.updateUser(id, user);
        return ResponseEntity.ok("User updated successfully");
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequest userReq) {
        userService.registerUser(userReq);
        return ResponseEntity.ok("User created successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        if (!isSelfOrAdmin(id)) {
            throw new ForbiddenException("You can only delete your own account");
        }

        userService.deleteUserById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    private boolean isSelfOrAdmin(Long targetUserId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        return currentUserId.equals(targetUserId) || SecurityUtil.hasRole("ADMIN");
    }
}