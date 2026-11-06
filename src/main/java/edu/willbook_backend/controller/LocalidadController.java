package edu.willbook_backend.controller;

import edu.willbook_backend.dto.LocalidadResponseDTO;
import edu.willbook_backend.service.LocalidadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/localidades")
@RequiredArgsConstructor
public class LocalidadController {

    private final LocalidadService localidadService;

    @GetMapping
    @Operation(summary = "Listar todas las localidades")
    public ResponseEntity<List<LocalidadResponseDTO>> listarLocalidades() {
        List<LocalidadResponseDTO> localidades = localidadService.listarLocalidades();
        return ResponseEntity.ok(localidades);
    }
}