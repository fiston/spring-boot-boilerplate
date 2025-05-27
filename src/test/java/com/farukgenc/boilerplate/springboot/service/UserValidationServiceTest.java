package com.farukgenc.boilerplate.springboot.service;

import com.farukgenc.boilerplate.springboot.model.common.ExceptionMessageAccessor;
import com.farukgenc.boilerplate.springboot.model.exception.RegistrationException;
import com.farukgenc.boilerplate.springboot.model.payload.RegistrationRequest;
import com.farukgenc.boilerplate.springboot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExceptionMessageAccessor exceptionMessageAccessor;

    @InjectMocks
    private UserValidationService userValidationService;

    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password");
        registrationRequest.setFirstName("Test");
        registrationRequest.setLastName("User");
    }

    @Test
    void validateUser_whenUsernameAndEmailDoNotExist_shouldNotThrowException() {
        // Given
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> userValidationService.validateUser(registrationRequest));

        // Verify
        verify(userRepository, times(1)).existsByUsername(registrationRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registrationRequest.getEmail());
    }

    @Test
    void validateUser_whenUsernameAlreadyExists_shouldThrowRegistrationException() {
        // Given
        String expectedErrorMessage = "Username already exists";
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);
        // No need to mock existsByEmail as the username check comes first and will throw

        when(exceptionMessageAccessor.getMessage(null, "username_already_exists")).thenReturn(expectedErrorMessage);

        // When
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userValidationService.validateUser(registrationRequest));

        // Then
        assertEquals(expectedErrorMessage, exception.getMessage());

        // Verify
        verify(userRepository, times(1)).existsByUsername(registrationRequest.getUsername());
        verify(userRepository, never()).existsByEmail(anyString()); // Ensure email check was not performed
        verify(exceptionMessageAccessor, times(1)).getMessage(null, "username_already_exists");
    }

    @Test
    void validateUser_whenEmailAlreadyExists_shouldThrowRegistrationException() {
        // Given
        String expectedErrorMessage = "Email already exists";
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        when(exceptionMessageAccessor.getMessage(null, "email_already_exists")).thenReturn(expectedErrorMessage);

        // When
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userValidationService.validateUser(registrationRequest));

        // Then
        assertEquals(expectedErrorMessage, exception.getMessage());

        // Verify
        verify(userRepository, times(1)).existsByUsername(registrationRequest.getUsername());
        verify(userRepository, times(1)).existsByEmail(registrationRequest.getEmail());
        verify(exceptionMessageAccessor, times(1)).getMessage(null, "email_already_exists");
    }

    @Test
    void validateUser_whenBothUsernameAndEmailAlreadyExist_shouldThrowEmailExceptionFirst() {
        // Given
        String expectedEmailErrorMessage = "Email already exists";
        // The UserValidationService is expected to check email first.
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);
        // Mocking existsByUsername as true to represent the "both exist" scenario,
        // but it should not be called if the email check throws first.
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);


        when(exceptionMessageAccessor.getMessage(null, "email_already_exists")).thenReturn(expectedEmailErrorMessage);

        // When
        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userValidationService.validateUser(registrationRequest));

        // Then
        assertEquals(expectedEmailErrorMessage, exception.getMessage());

        // Verify
        verify(userRepository, times(1)).existsByEmail(registrationRequest.getEmail());
        verify(userRepository, never()).existsByUsername(registrationRequest.getUsername()); // Username check should not be reached
        verify(exceptionMessageAccessor, times(1)).getMessage(null, "email_already_exists");
        verify(exceptionMessageAccessor, never()).getMessage(null, "username_already_exists");
    }
}
