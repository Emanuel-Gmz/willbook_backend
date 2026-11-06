package edu.willbook_backend.controller;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.service.LibroService;
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
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    @Operation(summary = "Listar o buscar libros en el catálogo por título o autor")
    public ResponseEntity<Object> buscarOCatalogar(
            @RequestParam(required = false) String q
    ) {
        List<LibroResponseDTO> resultados = libroService.buscarLibros(q);

        // Si se envió un término de búsqueda y no hay resultados (Escenario 2)
        if (resultados.isEmpty() && q != null && !q.isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "No se encontraron libros que coincidan con tu búsqueda. Intenta con otros términos.",
                    "resultados", List.of()
            ));
        }

        return ResponseEntity.ok(resultados);
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Registrar un nuevo libro en el catálogo")
    public ResponseEntity crearLibro(
            @Valid @RequestBody LibroCreateDTO request
    ) {
        Map<String, Object> response = libroService.crearLibro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}