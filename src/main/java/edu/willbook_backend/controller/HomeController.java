package edu.willbook_backend.controller;

import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Obtener datos de inicio ")
    public ResponseEntity obtenerInicioPorPerfil(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() == Rol.LECTOR) {
            return ResponseEntity.ok(Map.of(
                    "vista", "CATALOGO",
                    "mensaje", "Bienvenido al catálogo de libros de WillBook",
                    "prestamosActivosCount", 0
            ));
        } else if (usuario.getRol() == Rol.BIBLIOTECARIO || usuario.getRol() == Rol.ADMIN) {
            return ResponseEntity.ok(Map.of(
                    "vista", "DASHBOARD",
                    "mensaje", "Panel de control administrativo",
                    "retirosPendientesCount", 0,
                    "alertasMultasCount", 0
            ));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no autorizado");
    }
}