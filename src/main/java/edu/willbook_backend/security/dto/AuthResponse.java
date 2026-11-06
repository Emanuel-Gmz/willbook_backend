package edu.willbook_backend.security.dto;

import edu.willbook_backend.enums.Rol;

public record AuthResponse(
        String token,
        String message,
        Rol rol,
        String nombreCompleto
) {
}