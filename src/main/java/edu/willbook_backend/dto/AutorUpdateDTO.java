package edu.willbook_backend.dto;

import jakarta.validation.constraints.Size;

public record AutorUpdateDTO(
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @Size(max = 500, message = "La biografía no puede superar los 500 caracteres")
        String biografia,

        @Size(max = 50, message = "El país de origen no puede superar los 50 caracteres")
        String paisOrigen
) {}