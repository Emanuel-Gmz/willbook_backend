package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoMulta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MultaPersonalDTO(
        Long id,
        String tituloLibro,
        long diasAtraso,
        LocalDateTime fechaGeneracion,
        EstadoMulta estadoInterno,
        String estadoVisual, // "Activa" o "Resuelta" (o "Pagada" / "Cancelada")
        BigDecimal monto,
        String motivo
) {}