package edu.willbook_backend.dto;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequestDTO(
        @NotNull(message = "El ID del libro es obligatorio")
        Long libroId
) {}