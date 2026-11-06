package edu.willbook_backend.dto;

import java.util.List;

public record PerfilLectorCompletoDTO(
        Long id,
        String nombre,
        String apellido,
        String dni,
        String email,
        String telefono,
        String direccion,
        String localidad,
        Boolean cuentaActiva, // true = activa, false = bloqueada
        List<PrestamoPersonalDTO> prestamosActivos,
        List<PrestamoPersonalDTO> historialPrestamos,
        List<MultaDetalleDTO> multas
) {}