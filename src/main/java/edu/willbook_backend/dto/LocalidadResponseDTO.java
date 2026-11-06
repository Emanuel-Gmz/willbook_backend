package edu.willbook_backend.dto;

public record LocalidadResponseDTO(
        Long id,
        String nombre,
        String provincia,
        String direccionSucursal
) {}