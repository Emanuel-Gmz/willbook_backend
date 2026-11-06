package edu.willbook_backend.dto;

public record UsuarioBusquedaResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String dni,
        String email,
        Boolean activo
) {}