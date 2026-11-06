package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoMulta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MultaDetalleDTO(
        Long id,
        String motivo,
        BigDecimal monto,
        EstadoMulta estado,
        LocalDateTime fechaGeneracion
) {}