package edu.willbook_backend.impl;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    NotificacionRepository notificacionRepository;

    @Mock
    UsuarioRepository usuarioRepository;

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
        assertNotNull(resp.get("notificaciones"));
    }

    @Test
    void obtenerBandejaUsuarioNoEncontradoDa404() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // Esperamos UsuarioNoEncontradoException según la implementación real
        assertThrows(UsuarioNoEncontradoException.class, () -> {
            servicio.obtenerBandejaNotificaciones("noexiste@test.com");
        });
    }

    @Test
    void marcarComoLeidaExitoso() {
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));

        Map<String, String> resp = servicio.marcarComoLeida(1L, "user@test.com");

        assertEquals("Notificación marcada como leida", resp.get("message"));
        assertTrue(notificacion.getLeida());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void marcarComoLeidaNotificacionInexistenteDa404() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            servicio.marcarComoLeida(99L, "user@test.com");
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void marcarTodasComoLeidasExitoso() {
        when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findByUsuarioIdAndLeidaFalse(1L)).thenReturn(List.of(notificacion));

        Map<String, Object> resp = servicio.marcarTodasComoLeidas("user@test.com");

        assertNotNull(resp);
        assertTrue(notificacion.getLeida());
        verify(notificacionRepository, atLeastOnce()).save(any(Notificacion.class));
    }

    @Test
    void marcarTodasComoLeidasUsuarioNoEncontradoDa404() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // Esperamos UsuarioNoEncontradoException según la implementación real
        assertThrows(UsuarioNoEncontradoException.class, () -> {
            servicio.marcarTodasComoLeidas("noexiste@test.com");
        });
    }
}