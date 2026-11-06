package edu.willbook_backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "El email es obligatorio")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}