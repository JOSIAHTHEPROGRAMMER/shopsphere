package com.app.shopsphere;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController

{

    private final List<User> userList = new ArrayList<>();

    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        // Logic to retrieve all users from the database
        return userList;
    }

    @PostMapping("/api/users")
    public User createUser(@RequestBody User user) {
        // Logic to create a new user in the database
        userList.add(user);
        return user;
    }
}
