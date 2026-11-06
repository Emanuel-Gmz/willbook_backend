package edu.willbook_backend.controller;

import edu.willbook_backend.dto.MisMultasResponseDTO;
import edu.willbook_backend.dto.MultaCancelacionDTO;
import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;
import edu.willbook_backend.service.MultaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/multas")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Registrar o cargar una multa manualmente en la cuenta de un usuario")
    public ResponseEntity crearMultaManual(
            @Valid @RequestBody MultaCreateDTO request
    ) {
        Map<String, Object> response = multaService.crearMultaManual(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Anular o cancelar una multa activa de un usuario (Solo Bibliotecario o Admin)")
    public ResponseEntity cancelarMulta(
            @PathVariable Long id,
            @RequestBody(required = false) MultaCancelacionDTO request
    ) {
        Map<String, Object> response = multaService.cancelarMulta(id, request != null ? request : new MultaCancelacionDTO(null));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Registrar el cobro o pago de una multa activa (Solo Bibliotecario o Admin)")
    public ResponseEntity registrarPagoMulta(
            @PathVariable Long id,
            @Valid @RequestBody MultaPagoDTO request
    ) {
        Map<String, Object> response = multaService.registrarPagoMulta(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-multas")
    @PreAuthorize("hasRole('LECTOR')")
    @Operation(summary = "Consultar estado de multas y sanciones del lector autenticado")
    public ResponseEntity<MisMultasResponseDTO> consultarMisMultas(Principal principal) {
        MisMultasResponseDTO response = multaService.consultarMisMultas(principal.getName());
        return ResponseEntity.ok(response);
    }
}