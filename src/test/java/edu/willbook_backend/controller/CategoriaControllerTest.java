package edu.willbook_backend.controller;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.CategoriaService;
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
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarCategoriasDevuelveLista() throws Exception {
        CategoriaResponseDTO dto = new CategoriaResponseDTO(1L, "Ficción", "Libros de ficción");
        when(categoriaService.listarCategorias()).thenReturn(List.of(dto));

        mvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Ficción"));
    }

    @Test
    void crearCategoriaDevuelveCreated() throws Exception {
        when(categoriaService.crearCategoria(any(CategoriaCreateDTO.class))).thenReturn(Map.of("message", "Categoría creada"));

        String json = "{\"nombre\":\"Ficción\",\"descripcion\":\"Libros de ficción\"}";

        mvc.perform(post("/api/v1/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Categoría creada"));
    }

    @Test
    void actualizarCategoriaDevuelveOk() throws Exception {
        when(categoriaService.actualizarCategoria(eq(1L), any(CategoriaCreateDTO.class)))
                .thenReturn(Map.of("message", "Categoría actualizada correctamente"));

        String json = "{\"nombre\":\"Ficción Actualizada\",\"descripcion\":\"Nueva descripción\"}";

        mvc.perform(put("/api/v1/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Categoría actualizada correctamente"));
    }

    @Test
    void eliminarCategoriaDevuelveOk() throws Exception {
        when(categoriaService.eliminarCategoria(1L))
                .thenReturn(Map.of("message", "Categoría eliminada correctamente"));

        mvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Categoría eliminada correctamente"));
    }
}