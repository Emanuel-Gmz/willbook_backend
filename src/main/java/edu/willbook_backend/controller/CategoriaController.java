package edu.willbook_backend.controller;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.service.CategoriaService;
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
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar todas las categorías ordenadas alfabéticamente - Público")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Crear nueva categoría o género literario")
    public ResponseEntity crearCategoria(
            @Valid @RequestBody CategoriaCreateDTO request
    ) {
        Map<String, Object> response = categoriaService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Modificar una categoría existente")
    public ResponseEntity actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaCreateDTO request
    ) {
        Map<String, Object> response = categoriaService.actualizarCategoria(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Eliminar una categoría si no tiene libros asociados")
    public ResponseEntity eliminarCategoria(@PathVariable Long id) {
        Map<String, String> response = categoriaService.eliminarCategoria(id);
        return ResponseEntity.ok(response);
    }
}