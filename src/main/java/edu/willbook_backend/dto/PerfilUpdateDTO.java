package edu.willbook_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PerfilUpdateDTO(
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String apellido,

        @Email(message = "Por favor, ingresa un formato de correo electrónico válido")
        String email,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String direccion,

        Long localidadId
) {}