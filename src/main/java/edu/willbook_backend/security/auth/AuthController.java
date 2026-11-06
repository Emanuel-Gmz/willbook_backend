package edu.willbook_backend.security.auth;

import edu.willbook_backend.dto.RegisterRequestDTO;
import edu.willbook_backend.security.dto.AuthRequest;
import edu.willbook_backend.security.dto.AuthResponse;
import edu.willbook_backend.security.dto.OlvidoPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo lector")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar enlace de recuperación de contraseña")
    public ResponseEntity forgotPassword(
            @Valid @RequestBody OlvidoPasswordRequest request) {

        authService.requestPasswordReset(request);

        // Retorna exactamente el mensaje genérico requerido por el Escenario 1
        return ResponseEntity.ok(Map.of(
                "message", "Si tu correo está registrado, recibirás las instrucciones para restablecer tu contraseña"
        ));
    }
}