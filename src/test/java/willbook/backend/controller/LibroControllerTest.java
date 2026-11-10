package willbook.backend.controller;

import edu.willbook_backend.controller.LibroController;
import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.dto.LibroUpdateDTO;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.LibroService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LibroControllerTest {

    @Mock
    private LibroService libroService;

    @InjectMocks
    private LibroController libroController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(libroController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static LibroResponseDTO libroEjemplo() {
        return new LibroResponseDTO(
                1L,
                "Cien años de soledad",
                "Gabriel García Márquez",
                "978-0307474728",
                "Editorial Sudamericana",
                1967,
                "Ficción",
                "https://placehold.co/300x450",
                "Novela emblemática.",
                true,
                List.of()
        );
    }

    @Test
    void listarCatalogoDevuelveListaDeLibros() throws Exception {
        when(libroService.buscarLibros(null)).thenReturn(List.of(libroEjemplo()));

        mvc.perform(get("/api/v1/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Cien años de soledad"))
                .andExpect(jsonPath("$[0].autor").value("Gabriel García Márquez"));
    }

    @Test
    void buscarConQuerySinResultadosRetornaMensajeVacio() throws Exception {
        when(libroService.buscarLibros("Inexistente")).thenReturn(List.of());

        mvc.perform(get("/api/v1/catalogo").param("q", "Inexistente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("No se encontraron libros que coincidan con tu búsqueda. Intenta con otros términos."))
                .andExpect(jsonPath("$.resultados").isArray());
    }

    @Test
    void obtenerLibroPorIdDevuelveElLibro() throws Exception {
        when(libroService.obtenerLibroPorId(1L)).thenReturn(libroEjemplo());

        mvc.perform(get("/api/v1/catalogo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("978-0307474728"));
    }

    @Test
    void crearLibroDevuelveStatusCreated() throws Exception {
        Map<String, Object> respuestaMock = Map.of(
                "message", "Libro cargado exitosamente",
                "libro", libroEjemplo()
        );
        when(libroService.crearLibro(any(LibroCreateDTO.class))).thenReturn(respuestaMock);

        String jsonRequest = "{"
                + "\"titulo\":\"Cien años de soledad\","
                + "\"autorId\":1,"
                + "\"isbn\":\"978-0307474728\","
                + "\"editorial\":\"Sudamericana\","
                + "\"anioPublicacion\":1967,"
                + "\"categoriaId\":1"
                + "}";

        mvc.perform(post("/api/v1/catalogo/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Libro cargado exitosamente"));
    }

    @Test
    void actualizarLibroDevuelveOk() throws Exception {
        Map<String, Object> respuestaMock = Map.of(
                "message", "Libro actualizado correctamente",
                "libro", libroEjemplo()
        );
        when(libroService.actualizarLibro(eq(1L), any(LibroUpdateDTO.class))).thenReturn(respuestaMock);

        String jsonRequest = "{"
                + "\"titulo\":\"Cien años de soledad (Edición Especial)\","
                + "\"anioPublicacion\":1967"
                + "}";

        mvc.perform(put("/api/v1/catalogo/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro actualizado correctamente"));
    }

    @Test
    void agregarEjemplarDevuelveCreated() throws Exception {
        Map<String, Object> respuestaMock = Map.of(
                "message", "Ejemplar agregado correctamente al inventario"
        );
        when(libroService.agregarEjemplar(eq(1L), eq("INV-001"))).thenReturn(respuestaMock);

        mvc.perform(post("/api/v1/catalogo/1/ejemplares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoInventario\":\"INV-001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Ejemplar agregado correctamente al inventario"));
    }

    @Test
    void eliminarLibroDevuelveOk() throws Exception {
        when(libroService.eliminarLibro(1L)).thenReturn(Map.of("message", "Libro eliminado correctamente del catálogo"));

        mvc.perform(delete("/api/v1/catalogo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Libro eliminado correctamente del catálogo"));

        verify(libroService).eliminarLibro(1L);
    }
}
