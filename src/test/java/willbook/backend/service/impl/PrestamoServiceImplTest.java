package willbook.backend.service.impl;

import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.exception.LibroSinStockException;
import edu.willbook_backend.exception.UsuarioConMultasPendientesException;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.*;
import edu.willbook_backend.service.impl.PrestamoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceImplTest {

    @Mock PrestamoRepository prestamoRepository;
    @Mock EjemplarRepository ejemplarRepository;
    @Mock LibroRepository libroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock MultaRepository multaRepository;
    @InjectMocks
    PrestamoServiceImpl servicio;

    Usuario usuario;
    Libro libro;
    Ejemplar ejemplar;

    @BeforeEach
    void armar() {
        usuario = Usuario.builder().id(1L).email("lector@test.com").build();
        libro = Libro.builder().id(1L).titulo("Libro Test").build();
        ejemplar = Ejemplar.builder().id(1L).libro(libro).codigoInventario("INV-001").estado(EstadoEjemplar.DISPONIBLE).build();
    }

    @Test
    void solicitarRetiroExitoso() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findFirstByLibroIdAndEstado(1L, EstadoEjemplar.DISPONIBLE)).thenReturn(Optional.of(ejemplar));

        Prestamo prestamoGuardado = Prestamo.builder().id(1L).usuario(usuario).ejemplar(ejemplar).estado(EstadoPrestamo.PENDIENTE_RETIRO).build();
        when(prestamoRepository.save(any())).thenReturn(prestamoGuardado);

        var response = servicio.solicitarRetiro(dto, "lector@test.com");

        assertNotNull(response);
        assertEquals(EstadoPrestamo.PENDIENTE_RETIRO, response.estado());
        verify(prestamoRepository).save(any());
    }

    @Test
    void solicitarRetiroConMultaPendienteLanzaExcepcion() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(true);

        assertThrows(UsuarioConMultasPendientesException.class, () -> servicio.solicitarRetiro(dto, "lector@test.com"));
    }

    @Test
    void solicitarRetiroSinStockLanzaExcepcion() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findFirstByLibroIdAndEstado(1L, EstadoEjemplar.DISPONIBLE)).thenReturn(Optional.empty());

        assertThrows(LibroSinStockException.class, () -> servicio.solicitarRetiro(dto, "lector@test.com"));
    }
}