package edu.willbook_backend.controller;

import edu.willbook_backend.dto.MisMultasResponseDTO;
import edu.willbook_backend.dto.MultaCancelacionDTO;
import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;
import edu.willbook_backend.enums.MetodoPago;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.MultaService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MultaControllerTest {

    @Mock
    private MultaService multaService;

    @InjectMocks
    private MultaController multaController;

    @Mock
    private Principal principal;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(multaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearMultaManualDevuelveCreated() throws Exception {
        when(multaService.crearMultaManual(any(MultaCreateDTO.class))).thenReturn(Map.of("message", "Multa creada"));

        String json = "{\"usuarioId\":1,\"motivo\":\"Demora\",\"monto\":100.0}";

        // Corregido de "/api/v1/multas/crear" a "/api/v1/multas"
        mvc.perform(post("/api/v1/multas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Multa creada"));
    }

    @Test
    void cancelarMultaDevuelveOk() throws Exception {
        when(multaService.cancelarMulta(eq(1L), any(MultaCancelacionDTO.class))).thenReturn(Map.of("message", "Multa cancelada"));

        mvc.perform(put("/api/v1/multas/1/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Perdón\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Multa cancelada"));
    }

    @Test
    void registrarPagoMultaDevuelveOk() throws Exception {
        when(multaService.registrarPagoMulta(eq(1L), any(MultaPagoDTO.class)))
                .thenReturn(Map.of("message", "Pago registrado exitosamente"));

        String json = "{\"metodoPago\":\"EFECTIVO\"}";

        mvc.perform(put("/api/v1/multas/1/pagar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pago registrado exitosamente"));
    }

    @Test
    void consultarMisMultasDevuelveOk() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");

        MisMultasResponseDTO responseDTO = new MisMultasResponseDTO(
                false,
                "Excelente historial. No tienes multas registradas en tu cuenta.",
                List.of()
        );

        when(multaService.consultarMisMultas("lector@willbook.com")).thenReturn(responseDTO);

        mvc.perform(get("/api/v1/multas/mis-multas").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentaRestringida").value(false));
    }

    @Test
    void cancelarMultaConRequestNuloDevuelveOk() throws Exception {
        when(multaService.cancelarMulta(eq(1L), any(MultaCancelacionDTO.class))).thenReturn(Map.of("message", "Multa cancelada"));

        mvc.perform(put("/api/v1/multas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Multa cancelada"));
    }
}