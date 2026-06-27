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

import com.app.shopsphere.dto.UserRequest;
import com.app.shopsphere.dto.UserResponse;
import com.app.shopsphere.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController

{
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();

        return (users != null)
                ? ResponseEntity.ok(users)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody UserRequest user) {

        boolean updated = userService.updateUser(id, user);

        return (updated)
                ? ResponseEntity.ok("User updated successfully")
                : ResponseEntity.badRequest().body("Failed to update user");

    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserRequest userReq) {

        boolean created = userService.registerUser(userReq);

        return (created)
                ? ResponseEntity.ok("User created successfully")
                : ResponseEntity.badRequest().body("Failed to create user");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        boolean deleted = userService.deleteUserById(id);

        return (deleted)
                ? ResponseEntity.ok("User deleted successfully")
                : ResponseEntity.badRequest().body("Failed to delete user");
    }

}
