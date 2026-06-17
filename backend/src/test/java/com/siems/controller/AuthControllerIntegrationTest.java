package com.siems.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siems.dto.auth.LoginRequest;
import com.siems.dto.auth.RefreshTokenRequest;
import com.siems.dto.auth.RegisterRequest;
import com.siems.entity.Role;
import com.siems.repository.RoleRepository;
import com.siems.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        for (String roleName : new String[]{"ADMIN", "IMPORT_MANAGER", "EXPORT_MANAGER", "INVENTORY_MANAGER"}) {
            roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));
        }
    }

    @Test
    @DisplayName("POST /auth/register should create user and return token pair")
    void shouldRegisterUserAndReturnTokens() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser1");
        request.setEmail("newuser1@siems.com");
        request.setPassword("SecurePass123");
        request.setRoleName("INVENTORY_MANAGER");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()))
                .andExpect(jsonPath("$.data.username", is("newuser1")))
                .andExpect(jsonPath("$.data.role", is("INVENTORY_MANAGER")));
    }

    @Test
    @DisplayName("POST /auth/register should return 409 for duplicate username")
    void shouldReturn409ForDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicateuser");
        request.setEmail("first@siems.com");
        request.setPassword("SecurePass123");
        request.setRoleName("ADMIN");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        request.setEmail("second@siems.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Username already taken")));
    }

    @Test
    @DisplayName("POST /auth/login should succeed with correct credentials")
    void shouldLoginWithCorrectCredentials() throws Exception {
        registerUser("loginuser", "loginuser@siems.com", "CorrectPass123", "EXPORT_MANAGER");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("CorrectPass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.role", is("EXPORT_MANAGER")));
    }

    @Test
    @DisplayName("POST /auth/login should return 401 for incorrect password")
    void shouldReturn401ForIncorrectPassword() throws Exception {
        registerUser("loginuser2", "loginuser2@siems.com", "CorrectPass123", "ADMIN");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser2");
        loginRequest.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/refresh should issue new token pair and invalidate old refresh token")
    void shouldRefreshTokenAndRotate() throws Exception {
        String registerResponse = registerUser("refreshuser", "refreshuser@siems.com", "RefreshPass123", "ADMIN");
        String oldRefreshToken = objectMapper.readTree(registerResponse).path("data").path("refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(oldRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.refreshToken", notNullValue()));

        // Reusing the OLD refresh token should now fail (rotation)
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    private String registerUser(String username, String email, String password, String role) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setRoleName(role);

        return mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
