package edu.willbook_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de validación de los DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", errors);
    }

    // Errores manuales (ResponseStatusException)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex) {
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
    }

    // Método auxiliar para estructurar el JSON
    private ResponseEntity buildResponse(HttpStatus status, String message, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        if (details != null) {
            response.put("details", details);
        }
        return new ResponseEntity<>(response, status);
    }

    // Intercepta credenciales incorrectas (Escenario 3)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", null);
    }

    // Manejo de email duplicado (409)
    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity handleEmailAlreadyRegistered(EmailYaRegistradoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de registro", ex.getMessage());
    }

    // Manejo de DNI duplicado (409)
    @ExceptionHandler(DniYaRegistradoException.class)
    public ResponseEntity handleDniAlreadyRegistered(DniYaRegistradoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflicto de registro", ex.getMessage());
    }

    // Manejo de contraseñas que no coinciden (400)
    @ExceptionHandler(PasswordInvalidaException.class)
    public ResponseEntity handleInvalidPassword(PasswordInvalidaException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación de seguridad", ex.getMessage());
    }
}