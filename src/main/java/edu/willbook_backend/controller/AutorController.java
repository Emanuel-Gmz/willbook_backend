package edu.willbook_backend.controller;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.dto.AutorUpdateDTO;
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
    @Operation(summary = "Listar o filtrar autores por nombre/apellido")
    public ResponseEntity<List<AutorResponseDTO>> listarOFiltrarAutores(
            @RequestParam(required = false) String q
    ) {
        List<AutorResponseDTO> autores = autorService.listarOFiltrarAutores(q);
        return ResponseEntity.ok(autores);
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

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un autor por ID")
    public ResponseEntity<AutorResponseDTO> obtenerAutor(@PathVariable Long id) {
        return ResponseEntity.ok(autorService.obtenerAutorPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Modificar información de un autor existente")
    public ResponseEntity actualizarAutor(
            @PathVariable Long id,
            @Valid @RequestBody AutorUpdateDTO request
    ) {
        Map<String, Object> response = autorService.actualizarAutor(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Eliminar un autor si no tiene libros asociados")
    public ResponseEntity eliminarAutor(@PathVariable Long id) {
        Map<String, String> response = autorService.eliminarAutor(id);
        return ResponseEntity.ok(response);
    }
}