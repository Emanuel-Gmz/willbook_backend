package willbook.backend.controller;

import edu.willbook_backend.controller.AutorController;
import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.dto.AutorUpdateDTO;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.AutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutorControllerTest {

    @Mock
    private AutorService autorService;

    @InjectMocks
    private AutorController autorController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(autorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static AutorResponseDTO autorEjemplo() {
        return new AutorResponseDTO(1L, "Gabriel", "García Márquez", "Colombiana", "Biografía...");
    }

    @Test
    void listarAutoresDevuelveLista() throws Exception {
        when(autorService.listarOFiltrarAutores(null)).thenReturn(List.of(autorEjemplo()));

        mvc.perform(get("/api/v1/autores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Gabriel"));
    }

    @Test
    void crearAutorDevuelveCreated() throws Exception {
        when(autorService.crearAutor(any(AutorCreateDTO.class))).thenReturn(Map.of("message", "Autor creado"));

        String json = "{\"nombre\":\"Gabriel\",\"apellido\":\"García Márquez\",\"paisOrigen\":\"Colombiana\"}";

        mvc.perform(post("/api/v1/autores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Autor creado"));
    }

    @Test
    void obtenerAutorPorIdDevuelveAutor() throws Exception {
        when(autorService.obtenerAutorPorId(1L)).thenReturn(autorEjemplo());

        mvc.perform(get("/api/v1/autores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.apellido").value("García Márquez"));
    }

    @Test
    void actualizarAutorDevuelveOk() throws Exception {
        when(autorService.actualizarAutor(eq(1L), any(AutorUpdateDTO.class))).thenReturn(Map.of("message", "Autor actualizado"));

        String json = "{\"nombre\":\"Gabriel Modificado\",\"apellido\":\"García Márquez\"}";

        mvc.perform(put("/api/v1/autores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Autor actualizado"));
    }

    @Test
    void eliminarAutorDevuelveOk() throws Exception {
        when(autorService.eliminarAutor(1L)).thenReturn(Map.of("message", "Autor eliminado"));

        mvc.perform(delete("/api/v1/autores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Autor eliminado"));
    }
}
