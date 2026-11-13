package edu.willbook_backend.controller;

import edu.willbook_backend.dto.PrestamoResponseDTO;
import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PrestamoControllerTest {

    @Mock
    private PrestamoService prestamoService;

    @InjectMocks
    private PrestamoController prestamoController;

    @Mock
    private Principal principal;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(prestamoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void solicitarRetiroDevuelveCreated() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");
        PrestamoResponseDTO responseDTO = new PrestamoResponseDTO(
                1L,
                1L,
                "Título",
                "INV-001",
                LocalDateTime.now(),
                EstadoPrestamo.PENDIENTE_RETIRO,
                "Solicitado con éxito"
        );
        when(prestamoService.solicitarRetiro(any(PrestamoRequestDTO.class), eq("lector@willbook.com"))).thenReturn(responseDTO);

        mvc.perform(post("/api/v1/prestamos/solicitar")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPrestamo").value(1));
    }

    @Test
    void listarPendientesDevuelveLista() throws Exception {
        when(prestamoService.listarSolicitudesPendientes()).thenReturn(List.of());

        // Ruta corregida a "/api/v1/prestamos/pendientes"
        mvc.perform(get("/api/v1/prestamos/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void confirmarPrestamoDevuelveOk() throws Exception {
        when(prestamoService.confirmarPrestamo(1L)).thenReturn(Map.of("message", "Confirmado"));

        mvc.perform(put("/api/v1/prestamos/1/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Confirmado"));
    }

    @Test
    void cancelarReservaDevuelveOk() throws Exception {
        when(prestamoService.cancelarReserva(1L)).thenReturn(Map.of("message", "Cancelado"));

        mvc.perform(put("/api/v1/prestamos/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cancelado"));
    }

    @Test
    void consultarMisPrestamosDevuelveMensajeVacio() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");
        when(prestamoService.consultarMisPrestamos("lector@willbook.com")).thenReturn(List.of());

        // Al estar vacía la lista, el controlador retorna un Map con el mensaje informativo
        mvc.perform(get("/api/v1/prestamos/mis-prestamos").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Aún no has solicitado ningún libro. ¡Explora el catálogo!"));
    }

    @Test
    void reportarPerdidaDevuelveOk() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");
        when(prestamoService.reportarLossOrDamage(1L, "lector@willbook.com")).thenReturn(Map.of("message", "Reportado"));

        // Ruta corregida a "/api/v1/prestamos/1/reportar-perdida"
        mvc.perform(put("/api/v1/prestamos/1/reportar-perdida").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reportado"));
    }

    @Test
    void consultarMisPrestamosDevuelveListaConElementos() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");
        var prestamoMock = new edu.willbook_backend.dto.PrestamoPersonalDTO(
                1L, 1L, "Libro Test", "url", "INV-001",
                java.time.LocalDateTime.now(), null, null, null,
                edu.willbook_backend.enums.EstadoPrestamo.PRESTADO, "Vigente", false
        );
        when(prestamoService.consultarMisPrestamos("lector@willbook.com")).thenReturn(List.of(prestamoMock));

        mvc.perform(get("/api/v1/prestamos/mis-prestamos").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idPrestamo").value(1));
    }
}