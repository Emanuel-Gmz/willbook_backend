package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoEjemplar;

public record EjemplarResponseDTO(
        Long id,
        String codigoInventario,
        EstadoEjemplar estado
) {}