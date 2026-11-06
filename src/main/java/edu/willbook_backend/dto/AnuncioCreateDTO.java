package edu.willbook_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AnuncioCreateDTO(
        @NotBlank(message = "Debe completar el título y el mensaje para publicar")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        @NotBlank(message = "Debe completar el título y el mensaje para publicar")
        @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
        String mensaje,

        LocalDateTime fechaExpiracion
) {}