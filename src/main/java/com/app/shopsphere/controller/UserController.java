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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes user account endpoints for profile management and account visibility.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Returns the full user list for administrators.
     *
     * @return the list of registered users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        if (!SecurityUtil.hasRole("ADMIN")) {
            throw new ForbiddenException("Admin access required");
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Returns a user profile when the caller is the owner or an administrator.
     *
     * @param id the profile identifier
     * @return the requested user profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {

        if (!isSelfOrAdmin(id)) {
            throw new ForbiddenException("You can only view your own account");
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Updates a user profile when the caller is the owner or an administrator.
     *
     * @param id   the profile identifier
     * @param user the updated user payload
     * @return a confirmation message
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequest user) {

        if (!isSelfOrAdmin(id)) {
            throw new ForbiddenException("You can only update your own account");
        }

        userService.updateUser(id, user);
        return ResponseEntity.ok("User updated successfully");
    }

    /**
     * Registers a new user account through the public signup flow.
     *
     * @param userReq the registration payload
     * @return a confirmation message
     */
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody @Valid UserRequest userReq) {
        userService.registerUser(userReq);
        return ResponseEntity.ok("User created successfully");
    }

    /**
     * Deletes a user account when the caller is the owner or an administrator.
     *
     * @param id the profile identifier
     * @return a confirmation message
     */
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