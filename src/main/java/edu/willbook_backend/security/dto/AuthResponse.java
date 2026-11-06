package edu.willbook_backend.security.dto;

public record AuthResponse(
        String token,
        String message
) {}