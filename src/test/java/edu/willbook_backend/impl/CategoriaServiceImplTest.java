package edu.willbook_backend.impl;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.exception.CategoriaDuplicadaException;
import edu.willbook_backend.exception.CategoriaNoEncontradaException;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    CategoriaRepository categoriaRepository;

    @Mock
    LibroRepository libroRepository;

    @InjectMocks
    CategoriaServiceImpl servicio;

    Categoria categoria;

    @BeforeEach
    void armar() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Ficción")
                .descripcion("Libros de ficción literaria")
                .build();
    }

    @Test
    void listarCategoriasDevuelveListaOrdenada() {
        when(categoriaRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(categoria));

        List<CategoriaResponseDTO> resultado = servicio.listarCategorias();

        assertEquals(1, resultado.size());
        assertEquals("Ficción", resultado.get(0).nombre());
    }

    @Test
    void crearCategoriaExitosa() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Ficción", "Descripción");
        when(categoriaRepository.existsByNombreIgnoreCase("Ficción")).thenReturn(false);
        when(categoriaRepository.save(any())).thenReturn(categoria);

        Map<String, Object> resp = servicio.crearCategoria(dto);

        assertEquals("Categoría creada", resp.get("message"));
        verify(categoriaRepository).save(any());
    }

    @Test
    void crearCategoriaDuplicadaLanzaExcepcion() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Ficción", "Descripción");
        when(categoriaRepository.existsByNombreIgnoreCase("Ficción")).thenReturn(true);

        assertThrows(CategoriaDuplicadaException.class, () -> servicio.crearCategoria(dto));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizarCategoriaExitosa() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Ficción Actualizada", "Nueva desc");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Ficción Actualizada")).thenReturn(false);
        when(categoriaRepository.save(any())).thenReturn(categoria);

        Map<String, Object> resp = servicio.actualizarCategoria(1L, dto);

        assertEquals("Categoría actualizada correctamente", resp.get("message"));
        verify(categoriaRepository).save(any());
    }

    @Test
    void actualizarCategoriaNoEncontradaLanzaExcepcion() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Terror", "Desc");
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> servicio.actualizarCategoria(99L, dto));
    }

    @Test
    void actualizarCategoriaConNombreDuplicadoLanzaExcepcion() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO("Terror", "Desc");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNombreIgnoreCase("Terror")).thenReturn(true);

        assertThrows(CategoriaDuplicadaException.class, () -> servicio.actualizarCategoria(1L, dto));
    }

    @Test
    void eliminarCategoriaExitosa() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.findAll()).thenReturn(List.of());

        Map<String, String> resp = servicio.eliminarCategoria(1L);

        assertEquals("Categoría eliminada correctamente", resp.get("message"));
        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminarCategoriaNoEncontradaLanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoriaNoEncontradaException.class, () -> servicio.eliminarCategoria(99L));
    }

    @Test
    void eliminarCategoriaConLibrosAsociadosLanzaConflicto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Libro libroAsociado = Libro.builder().id(10L).categoria(categoria).build();
        when(libroRepository.findAll()).thenReturn(List.of(libroAsociado));

        assertThrows(ResponseStatusException.class, () -> servicio.eliminarCategoria(1L));
        verify(categoriaRepository, never()).delete(any());
    }
}