package edu.willbook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EjemplarCreateDTO(
        @NotBlank(message = "El código de inventario es obligatorio")
        @Size(max = 50, message = "El código de inventario no puede superar los 50 caracteres")
        String codigoInventario
) {}