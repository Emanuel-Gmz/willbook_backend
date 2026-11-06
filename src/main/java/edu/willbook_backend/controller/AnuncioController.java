package edu.willbook_backend.controller;

import edu.willbook_backend.dto.AnuncioCreateDTO;
import edu.willbook_backend.dto.AnuncioResponseDTO;
import edu.willbook_backend.service.AnuncioService;
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
@RequestMapping("/api/v1/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final AnuncioService anuncioService;

    @GetMapping
    @Operation(summary = "Listar anuncios vigentes publicados en la plataforma - Público")
    public ResponseEntity<List<AnuncioResponseDTO>> listarAnuncios() {
        List<AnuncioResponseDTO> anuncios = anuncioService.listarAnunciosVigentes();
        return ResponseEntity.ok(anuncios);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
    @Operation(summary = "Crear y publicar un nuevo anuncio (Solo Bibliotecario o Admin)")
    public ResponseEntity crearAnuncio(
            @Valid @RequestBody AnuncioCreateDTO request,
            Principal principal
    ) {
        Map<String, Object> response = anuncioService.crearAnuncio(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}