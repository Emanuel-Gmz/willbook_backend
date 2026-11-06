package edu.willbook_backend.dto;

import edu.willbook_backend.enums.EstadoPrestamo;
import java.time.LocalDateTime;

public record PrestamoDetalleDTO(
        Long idPrestamo,
        Long usuarioId,
        String nombreLector,
        String emailLector,
        Long libroId,
        String tituloLibro,
        String codigoEjemplar,
        LocalDateTime fechaSolicitud,
        EstadoPrestamo estado
) {}