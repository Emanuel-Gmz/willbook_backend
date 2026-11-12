package edu.willbook_backend.impl;

import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.LibroSinStockException;
import edu.willbook_backend.exception.UsuarioConMultasPendientesException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.*;
import edu.willbook_backend.service.impl.PrestamoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
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
    Prestamo prestamo;

    @BeforeEach
    void armar() {
        usuario = Usuario.builder().id(1L).email("lector@test.com").rol(Rol.LECTOR).nombre("Juan").apellido("Perez").build();
        libro = Libro.builder().id(1L).titulo("Libro Test").portadaUrl("url").build();
        ejemplar = Ejemplar.builder().id(1L).libro(libro).codigoInventario("INV-001").estado(EstadoEjemplar.DISPONIBLE).build();
        prestamo = Prestamo.builder()
                .id(1L)
                .usuario(usuario)
                .ejemplar(ejemplar)
                .estado(EstadoPrestamo.PENDIENTE_RETIRO)
                .fechaSolicitud(LocalDateTime.now())
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void solicitarRetiroExitoso() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(ejemplarRepository.findFirstByLibroIdAndEstado(1L, EstadoEjemplar.DISPONIBLE)).thenReturn(Optional.of(ejemplar));
        when(prestamoRepository.save(any())).thenReturn(prestamo);

        var response = servicio.solicitarRetiro(dto, "lector@test.com");

        assertNotNull(response);
        assertEquals(EstadoPrestamo.PENDIENTE_RETIRO, response.estado());
        verify(prestamoRepository).save(any());
    }

    @Test
    void solicitarRetiroUsuarioNoEncontradoLanzaExcepcion() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.solicitarRetiro(dto, "noexiste@test.com"));
    }

    @Test
    void solicitarRetiroConMultaPendienteLanzaExcepcion() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(1L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(true);

        assertThrows(UsuarioConMultasPendientesException.class, () -> servicio.solicitarRetiro(dto, "lector@test.com"));
    }

    @Test
    void solicitarRetiroLibroNoEncontradoLanzaExcepcion() {
        PrestamoRequestDTO dto = new PrestamoRequestDTO(99L);
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.solicitarRetiro(dto, "lector@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
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

    @Test
    void listarSolicitudesPendientesDevuelveLista() {
        when(prestamoRepository.findByEstado(EstadoPrestamo.PENDIENTE_RETIRO)).thenReturn(List.of(prestamo));

        var lista = servicio.listarSolicitudesPendientes();

        assertFalse(lista.isEmpty());
        assertEquals(1, lista.size());
    }

    @Test
    void confirmarPrestamoExitoso() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any())).thenReturn(prestamo);
        when(ejemplarRepository.save(any())).thenReturn(ejemplar);

        var resp = servicio.confirmarPrestamo(1L);

        assertEquals("Préstamo registrado exitosamente", resp.get("message"));
        assertEquals(EstadoPrestamo.PRESTADO, prestamo.getEstado());
    }

    @Test
    void confirmarPrestamoNoEncontradoLanzaExcepcion() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.confirmarPrestamo(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void confirmarPrestamoEstadoInvalidoLanzaExcepcion() {
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.confirmarPrestamo(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cancelarReservaExitoso() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any())).thenReturn(prestamo);
        when(ejemplarRepository.save(any())).thenReturn(ejemplar);

        var resp = servicio.cancelarReserva(1L);

        assertEquals("Reserva cancelada y ejemplar liberado exitosamente", resp.get("message"));
        assertEquals(EstadoPrestamo.CANCELADO, prestamo.getEstado());
    }

    @Test
    void cancelarReservaNoEncontradaLanzaExcepcion() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.cancelarReserva(99L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void cancelarReservaEstadoInvalidoLanzaExcepcion() {
        prestamo.setEstado(EstadoPrestamo.PRESTADO);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.cancelarReserva(1L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void consultarMisPrestamosExitoso() {
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(prestamoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(1L)).thenReturn(List.of(prestamo));

        var lista = servicio.consultarMisPrestamos("lector@test.com");
        assertFalse(lista.isEmpty());
        assertEquals("Pendiente de Retiro", lista.get(0).estadoVisual());

        // Test con estado DEVUELTO
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        lista = servicio.consultarMisPrestamos("lector@test.com");
        assertEquals("Devuelto", lista.get(0).estadoVisual());

        // Test con estado PRESTADO y atrasado
        prestamo.setEstado(EstadoPrestamo.PRESTADO);
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().minusDays(1));
        lista = servicio.consultarMisPrestamos("lector@test.com");
        assertEquals("Vencido/Atrasado", lista.get(0).estadoVisual());
        assertTrue(lista.get(0).atrasado()); // Corregido de esAtrasado() a atrasado()

        // Test con estado PRESTADO vigente
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().plusDays(5));
        lista = servicio.consultarMisPrestamos("lector@test.com");
        assertEquals("Vigente", lista.get(0).estadoVisual());
        assertFalse(lista.get(0).atrasado()); // Corregido de esAtrasado() a atrasado()

        // Test con otro estado
        prestamo.setEstado(EstadoPrestamo.EXTRAVIADO);
        lista = servicio.consultarMisPrestamos("lector@test.com");
        assertEquals("EXTRAVIADO", lista.get(0).estadoVisual());
    }

    @Test
    void consultarMisPrestamosUsuarioNoEncontradoLanzaExcepcion() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.consultarMisPrestamos("noexiste@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void reportarLossOrDamageExitosoLectorPropietario() {
        prestamo.setEstado(EstadoPrestamo.PRESTADO);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));
        when(prestamoRepository.save(any())).thenReturn(prestamo);
        when(ejemplarRepository.save(any())).thenReturn(ejemplar);
        when(multaRepository.save(any())).thenReturn(new Multa());

        var resp = servicio.reportarLossOrDamage(1L, "lector@test.com");
        assertNotNull(resp);
        assertEquals(EstadoPrestamo.EXTRAVIADO, prestamo.getEstado());
    }

    @Test
    void reportarLossOrDamageBibliotecarioPermitido() {
        prestamo.setEstado(EstadoPrestamo.RETIRADO);
        Usuario biblio = Usuario.builder().id(2L).email("biblio@test.com").rol(Rol.BIBLIOTECARIO).build();
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findByEmail("biblio@test.com")).thenReturn(Optional.of(biblio));
        when(prestamoRepository.save(any())).thenReturn(prestamo);
        when(ejemplarRepository.save(any())).thenReturn(ejemplar);
        when(multaRepository.save(any())).thenReturn(new Multa());

        var resp = servicio.reportarLossOrDamage(1L, "biblio@test.com");
        assertNotNull(resp);
    }

    @Test
    void reportarLossOrDamagePrestamoNoEncontradoLanzaExcepcion() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.reportarLossOrDamage(99L, "lector@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void reportarLossOrDamageUsuarioNoEncontradoLanzaExcepcion() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.reportarLossOrDamage(1L, "noexiste@test.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void reportarLossOrDamageLectorAjenoForbiddenLanzaExcepcion() {
        Usuario ajeno = Usuario.builder().id(2L).email("ajeno@test.com").rol(Rol.LECTOR).build();
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findByEmail("ajeno@test.com")).thenReturn(Optional.of(ajeno));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.reportarLossOrDamage(1L, "ajeno@test.com"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void reportarLossOrDamageEstadoInvalidoLanzaExcepcion() {
        prestamo.setEstado(EstadoPrestamo.PENDIENTE_RETIRO);
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findByEmail("lector@test.com")).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.reportarLossOrDamage(1L, "lector@test.com"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}