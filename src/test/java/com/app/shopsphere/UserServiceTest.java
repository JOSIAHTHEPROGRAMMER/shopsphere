package com.app.shopsphere;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.shopsphere.dto.user.UserRequest;
import com.app.shopsphere.dto.user.UserResponse;
import com.app.shopsphere.exception.BadRequestException;
import com.app.shopsphere.exception.ResourceNotFoundException;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest validRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {

        validRequest = new UserRequest();
        validRequest.setFirstName("Jane");
        validRequest.setLastName("Doe");
        validRequest.setEmail("jane.doe@example.com");
        validRequest.setPassword("plaintextPassword");
        validRequest.setPhoneNumber("1234567890");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword("$2a$10$hashedvalue");
    }

    @Test
    void registerUser_savesSuccessfully_whenValid() {

        when(passwordEncoder.encode("plaintextPassword")).thenReturn("$2a$10$encodedhash");
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userRepository.existsByFirstNameAndLastName("Jane", "Doe")).thenReturn(false);

        userService.registerUser(validRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_throwsBadRequest_whenEmailMissing() {

        validRequest.setEmail("");

        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_throwsBadRequest_whenEmailAlreadyExists() {

        when(passwordEncoder.encode("plaintextPassword")).thenReturn("$2a$10$encodedhash");
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_throwsBadRequest_whenNameComboAlreadyExists() {

        when(passwordEncoder.encode("plaintextPassword")).thenReturn("$2a$10$encodedhash");
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);
        when(userRepository.existsByFirstNameAndLastName("Jane", "Doe")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("name");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_returnsUser_whenFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getEmail()).isEqualTo("existing@example.com");
    }

    @Test
    void getUserById_throwsResourceNotFound_whenMissing() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_updatesFieldsWithoutChangingPassword_whenPasswordOmitted() {

        UserRequest updateReq = new UserRequest();
        updateReq.setFirstName("Updated");
        updateReq.setLastName("Name");
        updateReq.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.updateUser(1L, updateReq);

        assertThat(existingUser.getFirstName()).isEqualTo("Updated");
        assertThat(existingUser.getPassword()).isEqualTo("$2a$10$hashedvalue");
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_reHashesPassword_whenNewPasswordProvided() {

        UserRequest updateReq = new UserRequest();
        updateReq.setFirstName("Existing");
        updateReq.setLastName("User");
        updateReq.setEmail("existing@example.com");
        updateReq.setPassword("newPlaintextPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPlaintextPassword")).thenReturn("$2a$10$newhash");

        userService.updateUser(1L, updateReq);

        assertThat(existingUser.getPassword()).isEqualTo("$2a$10$newhash");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_throwsResourceNotFound_whenUserMissing() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, validRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserById_deletesSuccessfully_whenUserExists() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        userService.deleteUserById(1L);

        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    void deleteUserById_throwsResourceNotFound_whenUserMissing() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getAllUsers_returnsAllMappedUsers() {

        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("existing@example.com");
    }
}