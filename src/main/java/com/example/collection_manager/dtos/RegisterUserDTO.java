package com.example.collection_manager.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDTO(

        @NotBlank
        @Size(min = 3, max = 30)
        String userName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}

