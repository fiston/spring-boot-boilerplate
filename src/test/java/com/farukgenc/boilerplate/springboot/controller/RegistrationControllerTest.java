package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.model.payload.RegistrationRequest;
import com.farukgenc.boilerplate.springboot.model.response.RegistrationResponse;
import com.farukgenc.boilerplate.springboot.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(RegistrationController.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegistrationRequest();
        validRequest.setUsername("testuser");
        validRequest.setEmail("test@example.com");
        validRequest.setPassword("password123");
        validRequest.setFirstName("Test");
        validRequest.setLastName("User");
    }

    @Test
    void registrationRequest_whenValidRequest_shouldReturnCreated() throws Exception {
        // Given
        RegistrationResponse expectedResponse = new RegistrationResponse(
                true,
                "User registered successfully",
                "testuser",
                "test@example.com",
                "Test",
                "User"
        );

        when(userService.registration(any(RegistrationRequest.class))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("User registered successfully"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("testuser"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"));

        // Verify
        verify(userService, times(1)).registration(any(RegistrationRequest.class));
    }

    @Test
    void registrationRequest_whenUsernameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername(""); // Blank username
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(userService, never()).registration(any(RegistrationRequest.class));
    }

    @Test
    void registrationRequest_whenEmailIsInvalid_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("invalid-email"); // Invalid email format
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(userService, never()).registration(any(RegistrationRequest.class));
    }

    @Test
    void registrationRequest_whenPasswordIsTooShort_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("123"); // Assuming password needs to be longer (e.g. @Size(min=6))
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName("User");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(userService, never()).registration(any(RegistrationRequest.class));
    }
    
    @Test
    void registrationRequest_whenFirstNameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName(""); // Blank first name
        invalidRequest.setLastName("User");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(userService, never()).registration(any(RegistrationRequest.class));
    }

    @Test
    void registrationRequest_whenLastNameIsBlank_shouldReturnBadRequest() throws Exception {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest();
        invalidRequest.setUsername("testuser");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setFirstName("Test");
        invalidRequest.setLastName(""); // Blank last name

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // Verify
        verify(userService, never()).registration(any(RegistrationRequest.class));
    }
}
