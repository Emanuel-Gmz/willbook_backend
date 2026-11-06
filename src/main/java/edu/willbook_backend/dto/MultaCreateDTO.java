package edu.willbook_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MultaCreateDTO(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long usuarioId,

        @NotBlank(message = "El motivo de la multa es obligatorio")
        @Size(max = 250, message = "El motivo no puede superar los 250 caracteres")
        String motivo,

        @NotNull(message = "Por favor, ingrese un valor numérico válido para el monto")
        @DecimalMin(value = "0.01", message = "El monto de la multa debe ser mayor a cero")
        BigDecimal monto
) {}