package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.request.*;
import ro.unibuc.prodeng.response.*;
import ro.unibuc.prodeng.service.AuthService;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegister_withValidRequest_returnsAuthResponse() throws Exception {
        // ARRANGE
        RegisterRequest request = new RegisterRequest("testUser","test@example.com","password123","USER");
        AuthResponse response = new AuthResponse("token123", "testUser");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    void testLogin_withValidRequest_returnsAuthResponse() throws Exception {
        // ARRANGE
        LoginRequest request = new LoginRequest("test@example.com", "password");
        AuthResponse response = new AuthResponse("token123", "testUser");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testGetCurrentUser_withPrincipal_returnsUserProfile() throws Exception {
        // ARRANGE
        UserProfileResponse response = new UserProfileResponse("1" , "testUser", "test@example.com");

        when(authService.getCurrentUserProfile("test@example.com")).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(get("/api/auth/me")
                .principal(() -> "test@example.com"))
                .andExpect(status().isOk());

        verify(authService, times(1)).getCurrentUserProfile("test@example.com");
    }

    @Test
    void testChangePassword_withValidRequest_returnsSuccessMessage() throws Exception {
        // ARRANGE
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        doNothing().when(authService).changePassword(eq("test@example.com"), any(ChangePasswordRequest.class));

        // ACT & ASSERT
        mockMvc.perform(put("/api/auth/change-password")
                .principal(() -> "test@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password was changed succesfully!"));

        verify(authService, times(1)).changePassword(eq("test@example.com"), any(ChangePasswordRequest.class));
    }
}