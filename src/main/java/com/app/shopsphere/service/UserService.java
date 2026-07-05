package com.app.shopsphere.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.user.AddressDTO;
import com.app.shopsphere.dto.user.UserRequest;
import com.app.shopsphere.dto.user.UserResponse;
import com.app.shopsphere.model.Address;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean registerUser(UserRequest userReq) {

        User user = new User();
        updateUserFromRequest(user, userReq);

        if (user.getFirstName() == null || user.getFirstName().isBlank() ||
                user.getLastName() == null || user.getLastName().isBlank() ||

                user.getEmail() == null || user.getEmail().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            return false;
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return false;
        }

        if (userRepository.existsByFirstNameAndLastName(user.getFirstName(), user.getLastName())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return true;
    }

    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse);
    }

    public boolean updateUser(Long id, UserRequest updatedUserReq) {

        return userRepository.findById(id)
                .map(user -> {
                    updateUserFromRequest(user, updatedUserReq);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private void updateUserFromRequest(User user, UserRequest userReq) {
        user.setFirstName(userReq.getFirstName());
        user.setLastName(userReq.getLastName());
        user.setEmail(userReq.getEmail());
        user.setPhoneNumber(userReq.getPhoneNumber());

        if (userReq.getPassword() != null && !userReq.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userReq.getPassword()));
        }

        if (userReq.getAddress() != null) {
            Address address = user.getAddress();

            if (address == null) {
                address = new Address();
            }

            address.setStreet(userReq.getAddress().getStreet());
            address.setCity(userReq.getAddress().getCity());
            address.setState(userReq.getAddress().getState());
            address.setCountry(userReq.getAddress().getCountry());
            address.setZipCode(userReq.getAddress().getZipCode());

            user.setAddress(address);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse res = new UserResponse();

        res.setId(String.valueOf(user.getId()));
        res.setFirstName(user.getFirstName());
        res.setLastName(user.getLastName());
        res.setEmail(user.getEmail());
        res.setPhoneNumber(user.getPhoneNumber());
        res.setRole(user.getRole());

        Address address = user.getAddress();

        if (address != null) {
            AddressDTO addressDTO = new AddressDTO();

            addressDTO.setStreet(address.getStreet());
            addressDTO.setCity(address.getCity());
            addressDTO.setState(address.getState());
            addressDTO.setCountry(address.getCountry());
            addressDTO.setZipCode(address.getZipCode());

            res.setAddress(addressDTO);
        }

        return res;
    }
}
