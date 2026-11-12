package edu.willbook_backend.impl;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.exception.CategoriaDuplicadaException;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock CategoriaRepository categoriaRepository;
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
}