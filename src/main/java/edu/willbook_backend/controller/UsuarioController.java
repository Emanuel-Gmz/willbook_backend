package edu.willbook_backend.controller;

import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PatchMapping("/cambiar-password")
    @Operation(summary = "Cambiar la contraseña.")
    public ResponseEntity cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            Principal connectedUser
    ) {
        usuarioService.cambiarPassword(request, connectedUser);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }
}