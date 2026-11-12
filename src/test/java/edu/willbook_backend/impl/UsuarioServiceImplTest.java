package edu.willbook_backend.impl;

import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.PrestamoRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock LocalidadRepository localidadRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PrestamoRepository prestamoRepository;
    @Mock MultaRepository multaRepository;
    @Mock Principal principal;

    @InjectMocks
    UsuarioServiceImpl servicio;

    Usuario lector;

    @BeforeEach
    void armar() {
        lector = Usuario.builder()
                .id(1L)
                .nombre("Emanuel")
                .apellido("Gomez")
                .dni("40123456")
                .email("emanuel@test.com")
                .password("hash-password-actual")
                .rol(Rol.LECTOR)
                .activo(true)
                .build();
    }

    @Test
    void loadUserByUsernamePorEmailExitoso() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));

        UserDetails userDetails = servicio.loadUserByUsername("emanuel@test.com");

        assertNotNull(userDetails);
        assertEquals("emanuel@test.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsernamePorDniSiEmailNoExiste() {
        when(usuarioRepository.findByEmail("40123456")).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni("40123456")).thenReturn(Optional.of(lector));

        UserDetails userDetails = servicio.loadUserByUsername("40123456");

        assertNotNull(userDetails);
    }

    @Test
    void loadUserByUsernameInexistenteLanzaExcepcion() {
        when(usuarioRepository.findByEmail("nadie")).thenReturn(Optional.empty());
        when(usuarioRepository.findByDni("nadie")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> servicio.loadUserByUsername("nadie"));
    }

    @Test
    void cambiarPasswordExitoso() {
        when(principal.getName()).thenReturn("emanuel@test.com");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        when(passwordEncoder.matches("claveActual123", "hash-password-actual")).thenReturn(true);
        when(passwordEncoder.matches("claveNueva123", "hash-password-actual")).thenReturn(false);
        when(passwordEncoder.encode("claveNueva123")).thenReturn("hash-nueva");

        CambiarPasswordRequest request = new CambiarPasswordRequest(
                "claveActual123",
                "claveNueva123",
                "claveNueva123"
        );

        assertDoesNotThrow(() -> servicio.cambiarPassword(request, principal));
        verify(usuarioRepository).save(lector);
        assertEquals("hash-nueva", lector.getPassword());
    }

    @Test
    void cambiarPasswordUsuarioNoEncontradoLanzaExcepcion() {
        when(principal.getName()).thenReturn("nadie@test.com");
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        CambiarPasswordRequest request = new CambiarPasswordRequest("a", "b", "b");
        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.cambiarPassword(request, principal));
    }

    @Test
    void cambiarPasswordActualIncorrectaLanzaExcepcion() {
        when(principal.getName()).thenReturn("emanuel@test.com");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        CambiarPasswordRequest request = new CambiarPasswordRequest("mal", "nueva", "nueva");

        assertThrows(PasswordInvalidaException.class, () -> servicio.cambiarPassword(request, principal));
    }

    @Test
    void cambiarPasswordNoCoincidenConfirmacionLanzaExcepcion() {
        when(principal.getName()).thenReturn("emanuel@test.com");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        when(passwordEncoder.matches("claveActual123", "hash-password-actual")).thenReturn(true);

        CambiarPasswordRequest request = new CambiarPasswordRequest("claveActual123", "nueva123", "diferente123");

        assertThrows(PasswordInvalidaException.class, () -> servicio.cambiarPassword(request, principal));
    }

    @Test
    void cambiarPasswordIgualALaAnteriorLanzaExcepcion() {
        when(principal.getName()).thenReturn("emanuel@test.com");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        when(passwordEncoder.matches("claveActual123", "hash-password-actual")).thenReturn(true);
        when(passwordEncoder.matches("claveActual123", lector.getPassword())).thenReturn(true);

        CambiarPasswordRequest request = new CambiarPasswordRequest("claveActual123", "claveActual123", "claveActual123");

        assertThrows(PasswordInvalidaException.class, () -> servicio.cambiarPassword(request, principal));
    }

    @Test
    void actualizarPerfilExitoso() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        PerfilUpdateDTO updateDTO = new PerfilUpdateDTO(
                "Emanuel Modificado",
                "Gomez",
                "nuevo@test.com",
                "3794111111",
                "Calle Falsa 123",
                1L
        );

        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(localidadRepository.findById(1L)).thenReturn(Optional.of(Localidad.builder().id(1L).nombre("Capital").build()));

        PerfilResponseDTO resp = servicio.actualizarPerfil("emanuel@test.com", updateDTO);

        assertNotNull(resp);
        assertEquals("Emanuel Modificado", resp.nombre());
        verify(usuarioRepository).save(lector);
    }

    @Test
    void actualizarPerfilUsuarioNoEncontradoLanza404() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicio.actualizarPerfil("nadie@test.com", new PerfilUpdateDTO("A", "B", "c", "d", "e", 1L))
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void actualizarPerfilEmailDuplicadoLanzaConflict() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        PerfilUpdateDTO updateDTO = new PerfilUpdateDTO(null, null, "duplicado@test.com", null, null, null);

        when(usuarioRepository.existsByEmail("duplicado@test.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicio.actualizarPerfil("emanuel@test.com", updateDTO)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void actualizarPerfilLocalidadNoEncontradaLanza404() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        PerfilUpdateDTO updateDTO = new PerfilUpdateDTO(null, null, null, null, null, 99L);

        when(localidadRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                servicio.actualizarPerfil("emanuel@test.com", updateDTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void buscarLectoresConQueryRetornaLista() {
        when(usuarioRepository.buscarPorDniNombreOApellido("Emanuel")).thenReturn(List.of(lector));

        var resultados = servicio.buscarLectores("Emanuel");

        assertEquals(1, resultados.size());
        assertEquals("40123456", resultados.get(0).dni());
    }

    @Test
    void buscarLectoresSinQueryRetornaTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(lector));

        var resultados = servicio.buscarLectores("");

        assertEquals(1, resultados.size());
    }

    @Test
    void obtenerPerfilLectorCompletoExitoso() {
        Libro libro = Libro.builder().id(1L).titulo("Test").portadaUrl("url").build();
        Ejemplar ejemplar = Ejemplar.builder().id(1L).libro(libro).codigoInventario("INV-001").build();

        Prestamo prestamoPendiente = Prestamo.builder()
                .id(1L).usuario(lector).ejemplar(ejemplar)
                .estado(EstadoPrestamo.PENDIENTE_RETIRO)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        Prestamo prestamoVencido = Prestamo.builder()
                .id(2L).usuario(lector).ejemplar(ejemplar)
                .estado(EstadoPrestamo.PRESTADO)
                .fechaSolicitud(LocalDateTime.now().minusDays(20))
                .fechaDevolucionEsperada(LocalDateTime.now().minusDays(5))
                .build();

        Prestamo prestamoDevuelto = Prestamo.builder()
                .id(3L).usuario(lector).ejemplar(ejemplar)
                .estado(EstadoPrestamo.DEVUELTO)
                .fechaSolicitud(LocalDateTime.now().minusDays(30))
                .build();

        Multa multa = Multa.builder()
                .id(1L).usuario(lector).motivo("Demora").monto(BigDecimal.TEN)
                .estado(EstadoMulta.PENDIENTE).fechaGeneracion(LocalDateTime.now())
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
        when(prestamoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(1L)).thenReturn(List.of(prestamoPendiente, prestamoVencido, prestamoDevuelto));
        when(multaRepository.findAll()).thenReturn(List.of(multa));

        var perfilCompleto = servicio.obtenerPerfilLector(1L);

        assertNotNull(perfilCompleto);
        assertEquals("Emanuel", perfilCompleto.nombre());
        assertFalse(perfilCompleto.cuentaActiva()); // Bloqueada por multa pendiente
        assertEquals(2, perfilCompleto.prestamosActivos().size());
        assertEquals(1, perfilCompleto.historialPrestamos().size());
    }

    @Test
    void obtenerPerfilLectorUsuarioNoEncontradoLanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> servicio.obtenerPerfilLector(99L));
    }

    @Test
    void buscarLectoresConQueryEnBlancoRetornaTodos() {
        when(usuarioRepository.findAll()).thenReturn(List.of(lector));

        var resultados = servicio.buscarLectores("   ");

        assertFalse(resultados.isEmpty());
        verify(usuarioRepository).findAll();
    }

    @Test
    void actualizarPerfilCamposNulosOVaciosNoModificaDatos() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));


        PerfilUpdateDTO updateDTO = new PerfilUpdateDTO(
                "   ",
                "",
                null,
                null,
                null,
                null
        );

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(lector);

        PerfilResponseDTO resp = servicio.actualizarPerfil("emanuel@test.com", updateDTO);

        assertNotNull(resp);
        verify(usuarioRepository).save(lector);
    }
}