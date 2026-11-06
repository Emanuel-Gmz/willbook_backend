package edu.willbook_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @NotBlank(message = "El DNI es obligatorio")
        String dni,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Debe ser un correo electrónico válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotBlank(message = "Debe confirmar su contraseña")
        String confirmacionPassword,

        @NotNull(message = "Debe seleccionar una localidad")
        Long localidadId
) {}