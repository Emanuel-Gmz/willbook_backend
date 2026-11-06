package edu.willbook_backend.controller;

import edu.willbook_backend.dto.PerfilLectorCompletoDTO;
import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.dto.UsuarioBusquedaResponseDTO;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
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

    @PutMapping("/perfil")
    @Operation(summary = "Actualizar perfil")
    public ResponseEntity actualizarPerfil(
            @Valid @RequestBody PerfilUpdateDTO request,
            Principal connectedUser
    ) {
        PerfilResponseDTO perfilActualizado = usuarioService.actualizarPerfil(connectedUser.getName(), request);

        return ResponseEntity.ok(Map.of(
                "message", "Perfil actualizado con éxito",
                "perfil", perfilActualizado
        ));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Buscar lectores por DNI, Nombre o Apellido (Solo Bibliotecario o Admin)")
    public ResponseEntity<List<UsuarioBusquedaResponseDTO>> buscarLectores(
            @RequestParam(required = false) String q
    ) {
        List<UsuarioBusquedaResponseDTO> resultados = usuarioService.buscarLectores(q);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/{id}/perfil")
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Visualizar el perfil completo de un lector: datos, préstamos activos, historial y multas (Solo Bibliotecario o Admin)")
    public ResponseEntity<PerfilLectorCompletoDTO> obtenerPerfilLector(@PathVariable Long id) {
        PerfilLectorCompletoDTO perfil = usuarioService.obtenerPerfilLector(id);
        return ResponseEntity.ok(perfil);
    }
}