package edu.willbook_backend.dto;

public record LibroResponseDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        String editorial,
        Integer anioPublicacion,
        String categoria,
        String portadaUrl,
        String descripcion,
        Boolean disponible
) {}