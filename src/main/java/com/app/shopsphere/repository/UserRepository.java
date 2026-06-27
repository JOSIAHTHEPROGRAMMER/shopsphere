package com.app.shopsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.shopsphere.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
