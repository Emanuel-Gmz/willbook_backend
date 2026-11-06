package edu.willbook_backend.controller;

import edu.willbook_backend.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LECTOR', 'BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Obtener bandeja de notificaciones y contador de no leídas para la campana")
    public ResponseEntity obtenerBandeja(Principal principal) {
        Map<String, Object> response = notificacionService.obtenerBandejaNotificaciones(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/leer")
    @PreAuthorize("hasAnyRole('LECTOR', 'BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Marcar una notificación específica como leída")
    public ResponseEntity marcarComoLeida(
            @PathVariable Long id,
            Principal principal
    ) {
        Map<String, String> response = notificacionService.marcarComoLeida(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/marcar-todas")
    @PreAuthorize("hasAnyRole('LECTOR', 'BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Marcar todas las notificaciones del usuario como leídas")
    public ResponseEntity marcarTodasComoLeidas(Principal principal) {
        Map<String, Object> response = notificacionService.marcarTodasComoLeidas(principal.getName());
        return ResponseEntity.ok(response);
    }
}