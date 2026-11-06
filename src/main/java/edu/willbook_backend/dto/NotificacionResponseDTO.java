package edu.willbook_backend.dto;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
        Long id,
        String mensaje,
        Boolean leida,
        LocalDateTime fechaCreacion
) {}