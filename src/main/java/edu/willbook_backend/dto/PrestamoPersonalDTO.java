package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoPrestamo;
import java.time.LocalDateTime;

public record PrestamoPersonalDTO(
        Long idPrestamo,
        Long libroId,
        String tituloLibro,
        String portadaUrl,
        String codigoEjemplar,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaRetiro,
        LocalDateTime fechaDevolucionEsperada,
        LocalDateTime fechaDevolucionReal,
        EstadoPrestamo estadoInterno,
        String estadoVisual, // "Vigente", "Vencido/Atrasado", "Devuelto", "Pendiente de Retiro"
        boolean atrasado     // Útil para que el frontend resalte la fecha y estado en rojo
) {}