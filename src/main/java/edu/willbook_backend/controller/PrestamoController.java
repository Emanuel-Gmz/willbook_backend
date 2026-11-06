package edu.willbook_backend.controller;

import edu.willbook_backend.dto.PrestamoDetalleDTO;
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
import java.util.List;
import java.util.Map;

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
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Listar solicitudes de retiro pendientes de confirmación")
    public ResponseEntity<List<PrestamoDetalleDTO>> listarPendientes() {
        return ResponseEntity.ok(prestamoService.listarSolicitudesPendientes());
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Confirmar entrega física de un libro y registrar préstamo")
    public ResponseEntity confirmarPrestamo(@PathVariable Long id) {
        Map<String, String> response = prestamoService.confirmarPrestamo(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Cancelar reserva caducada y liberar ejemplar a disponible")
    public ResponseEntity cancelarReserva(@PathVariable Long id) {
        Map<String, String> response = prestamoService.cancelarReserva(id);
        return ResponseEntity.ok(response);
    }
}