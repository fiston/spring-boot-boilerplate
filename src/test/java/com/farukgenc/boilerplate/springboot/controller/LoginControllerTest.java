package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.model.enums.Role;
import com.farukgenc.boilerplate.springboot.model.payload.LoginRequest;
import com.farukgenc.boilerplate.springboot.model.response.LoginResponse;
import com.farukgenc.boilerplate.springboot.security.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void loginRequest_whenValidRequest_shouldReturnLoginResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse(
                "dummy-token",
                "testuser",
                Set.of(Role.USER)
        );

        when(jwtTokenService.getLoginResponse(any(LoginRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // Print request and response for debugging
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(expectedResponse.getToken()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(expectedResponse.getUsername()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0]").value(Role.USER.toString())); // Assuming one role for simplicity

        // Verify
        verify(jwtTokenService, times(1)).getLoginResponse(any(LoginRequest.class));
    }

    @Test
    void loginRequest_whenUsernameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(""); // Blank username
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(jwtTokenService, never()).getLoginResponse(any(LoginRequest.class));
    }

    @Test
    void loginRequest_whenPasswordIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword(""); // Blank password

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(jwtTokenService, never()).getLoginResponse(any(LoginRequest.class));
    }

    @Test
    void loginRequest_whenUsernameIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(null); // Null username
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(jwtTokenService, never()).getLoginResponse(any(LoginRequest.class));
    }

    @Test
    void loginRequest_whenPasswordIsNull_shouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword(null); // Null password

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(jwtTokenService, never()).getLoginResponse(any(LoginRequest.class));
    }
}
