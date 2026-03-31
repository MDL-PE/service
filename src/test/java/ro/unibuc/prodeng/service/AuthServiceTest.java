package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.*;
import ro.unibuc.prodeng.response.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity("1", "testUser", "test@example.com", "encodedPass", "ROLE_USER");
    }

    @Test
    void testRegister_validUser_createsUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("testUser", "test@example.com", "password", "USER");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("token123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("token123", response.token());
        assertEquals("testUser", response.username());

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testRegister_emailAlreadyExists_throwsException() {
        RegisterRequest request = new RegisterRequest("testUser", "test@example.com", "password", "USER");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void testRegister_usernameAlreadyExists_throwsException() {
        RegisterRequest request = new RegisterRequest("testUser", "test@example.com", "password", "USER");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void testRegister_adminRole_assignsAdminRole() {
        RegisterRequest request = new RegisterRequest("admin", "admin@example.com", "password", "ADMIN");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("token");

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.role().equals("ROLE_ADMIN")
        ));
    }

    @Test
    void testRegister_nullRole_assignsUserRole() {
        RegisterRequest request = new RegisterRequest("user", "user@test.com", "pass", null);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.role().equals("ROLE_USER")
        ));
    }

    @Test
    void testLogin_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("token123");

        AuthResponse response = authService.login(request);

        assertEquals("token123", response.token());
    }

    @Test
    void testLogin_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void testGetCurrentUserProfile_userExists_returnsProfile() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserProfileResponse response = authService.getCurrentUserProfile("test@example.com");

        assertEquals("testUser", response.username());
        assertEquals("test@example.com", response.email());
    }

    @Test
    void testGetCurrentUserProfile_userNotFound_throwsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authService.getCurrentUserProfile("test@example.com"));
    }

    @Test
    void testChangePassword_validOldPassword_updatesPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");

        authService.changePassword("test@example.com", request);

        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void testChangePassword_wrongOldPassword_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrong", "newPass");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("test@example.com", request));
    }
}