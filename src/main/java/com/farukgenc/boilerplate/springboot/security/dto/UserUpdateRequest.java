package com.farukgenc.boilerplate.springboot.security.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class UserUpdateRequest {
    @NotEmpty(message = "Email may not be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    // Add other fields that can be updated if necessary
    // For example, name, etc. but keep it simple for now.
}
