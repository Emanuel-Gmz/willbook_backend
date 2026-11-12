package edu.willbook_backend.impl;

import edu.willbook_backend.dto.MultaCancelacionDTO;
import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.MetodoPago;
import edu.willbook_backend.exception.MultaNoActivaException;
import edu.willbook_backend.exception.MultaYaProcesadaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.impl.MultaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultaServiceImplTest {

    @Mock MultaRepository multaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks
    MultaServiceImpl servicio;

    Usuario usuario;
    Multa multa;

    @BeforeEach
    void armar() {
        usuario = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").email("juan@test.com").build();
        multa = Multa.builder().id(1L).usuario(usuario).monto(BigDecimal.TEN).estado(EstadoMulta.PENDIENTE).fechaGeneracion(LocalDateTime.now()).build();
    }

    @Test
    void crearMultaManualExitosa() {
        MultaCreateDTO dto = new MultaCreateDTO(1L, "Demora", BigDecimal.TEN);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(multaRepository.save(any())).thenReturn(multa);

        Map<String, Object> resp = servicio.crearMultaManual(dto);

        assertEquals("Multa cargada exitosamente", resp.get("message"));
        verify(multaRepository).save(any());
    }

    @Test
    void crearMultaManualUsuarioNoEncontradoLanzaExcepcion() {
        MultaCreateDTO dto = new MultaCreateDTO(99L, "Demora", BigDecimal.TEN);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.crearMultaManual(dto));
    }

    @Test
    void cancelarMultaExitosoConRehabilitacion() {
        MultaCancelacionDTO dto = new MultaCancelacionDTO("Anular");
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);

        Map<String, Object> resp = servicio.cancelarMulta(1L, dto);

        assertEquals(EstadoMulta.CANCELADA.name(), resp.get("nuevoEstado"));
        assertTrue(((String) resp.get("message")).contains("rehabilitada"));
    }

    @Test
    void cancelarMultaExitosoSinRehabilitacion() {
        MultaCancelacionDTO dto = new MultaCancelacionDTO("Anular");
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(true);

        Map<String, Object> resp = servicio.cancelarMulta(1L, dto);

        assertEquals(EstadoMulta.CANCELADA.name(), resp.get("nuevoEstado"));
    }

    @Test
    void cancelarMultaNoEncontradaLanza404() {
        when(multaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.cancelarMulta(99L, new MultaCancelacionDTO("test")));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void cancelarMultaYaProcesadaLanzaExcepcion() {
        multa.setEstado(EstadoMulta.PAGADA);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));

        assertThrows(MultaYaProcesadaException.class, () -> servicio.cancelarMulta(1L, new MultaCancelacionDTO("test")));
    }

    @Test
    void registrarPagoMultaActivaExitoso() {
        MultaPagoDTO pagoDTO = new MultaPagoDTO(MetodoPago.EFECTIVO);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);

        Map<String, Object> resp = servicio.registrarPagoMulta(1L, pagoDTO);

        assertEquals(EstadoMulta.PAGADA.name(), resp.get("estado"));
        verify(multaRepository).save(multa);
    }

    @Test
    void registrarPagoMultaConOtrasPendientes() {
        MultaPagoDTO pagoDTO = new MultaPagoDTO(MetodoPago.TARJETA_CREDITO);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(true);

        Map<String, Object> resp = servicio.registrarPagoMulta(1L, pagoDTO);

        assertEquals(EstadoMulta.PAGADA.name(), resp.get("estado"));
        assertTrue((Boolean) resp.get("usuarioBloqueado"));
    }

    @Test
    void registrarPagoMultaNoEncontradaLanza404() {
        when(multaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.registrarPagoMulta(99L, new MultaPagoDTO(MetodoPago.EFECTIVO)));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void registrarPagoMultaNoActivaLanzaExcepcion() {
        multa.setEstado(EstadoMulta.PAGADA);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));

        assertThrows(MultaNoActivaException.class, () -> servicio.registrarPagoMulta(1L, new MultaPagoDTO(MetodoPago.EFECTIVO)));
    }

    @Test
    void consultarMisMultasVacias() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.findByUsuarioIdOrderByFechaGeneracionDesc(1L)).thenReturn(List.of());

        var misMultas = servicio.consultarMisMultas("juan@test.com");
        assertFalse(misMultas.cuentaRestringida());
    }

    @Test
    void consultarMisMultasConPrestamoYResuelta() {
        Libro libro = Libro.builder().titulo("Java Avanzado").build();
        Ejemplar ejemplar = Ejemplar.builder().libro(libro).build();
        Prestamo prestamo = Prestamo.builder()
                .ejemplar(ejemplar)
                .fechaDevolucionEsperada(LocalDateTime.now().minusDays(5))
                .fechaDevolucionReal(LocalDateTime.now().minusDays(2))
                .build();
        multa.setPrestamo(prestamo);
        multa.setEstado(EstadoMulta.PAGADA);

        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.findByUsuarioIdOrderByFechaGeneracionDesc(1L)).thenReturn(List.of(multa));

        var misMultas = servicio.consultarMisMultas("juan@test.com");
        assertNotNull(misMultas);
        assertFalse(misMultas.cuentaRestringida());
    }

    @Test
    void consultarMisMultasConAtrasoSinDevolucionReal() {
        Libro libro = Libro.builder().titulo("Java Avanzado").build();
        Ejemplar ejemplar = Ejemplar.builder().libro(libro).build();
        Prestamo prestamo = Prestamo.builder()
                .ejemplar(ejemplar)
                .fechaDevolucionEsperada(LocalDateTime.now().minusDays(3))
                .build();
        multa.setPrestamo(prestamo);
        multa.setEstado(EstadoMulta.PENDIENTE);

        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.findByUsuarioIdOrderByFechaGeneracionDesc(1L)).thenReturn(List.of(multa));

        var misMultas = servicio.consultarMisMultas("juan@test.com");
        assertNotNull(misMultas);
        assertTrue(misMultas.cuentaRestringida());
    }

    @Test
    void consultarMisMultasUsuarioNoEncontradoLanzaExcepcion() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.consultarMisMultas("noexiste@test.com"));
    }
}