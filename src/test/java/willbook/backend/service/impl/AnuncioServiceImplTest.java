package willbook.backend.service.impl;
import edu.willbook_backend.dto.AnuncioCreateDTO;
import edu.willbook_backend.dto.AnuncioResponseDTO;
import edu.willbook_backend.model.Anuncio;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.AnuncioRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.impl.AnuncioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceImplTest {

    @Mock AnuncioRepository anuncioRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks
    AnuncioServiceImpl servicio;

    Usuario autor;
    Anuncio anuncio;

    @BeforeEach
    void armar() {
        autor = Usuario.builder()
                .id(1L)
                .nombre("Admin")
                .apellido("WillBook")
                .email("admin@willbook.com")
                .build();

        anuncio = Anuncio.builder()
                .id(1L)
                .titulo("Mantenimiento")
                .mensaje("Sistema cerrado por actualización")
                .fechaCreacion(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusDays(1))
                .autor(autor)
                .build();
    }

    @Test
    void listarAnunciosVigentesDevuelveLista() {
        when(anuncioRepository.findAnunciosVigentes(any())).thenReturn(List.of(anuncio));

        List<AnuncioResponseDTO> resultado = servicio.listarAnunciosVigentes();

        assertEquals(1, resultado.size());
        assertEquals("Mantenimiento", resultado.get(0).titulo());
        assertEquals("Admin WillBook", resultado.get(0).nombreAutor());
    }

    @Test
    void crearAnuncioExitoso() {
        AnuncioCreateDTO dto = new AnuncioCreateDTO("Nuevo Evento", "Apertura de biblioteca", null);
        when(usuarioRepository.findByEmail("admin@willbook.com")).thenReturn(Optional.of(autor));
        when(anuncioRepository.save(any())).thenReturn(anuncio);

        Map<String, Object> respuesta = servicio.crearAnuncio(dto, "admin@willbook.com");

        assertEquals("Anuncio publicado correctamente", respuesta.get("message"));
        assertNotNull(respuesta.get("anuncio"));
        verify(anuncioRepository).save(any());
    }

    @Test
    void crearAnuncioUsuarioNoEncontradoDa404() {
        AnuncioCreateDTO dto = new AnuncioCreateDTO("Título", "Mensaje", null);
        when(usuarioRepository.findByEmail("noexiste@willbook.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            servicio.crearAnuncio(dto, "noexiste@willbook.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());
    }
}
