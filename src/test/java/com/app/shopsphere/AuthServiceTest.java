package com.app.shopsphere;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.shopsphere.dto.user.LoginRequest;
import com.app.shopsphere.dto.user.LoginResponse;
import com.app.shopsphere.enum_values.UserRole;
import com.app.shopsphere.exception.UnauthorizedException;
import com.app.shopsphere.model.User;
import com.app.shopsphere.repository.UserRepository;
import com.app.shopsphere.security.JwtUtil;
import com.app.shopsphere.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User existingUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("jane.doe@example.com");
        existingUser.setPassword("$2a$10$hashedvalue");
        existingUser.setRole(UserRole.CUSTOMER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("jane.doe@example.com");
        loginRequest.setPassword("correctPassword");
    }

    @Test
    void login_succeeds_whenCredentialsAreValid() {

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("correctPassword", "$2a$10$hashedvalue")).thenReturn(true);
        when(jwtUtil.generateToken(1L, UserRole.CUSTOMER)).thenReturn("mock.jwt.token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUserId()).isEqualTo("1");
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void login_throwsUnauthorized_whenEmailNotFound() {

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_throwsUnauthorized_whenPasswordIsWrong() {

        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedvalue")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_returnsSameErrorMessage_forMissingEmailAndWrongPassword() {

        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());

        String missingEmailMessage = catchMessage(() -> authService.login(loginRequest));

        loginRequest.setPassword("wrongPassword");
        when(userRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedvalue")).thenReturn(false);

        String wrongPasswordMessage = catchMessage(() -> authService.login(loginRequest));

        assertThat(missingEmailMessage).isEqualTo(wrongPasswordMessage);
    }

    private String catchMessage(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}