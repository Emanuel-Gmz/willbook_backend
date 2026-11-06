package edu.willbook_backend.dto;

public record LibroResponseDTO(
        Long id,
        String titulo,
        String autor,
        String isbn,
        String descripcion,
        Boolean disponible
) {}