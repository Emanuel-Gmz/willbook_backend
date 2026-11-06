package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoEjemplar;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EjemplarUpdateDTO(
        @NotNull(message = "El ID del ejemplar es obligatorio para su edición")
        Long id,

        @Size(max = 50, message = "El código de inventario no puede superar los 50 caracteres")
        String codigoInventario,

        @NotNull(message = "El estado del ejemplar es obligatorio")
        EstadoEjemplar estado
) {}