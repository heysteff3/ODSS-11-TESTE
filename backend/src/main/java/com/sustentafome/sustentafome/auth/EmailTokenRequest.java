package com.sustentafome.sustentafome.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailTokenRequest(@NotBlank @Email String email) {}
