package edu.willbook_backend.controller;

import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    @Mock
    private Principal principal;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(notificacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerBandejaExitosa() throws Exception {
        when(principal.getName()).thenReturn("user@willbook.com");
        when(notificacionService.obtenerBandejaNotificaciones("user@willbook.com"))
                .thenReturn(Map.of("contadorNoLeidas", 2L));

        mvc.perform(get("/api/v1/notificaciones").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contadorNoLeidas").value(2));
    }

    @Test
    void marcarComoLeidaExitoso() throws Exception {
        when(principal.getName()).thenReturn("user@willbook.com");
        when(notificacionService.marcarComoLeida(eq(1L), eq("user@willbook.com")))
                .thenReturn(Map.of("message", "Notificación marcada como leida"));

        mvc.perform(put("/api/v1/notificaciones/1/leer").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notificación marcada como leida"));
    }

    @Test
    void marcarTodasComoLeidasExitoso() throws Exception {
        when(principal.getName()).thenReturn("user@willbook.com");
        when(notificacionService.marcarTodasComoLeidas("user@willbook.com"))
                .thenReturn(Map.of("message", "Todas marcadas como leídas"));

        mvc.perform(put("/api/v1/notificaciones/marcar-todas").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todas marcadas como leídas"));
    }
}