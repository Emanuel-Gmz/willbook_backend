package edu.willbook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LibroCreateDTO(
        @NotBlank(message = "Este campo es requerido")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        @NotNull(message = "Debe seleccionar un autor")
        Long autorId,

        @NotBlank(message = "Este campo es requerido")
        @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
        String isbn,

        @Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
        String editorial,

        @NotNull(message = "Este campo es requerido")
        Integer anioPublicacion,

        @NotNull(message = "Debe seleccionar una categoría")
        Long categoriaId,

        String portadaUrl,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion
) {}