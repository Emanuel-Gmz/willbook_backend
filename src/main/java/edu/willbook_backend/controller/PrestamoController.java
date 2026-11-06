package edu.willbook_backend.controller;

import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.dto.PrestamoResponseDTO;
import edu.willbook_backend.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('LECTOR')")
    @Operation(summary = "Solicitar el retiro de un libro del catálogo virtual")
    public ResponseEntity<PrestamoResponseDTO> solicitarRetiro(
            @Valid @RequestBody PrestamoRequestDTO request,
            Principal principal
    ) {
        PrestamoResponseDTO response = prestamoService.solicitarRetiro(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}