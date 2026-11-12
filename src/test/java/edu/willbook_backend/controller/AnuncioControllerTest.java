package edu.willbook_backend.controller;

import edu.willbook_backend.controller.AnuncioController;
import edu.willbook_backend.dto.AnuncioCreateDTO;
import edu.willbook_backend.dto.AnuncioResponseDTO;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.AnuncioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnuncioControllerTest {

    @Mock
    private AnuncioService anuncioService;

    @InjectMocks
    private AnuncioController anuncioController;

    @Mock
    private Principal principal;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(anuncioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarAnunciosDevuelveLista() throws Exception {
        AnuncioResponseDTO dto = new AnuncioResponseDTO(
                1L,
                "Título",
                "Contenido",
                LocalDateTime.now(),
                null, // fechaExpiracion (LocalDateTime o null)
                "Admin" // nombreAutor (String)
        );
        when(anuncioService.listarAnunciosVigentes()).thenReturn(List.of(dto));

        mvc.perform(get("/api/v1/anuncios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].titulo").value("Título"));
    }

    @Test
    void crearAnuncioDevuelveCreated() throws Exception {
        when(principal.getName()).thenReturn("admin@willbook.com");
        when(anuncioService.crearAnuncio(any(AnuncioCreateDTO.class), eq("admin@willbook.com")))
                .thenReturn(Map.of("message", "Anuncio publicado correctamente"));

        // Ajustamos la clave a "mensaje" tal como lo define AnuncioCreateDTO
        String json = "{\"titulo\":\"Nuevo Anuncio\",\"mensaje\":\"Contenido del anuncio\"}";

        mvc.perform(post("/api/v1/anuncios")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Anuncio publicado correctamente"));
    }
}
