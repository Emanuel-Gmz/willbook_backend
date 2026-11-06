package edu.willbook_backend.dto;

public record AutorResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String biografia,
        String paisOrigen
) {}