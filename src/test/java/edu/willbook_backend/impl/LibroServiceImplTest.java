package edu.willbook_backend.impl;

import edu.willbook_backend.dto.EjemplarUpdateDTO;
import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.dto.LibroUpdateDTO;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.exception.AutorNoEncontradoException;
import edu.willbook_backend.exception.CategoriaNoEncontradaException;
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
    void buscarLibrosConQueryDevuelveResultados() {
        when(libroRepository.buscarPorTituloOAutor("Cien")).thenReturn(List.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        List<LibroResponseDTO> resultado = servicio.buscarLibros("  Cien  ");

        assertEquals(1, resultado.size());
        assertEquals("Cien Años", resultado.get(0).titulo());
    }

    @Test
    void crearLibroExitoso() {
        LibroCreateDTO dto = new LibroCreateDTO(
                "Cien Años",
                1L,
                "978-3-16-148410-0",
                "Sudamericana",
                1967,
                1L,
                "url",
                "desc"
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
                "Cien Años",
                1L,
                "978-3-16-148410-0",
                "Sudamericana",
                1967,
                1L,
                "url",
                "desc"
        );
        when(libroRepository.existsByIsbn(any())).thenReturn(true);

        assertThrows(IsbnDuplicadoException.class, () -> servicio.crearLibro(dto));
    }

    @Test
    void crearLibroAutorNoEncontradoLanzaExcepcion() {
        LibroCreateDTO dto = new LibroCreateDTO(
                "Cien Años",
                99L,
                "978-3-16-148410-0",
                "Sudamericana",
                1967,
                1L,
                "url",
                "desc"
        );
        when(libroRepository.existsByIsbn(any())).thenReturn(false);
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AutorNoEncontradoException.class, () -> servicio.crearLibro(dto));
    }

    @Test
    void crearLibroCategoriaNoEncontradaLanzaExcepcion() {
        LibroCreateDTO dto = new LibroCreateDTO(
                "Cien Años",
                1L,
                "978-3-16-148410-0",
                "Sudamericana",
                1967,
                99L,
                "url",
                "desc"
        );
        when(libroRepository.existsByIsbn(any())).thenReturn(false);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> servicio.crearLibro(dto));
    }

    @Test
    void obtenerLibroPorIdExitoso() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        LibroResponseDTO dto = servicio.obtenerLibroPorId(1L);

        assertNotNull(dto);
        assertEquals("Cien Años", dto.titulo());
    }

    @Test
    void obtenerLibroPorIdNoEncontradoLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.obtenerLibroPorId(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void actualizarLibroExitoso() {
        LibroUpdateDTO updateDTO = new LibroUpdateDTO(
                "Nuevo Título",
                1L,
                "978-3-16-148410-9",
                "Nueva Editorial",
                2000,
                1L,
                "nueva-url",
                "nueva desc",
                false,
                List.of(new EjemplarUpdateDTO(1L, "INV-002", EstadoEjemplar.DISPONIBLE))
        );

        Ejemplar ejemplar = Ejemplar.builder().id(1L).estado(EstadoEjemplar.DISPONIBLE).build();

        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.existsByIsbnAndIdNot("978-3-16-148410-9", 1L)).thenReturn(false);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(any())).thenReturn(libro);
        when(ejemplarRepository.findById(1L)).thenReturn(Optional.of(ejemplar));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        Map<String, Object> resp = servicio.actualizarLibro(1L, updateDTO);

        assertNotNull(resp);
        assertEquals("Libro actualizado correctamente", resp.get("message"));
    }

    @Test
    void actualizarLibroIsbnDuplicadoLanzaExcepcion() {
        LibroUpdateDTO updateDTO = new LibroUpdateDTO(
                null, null, "978-3-16-148410-9", null, null, null, null, null, null, null
        );

        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.existsByIsbnAndIdNot("978-3-16-148410-9", 1L)).thenReturn(true);

        assertThrows(IsbnDuplicadoException.class, () -> servicio.actualizarLibro(1L, updateDTO));
    }

    @Test
    void actualizarLibroNoEncontradoLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.actualizarLibro(99L, new LibroUpdateDTO(null, null, null, null, null, null, null, null, null, null)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void actualizarLibroEjemplarNoEncontradoLanza404() {
        LibroUpdateDTO updateDTO = new LibroUpdateDTO(
                null, null, null, null, null, null, null, null, null,
                List.of(new EjemplarUpdateDTO(99L, "INV-002", EstadoEjemplar.DISPONIBLE))
        );

        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.actualizarLibro(1L, updateDTO));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void agregarEjemplarExitoso() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByCodigoInventario("INV-001")).thenReturn(Optional.empty());
        when(ejemplarRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> resp = servicio.agregarEjemplar(1L, "INV-001");

        assertNotNull(resp);
        assertEquals("Ejemplar agregado correctamente al inventario", resp.get("message"));
    }

    @Test
    void agregarEjemplarCodigoExistenteLanzaConflict() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByCodigoInventario("INV-001")).thenReturn(Optional.of(new Ejemplar()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.agregarEjemplar(1L, "INV-001"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void agregarEjemplarLibroNoEncontradoLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.agregarEjemplar(99L, "INV-001"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void eliminarLibroExitoso() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        Map<String, String> resp = servicio.eliminarLibro(1L);

        assertNotNull(resp);
        assertEquals("Libro eliminado correctamente del catálogo", resp.get("message"));
        verify(libroRepository).delete(libro);
    }

    @Test
    void eliminarLibroNoEncontradoLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.eliminarLibro(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void eliminarLibroConEjemplarPrestadoLanzaConflict() {
        Ejemplar ejemplar = Ejemplar.builder().id(1L).estado(EstadoEjemplar.PRESTADO).build();
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of(ejemplar));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.eliminarLibro(1L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void actualizarLibroCamposNulosIgnoraCambios() {
        LibroUpdateDTO updateDTO = new LibroUpdateDTO(
                null, null, null, null, null, null, null, null, null, null
        );

        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.save(any())).thenReturn(libro);
        when(ejemplarRepository.findByLibroId(1L)).thenReturn(List.of());

        Map<String, Object> resp = servicio.actualizarLibro(1L, updateDTO);

        assertNotNull(resp);
        assertEquals("Libro actualizado correctamente", resp.get("message"));
    }
}