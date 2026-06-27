package com.app.shopsphere.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.app.shopsphere.model.Product;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public void seedUsers() throws IOException {

        ClassPathResource resource = new ClassPathResource("seed/users.json");

        List<User> users = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<User>>() {
                });

        userRepository.saveAll(users);
    }

    public void seedProducts() throws IOException {

        ClassPathResource resource = new ClassPathResource("seed/products.json");

        List<Product> products = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Product>>() {
                });

        productRepository.saveAll(products);
    }
}