package edu.willbook_backend.controller;

import edu.willbook_backend.dto.PrestamoDetalleDTO;
import edu.willbook_backend.dto.PrestamoPersonalDTO;
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

    @GetMapping("/mis-prestamos")
    @PreAuthorize("hasRole('LECTOR')")
    @Operation(summary = "Consultar lista personal de préstamos e historial del lector autenticado")
    public ResponseEntity<Object> consultarMisPrestamos(Principal principal) {
        List<PrestamoPersonalDTO> misPrestamos = prestamoService.consultarMisPrestamos(principal.getName());

        // Manejo del Escenario 2: Lector nuevo sin historial
        if (misPrestamos.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Aún no has solicitado ningún libro. ¡Explora el catálogo!",
                    "prestamos", List.of()
            ));
        }

        return ResponseEntity.ok(misPrestamos);
    }

    @PutMapping("/{id}/reportar-perdida")
    @PreAuthorize("hasAnyRole('LECTOR', 'BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Reportar pérdida o daño de un libro en préstamo")
    public ResponseEntity reportarPerdida(
            @PathVariable Long id,
            Principal principal
    ) {
        Map<String, Object> response = prestamoService.reportarLossOrDamage(id, principal.getName());
        return ResponseEntity.ok(response);
    }
}