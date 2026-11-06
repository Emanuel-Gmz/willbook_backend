package edu.willbook_backend.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OlvidoPasswordRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Debe ser un correo electrónico válido")
        String email
) {}