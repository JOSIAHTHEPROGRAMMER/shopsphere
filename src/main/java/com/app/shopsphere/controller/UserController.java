package com.app.shopsphere.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.shopsphere.dto.UserRequest;
import com.app.shopsphere.dto.UserResponse;
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {

        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only view your own account");
        }

        Optional<UserResponse> userOpt = userService.getUserById(id);

        return userOpt.isPresent()
                ? ResponseEntity.ok(userOpt.get())
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest user) {

        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own account");
        }

        boolean updated = userService.updateUser(id, user);

        return updated
                ? ResponseEntity.ok("User updated successfully")
                : ResponseEntity.badRequest().body("Failed to update user");
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequest userReq) {

        boolean created = userService.registerUser(userReq);

        return created
                ? ResponseEntity.ok("User created successfully")
                : ResponseEntity.badRequest().body("Failed to create user");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        if (!isSelfOrAdmin(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own account");
        }

        boolean deleted = userService.deleteUserById(id);

        return deleted
                ? ResponseEntity.ok("User deleted successfully")
                : ResponseEntity.badRequest().body("Failed to delete user");
    }

    private boolean isSelfOrAdmin(Long targetUserId) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        return currentUserId.equals(targetUserId) || SecurityUtil.hasRole("ADMIN");
    }
}