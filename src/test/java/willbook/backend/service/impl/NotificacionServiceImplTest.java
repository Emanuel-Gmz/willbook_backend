package willbook.backend.service.impl;

import edu.willbook_backend.model.Notificacion;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.NotificacionRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.impl.NotificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock NotificacionRepository notificacionRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks
    NotificacionServiceImpl servicio;

    Usuario usuario;
    Notificacion notificacion;

    @BeforeEach
    void armar() {
        usuario = Usuario.builder().id(1L).email("user@test.com").build();
        notificacion = Notificacion.builder().id(1L).usuario(usuario).mensaje("Aviso").leida(false).build();
    }

    @Test
    void obtenerBandejaNotificacionesExitosa() {
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of(notificacion));
        when(notificacionRepository.countByUsuarioIdAndLeidaFalse(1L)).thenReturn(1L);

        Map<String, Object> resp = servicio.obtenerBandejaNotificaciones("user@test.com");

        assertEquals(1L, resp.get("contadorNoLeidas"));
    }

    @Test
    void marcarComoLeidaExitoso() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        Map<String, String> resp = servicio.marcarComoLeida(1L, "user@test.com");

        assertEquals("Notificación marcada como leida", resp.get("message"));
        assertTrue(notificacion.getLeida());
    }
}
