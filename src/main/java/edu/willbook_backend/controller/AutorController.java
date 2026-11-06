package edu.willbook_backend.controller;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.service.AutorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/autores")
@RequiredArgsConstructor
public class AutorController {

    private final AutorService autorService;

    @GetMapping
    @Operation(summary = "Listar todos los autores ordenados alfabéticamente por apellido")
    public ResponseEntity<List<AutorResponseDTO>> listarAutores() {
        return ResponseEntity.ok(autorService.listarAutores());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Registrar un nuevo autor")
    public ResponseEntity crearAutor(
            @Valid @RequestBody AutorCreateDTO request
    ) {
        Map<String, Object> response = autorService.crearAutor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}