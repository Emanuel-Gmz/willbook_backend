package edu.willbook_backend.controller;

import edu.willbook_backend.service.EjemplarService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ejemplares")
@RequiredArgsConstructor
public class EjemplarController {

    private final EjemplarService ejemplarService;

    @DeleteMapping("/{codigoInventario}")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Dar de baja o eliminar un ejemplar físico por su código de inventario")
    public ResponseEntity darDeBajaEjemplar(@PathVariable String codigoInventario) {
        Map<String, String> response = ejemplarService.darDeBajaEjemplar(codigoInventario);
        return ResponseEntity.ok(response);
    }
}