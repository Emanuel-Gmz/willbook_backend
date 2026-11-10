package willbook.backend.service.impl;

import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.model.Usuario;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    void cambiarPasswordActualIncorrectaLanzaExcepcion() {
        when(principal.getName()).thenReturn("emanuel@test.com");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        CambiarPasswordRequest request = new CambiarPasswordRequest("mal", "nueva", "nueva");

        assertThrows(PasswordInvalidaException.class, () -> servicio.cambiarPassword(request, principal));
    }

    @Test
    void actualizarPerfilExitoso() {
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
        PerfilUpdateDTO updateDTO = new PerfilUpdateDTO(
                "Emanuel Modificado",
                "Gomez",
                "emanuel@test.com",
                "3794111111",
                "Calle Falsa 123",
                1L
        );

        when(localidadRepository.findById(1L)).thenReturn(Optional.of(edu.willbook_backend.model.Localidad.builder().id(1L).nombre("Capital").build()));

        PerfilResponseDTO resp = servicio.actualizarPerfil("emanuel@test.com", updateDTO);

        assertNotNull(resp);
        assertEquals("Emanuel Modificado", resp.nombre());
        verify(usuarioRepository).save(lector);
    }

    @Test
    void buscarLectoresConQueryRetornaLista() {
        when(usuarioRepository.buscarPorDniNombreOApellido("Emanuel")).thenReturn(List.of(lector));

        var resultados = servicio.buscarLectores("Emanuel");

        assertEquals(1, resultados.size());
        assertEquals("40123456", resultados.get(0).dni());
    }

    @Test
    void obtenerPerfilLectorCompletoExitoso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(lector));
        when(prestamoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(1L)).thenReturn(List.of());
        when(multaRepository.findAll()).thenReturn(List.of());

        var perfilCompleto = servicio.obtenerPerfilLector(1L);

        assertNotNull(perfilCompleto);
        assertEquals("Emanuel", perfilCompleto.nombre());
        assertTrue(perfilCompleto.cuentaActiva());
    }
}
