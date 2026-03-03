package com.sustentafome.sustentafome.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 4, max = 30) String username,
        @NotBlank @Email String email,
        @Size(max = 30) String phone,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$",
                message = "Senha inválida"
        )
        String password,
        @NotBlank String confirmPassword,
        @NotBlank String verificationCode
) {}
