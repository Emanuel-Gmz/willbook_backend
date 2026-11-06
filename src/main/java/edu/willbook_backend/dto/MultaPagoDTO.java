package edu.willbook_backend.dto;

import edu.willbook_backend.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;

public record MultaPagoDTO(
        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodoPago
) {}