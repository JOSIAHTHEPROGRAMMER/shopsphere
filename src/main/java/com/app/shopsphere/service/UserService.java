package com.app.shopsphere.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.user.AddressDTO;
import com.app.shopsphere.dto.user.UserRequest;
import com.app.shopsphere.dto.user.UserResponse;
import com.app.shopsphere.exception.BadRequestException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.Address;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.security.LogMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages account registration, profile updates, and user lookups for the
 * storefront.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user account after validating the required profile fields and
     * uniqueness rules.
     *
     * @param userReq the incoming registration payload
     * @throws BadRequestException when required data is missing or duplicates are
     *                             found
     */
    public void registerUser(UserRequest userReq) {

        User user = new User();
        updateUserFromRequest(user, userReq);

        if (user.getFirstName() == null || user.getFirstName().isBlank() ||
                user.getLastName() == null || user.getLastName().isBlank() ||
                user.getEmail() == null || user.getEmail().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BadRequestException("First name, last name, email, and password are required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        if (userRepository.existsByFirstNameAndLastName(user.getFirstName(), user.getLastName())) {
            throw new BadRequestException("An account with this name already exists");
        }

        userRepository.save(user);

        log.info("User registered: {} (id: {})", LogMaskUtil.maskEmail(user.getEmail()), user.getId());
    }

    /**
     * Returns a user profile DTO for the requested identifier.
     *
     * @param id the user identifier
     * @return the public-facing user profile data
     * @throws ResourceNotFoundException when the account does not exist
     */
    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Updates an existing user profile and preserves the password unless a new one
     * is supplied.
     *
     * @param id             the user identifier to update
     * @param updatedUserReq the incoming profile changes
     * @throws ResourceNotFoundException when the account does not exist
     */
    public void updateUser(Long id, UserRequest updatedUserReq) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        updateUserFromRequest(user, updatedUserReq);
        userRepository.save(user);

        log.info("User updated: {} (id: {})", LogMaskUtil.maskEmail(user.getEmail()), user.getId());
    }

    /**
     * Removes an account from the system.
     *
     * @param id the user identifier
     * @throws ResourceNotFoundException when the account does not exist
     */
    public void deleteUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);

        log.warn("User deleted: {} (id: {})", LogMaskUtil.maskEmail(user.getEmail()), id);
    }

    /**
     * Returns every registered user as a response DTO.
     *
     * @return the full user list
     */
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