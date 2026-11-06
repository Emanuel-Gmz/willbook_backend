package edu.willbook_backend.dto;

import jakarta.validation.constraints.Size;

public record MultaCancelacionDTO(
        @Size(max = 250, message = "El motivo de cancelación no puede superar los 250 caracteres")
        String motivoCancelacion
) {}