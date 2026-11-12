package edu.willbook_backend.impl;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.dto.AutorUpdateDTO;
import edu.willbook_backend.exception.AutorConLibrosException;
import edu.willbook_backend.exception.AutorDuplicadoException;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.impl.AutorServiceImpl;
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
class AutorServiceImplTest {

    @Mock AutorRepository autorRepository;
    @Mock LibroRepository libroRepository;
    @InjectMocks
    AutorServiceImpl servicio;

    Autor autor;

    @BeforeEach
    void armar() {
        autor = Autor.builder()
                .id(1L)
                .nombre("Gabriel")
                .apellido("García Márquez")
                .biografia("Escritor colombiano")
                .nacionalidad("Colombiana")
                .build();
    }

    @Test
    void listarOFiltrarAutoresSinQuery() {
        when(autorRepository.findAllByOrderByApellidoAscNombreAsc()).thenReturn(List.of(autor));

        List<AutorResponseDTO> lista = servicio.listarOFiltrarAutores(null);

        assertEquals(1, lista.size());
        assertEquals("Gabriel", lista.get(0).nombre());
    }

    @Test
    void listarOFiltrarAutoresConQueryBlanco() {
        when(autorRepository.findAllByOrderByApellidoAscNombreAsc()).thenReturn(List.of(autor));

        List<AutorResponseDTO> lista = servicio.listarOFiltrarAutores("   ");

        assertEquals(1, lista.size());
        verify(autorRepository).findAllByOrderByApellidoAscNombreAsc();
    }

    @Test
    void listarOFiltrarAutoresConQueryValido() {
        when(autorRepository.buscarPorNombreOApellido("Gabriel")).thenReturn(List.of(autor));

        List<AutorResponseDTO> lista = servicio.listarOFiltrarAutores("  Gabriel  ");

        assertEquals(1, lista.size());
        verify(autorRepository).buscarPorNombreOApellido("Gabriel");
    }

    @Test
    void crearAutorExitoso() {
        AutorCreateDTO dto = new AutorCreateDTO("Jorge Luis", "Borges", "Argentino", "Biografía...");
        when(autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase("Jorge Luis", "Borges")).thenReturn(false);
        when(autorRepository.save(any())).thenReturn(autor);

        Map<String, Object> respuesta = servicio.crearAutor(dto);

        assertEquals("Autor guardado exitosamente", respuesta.get("message"));
        verify(autorRepository).save(any());
    }

    @Test
    void crearAutorDuplicadoLanzaExcepcion() {
        AutorCreateDTO dto = new AutorCreateDTO("Gabriel", "García Márquez", "Colombiana", "Bio");
        when(autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase("Gabriel", "García Márquez")).thenReturn(true);

        assertThrows(AutorDuplicadoException.class, () -> servicio.crearAutor(dto));
    }

    @Test
    void obtenerAutorPorIdExitosoYNofound() {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        assertNotNull(servicio.obtenerAutorPorId(1L));
        assertThrows(ResponseStatusException.class, () -> servicio.obtenerAutorPorId(99L));
    }

    @Test
    void actualizarAutorExitoso() {
        AutorUpdateDTO dto = new AutorUpdateDTO("Gabriel Modificado", "Nuevo Apellido", "Mexicana", "Nueva bio");
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase("Gabriel Modificado", "Nuevo Apellido")).thenReturn(false);
        when(autorRepository.save(any())).thenReturn(autor);

        Map<String, Object> resp = servicio.actualizarAutor(1L, dto);
        assertEquals("Autor actualizado correctamente", resp.get("message"));
    }

    @Test
    void actualizarAutorMismoNombreYApellidoNoVerificaDuplicado() {
        // Actualiza biografía y nacionalidad manteniendo nombre y apellido iguales
        AutorUpdateDTO dto = new AutorUpdateDTO("Gabriel", "García Márquez", "Mexicana", "Bio actualizada");
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(autorRepository.save(any())).thenReturn(autor);

        Map<String, Object> resp = servicio.actualizarAutor(1L, dto);
        assertEquals("Autor actualizado correctamente", resp.get("message"));
        verify(autorRepository, never()).existsByNombreIgnoreCaseAndApellidoIgnoreCase(anyString(), anyString());
    }

    @Test
    void actualizarAutorCamposNulosOBlancoNoModificaNiDuplica() {
        AutorUpdateDTO dto = new AutorUpdateDTO(null, "   ", null, null);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(autorRepository.save(any())).thenReturn(autor);

        Map<String, Object> resp = servicio.actualizarAutor(1L, dto);
        assertEquals("Autor actualizado correctamente", resp.get("message"));
    }

    @Test
    void actualizarAutorNoEncontradoLanza404() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicio.actualizarAutor(99L, new AutorUpdateDTO("A", "B", "C", "D"))
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void actualizarAutorDuplicadoLanzaExcepcion() {
        AutorUpdateDTO dto = new AutorUpdateDTO("Duplicado", "Autor", null, null);
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase("Duplicado", "Autor")).thenReturn(true);

        assertThrows(AutorDuplicadoException.class, () -> servicio.actualizarAutor(1L, dto));
    }

    @Test
    void eliminarAutorNoEncontradoLanza404() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.eliminarAutor(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void eliminarAutorConLibrosLanzaExcepcion() {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(libroRepository.existsByAutorId(1L)).thenReturn(true);

        assertThrows(AutorConLibrosException.class, () -> servicio.eliminarAutor(1L));
        verify(autorRepository, never()).delete(any());
    }

    @Test
    void eliminarAutorSinLibrosExitoso() {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autor));
        when(libroRepository.existsByAutorId(1L)).thenReturn(false);

        Map<String, String> resp = servicio.eliminarAutor(1L);

        assertEquals("Autor eliminado correctamente", resp.get("message"));
        verify(autorRepository).delete(autor);
    }
}