package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.AnuncioCreateDTO;
import edu.willbook_backend.dto.AnuncioResponseDTO;
import edu.willbook_backend.model.Anuncio;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.AnuncioRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.AnuncioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnuncioServiceImpl implements AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AnuncioResponseDTO> listarAnunciosVigentes() {
        LocalDateTime ahora = LocalDateTime.now();
        return anuncioRepository.findAnunciosVigentes(ahora).stream()
                .map(a -> new AnuncioResponseDTO(
                        a.getId(),
                        a.getTitulo(),
                        a.getMensaje(),
                        a.getFechaCreacion(),
                        a.getFechaExpiracion(),
                        a.getAutor().getNombre() + " " + a.getAutor().getApellido()
                ))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, Object> crearAnuncio(AnuncioCreateDTO request, String emailUsuario) {
        // Obtener el usuario autenticado (Bibliotecario o Admin)
        Usuario autor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Anuncio anuncio = Anuncio.builder()
                .titulo(request.titulo().trim())
                .mensaje(request.mensaje().trim())
                .fechaExpiracion(request.fechaExpiracion())
                .autor(autor)
                .build();

        Anuncio guardado = anuncioRepository.save(anuncio);

        AnuncioResponseDTO anuncioDTO = new AnuncioResponseDTO(
                guardado.getId(),
                guardado.getTitulo(),
                guardado.getMensaje(),
                guardado.getFechaCreacion(),
                guardado.getFechaExpiracion(),
                autor.getNombre() + " " + autor.getApellido()
        );

        return Map.of(
                "message", "Anuncio publicado correctamente",
                "anuncio", anuncioDTO
        );
    }
}