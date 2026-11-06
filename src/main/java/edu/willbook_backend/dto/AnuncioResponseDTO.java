package edu.willbook_backend.dto;

import java.time.LocalDateTime;

public record AnuncioResponseDTO(
        Long id,
        String titulo,
        String mensaje,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaExpiracion,
        String nombreAutor
) {}