package edu.willbook_backend.impl;

import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.exception.EjemplarPrestadoException;
import edu.willbook_backend.model.Ejemplar;
import edu.willbook_backend.repository.EjemplarRepository;
import edu.willbook_backend.service.impl.EjemplarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EjemplarServiceImplTest {

    @Mock EjemplarRepository ejemplarRepository;
    @InjectMocks
    EjemplarServiceImpl servicio;

    Ejemplar ejemplar;

    @BeforeEach
    void armar() {
        ejemplar = Ejemplar.builder()
                .id(1L)
                .codigoInventario("INV-001")
                .estado(EstadoEjemplar.DISPONIBLE)
                .build();
    }

    @Test
    void darDeBajaEjemplarDisponibleExitoso() {
        when(ejemplarRepository.findByCodigoInventario("INV-001")).thenReturn(Optional.of(ejemplar));

        Map<String, String> respuesta = servicio.darDeBajaEjemplar("  INV-001  ");

        assertEquals("Ejemplar dado de baja correctamente", respuesta.get("message"));
        verify(ejemplarRepository).delete(ejemplar);
    }

    @Test
    void darDeBajaEjemplarPrestadoLanzaExcepcion() {
        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        when(ejemplarRepository.findByCodigoInventario("INV-001")).thenReturn(Optional.of(ejemplar));

        assertThrows(EjemplarPrestadoException.class, () -> {
            servicio.darDeBajaEjemplar("INV-001");
        });

        verify(ejemplarRepository, never()).delete(any());
    }

    @Test
    void darDeBajaEjemplarInexistenteDa404() {
        when(ejemplarRepository.findByCodigoInventario("INV-999")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            servicio.darDeBajaEjemplar("INV-999");
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Ejemplar no encontrado"));
    }
}
