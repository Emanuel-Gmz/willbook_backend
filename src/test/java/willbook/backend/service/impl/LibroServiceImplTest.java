package willbook.backend.service.impl;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.exception.IsbnDuplicadoException;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.model.Ejemplar;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.repository.EjemplarRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.impl.LibroServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceImplTest {

    @Mock LibroRepository libroRepository;
    @Mock AutorRepository autorRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock EjemplarRepository ejemplarRepository;
    @InjectMocks
    LibroServiceImpl servicio;

    Libro libro;
    Autor autor;
    Categoria categoria;

    @BeforeEach
    void armar() {
        autor = Autor.builder().id(1L).nombre("Gabriel").apellido("García").build();
        categoria = Categoria.builder().id(1L).nombre("Novela").build();
        libro = Libro.builder()
                .id(1L)
                .titulo("Cien Años")
                .isbn("978-3-16-148410-0")
                .editorial("Sudamericana")
                .anioPublicacion(1967)
                .disponible(true)
                .autor(autor)
                .categoria(categoria)
                .build();
    }

    @Test
    void buscarLibrosSinQueryDevuelveCatalogo() {
        when(libroRepository.findAll()).thenReturn(List.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        List<LibroResponseDTO> resultado = servicio.buscarLibros(null);

        assertEquals(1, resultado.size());
        assertEquals("Cien Años", resultado.get(0).titulo());
    }

    @Test
    void crearLibroExitoso() {
        LibroCreateDTO dto = new LibroCreateDTO(
                "Cien Años",                 // titulo
                1L,                          // autorId
                "978-3-16-148410-0",         // isbn
                "Sudamericana",              // editorial
                1967,                        // anioPublicacion
                1L,                          // categoriaId
                "url",                       // portadaUrl
                "desc"                       // descripcion
        );
        when(libroRepository.existsByIsbn("978-3-16-148410-0")).thenReturn(false);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(any())).thenReturn(libro);
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        Map<String, Object> resp = servicio.crearLibro(dto);

        assertEquals("Libro cargado exitosamente", resp.get("message"));
        verify(libroRepository).save(any());
    }

    @Test
    void crearLibroIsbnDuplicadoLanzaExcepcion() {
        LibroCreateDTO dto = new LibroCreateDTO(
                "Cien Años",                 // titulo
                1L,                          // autorId
                "978-3-16-148410-0",         // isbn
                "Sudamericana",              // editorial
                1967,                        // anioPublicacion
                1L,                          // categoriaId
                "url",                       // portadaUrl
                "desc"                       // descripcion
        );
        when(libroRepository.existsByIsbn(any())).thenReturn(true);

        assertThrows(IsbnDuplicadoException.class, () -> servicio.crearLibro(dto));
    }

    @Test
    void eliminarLibroConEjemplarPrestadoLanzaConflict() {
        Ejemplar ejemplar = Ejemplar.builder().id(1L).estado(EstadoEjemplar.PRESTADO).build();
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of(ejemplar));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.eliminarLibro(1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}
