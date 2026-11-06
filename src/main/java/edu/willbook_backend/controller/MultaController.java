package edu.willbook_backend.controller;

import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.service.MultaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/multas")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar o cargar una multa manualmente en la cuenta de un usuario")
    public ResponseEntity crearMultaManual(
            @Valid @RequestBody MultaCreateDTO request
    ) {
        Map<String, Object> response = multaService.crearMultaManual(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}