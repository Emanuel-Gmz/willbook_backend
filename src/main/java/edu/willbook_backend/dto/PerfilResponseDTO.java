package edu.willbook_backend.dto;

import edu.willbook_backend.enums.Rol;

public record PerfilResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String dni,
        String email,
        String telefono,
        String direccion,
        Rol rol,
        String localidadNombre
) {}