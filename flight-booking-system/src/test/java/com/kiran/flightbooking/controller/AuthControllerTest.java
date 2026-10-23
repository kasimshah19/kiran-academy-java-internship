package com.kiran.flightbooking.controller;

import com.kiran.flightbooking.dto.request.LoginRequest;
import com.kiran.flightbooking.dto.request.RegisterRequest;
import com.kiran.flightbooking.dto.response.AuthResponse;
import com.kiran.flightbooking.security.CustomUserDetailsService;
import com.kiran.flightbooking.security.JwtTokenProvider;
import com.kiran.flightbooking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController MockMvc Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/v1/auth/register creates user and returns 201 Created")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Rahul Sharma");
        request.setEmail("rahul@example.com");
        request.setPassword("SecurePass123");

        AuthResponse authResponse = new AuthResponse(
                "mock-jwt-token-xyz", "Bearer", 1L, "rahul@example.com", "Rahul Sharma", "PASSENGER"
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-xyz"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login authenticates and returns 200 OK")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("rahul@example.com");
        request.setPassword("SecurePass123");

        AuthResponse authResponse = new AuthResponse(
                "mock-jwt-token-xyz", "Bearer", 1L, "rahul@example.com", "Rahul Sharma", "PASSENGER"
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-xyz"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid email returns 400 Bad Request")
    void testRegisterInvalidEmailReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Rahul Sharma");
        request.setEmail("invalid-email-format"); // invalid
        request.setPassword("SecurePass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
