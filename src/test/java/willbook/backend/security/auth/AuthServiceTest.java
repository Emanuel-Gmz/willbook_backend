package willbook.backend.security.auth;

import edu.willbook_backend.dto.RegisterRequestDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.DniYaRegistradoException;
import edu.willbook_backend.exception.EmailYaRegistradoException;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.model.Localidad;
import edu.willbook_backend.model.PasswordResetToken;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.PasswordResetTokenRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.security.auth.AuthService;
import edu.willbook_backend.security.dto.AuthRequest;
import edu.willbook_backend.security.dto.AuthResponse;
import edu.willbook_backend.security.dto.OlvidoPasswordRequest;
import edu.willbook_backend.security.jwt.JwtService;
import edu.willbook_backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock LocalidadRepository localidadRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock PasswordResetTokenRepository resetTokenRepository;
    @Mock EmailService emailService;
    @InjectMocks
    AuthService authService;

    Usuario lector;
    AuthRequest loginRequest;

    @BeforeEach
    void setUp() {
        lector = Usuario.builder()
                .id(1L)
                .nombre("Emanuel")
                .apellido("Gomez")
                .dni("40123456")
                .email("emanuel@test.com")
                .password("hash")
                .rol(Rol.LECTOR)
                .activo(true)
                .build();

        loginRequest = new AuthRequest("emanuel@test.com", "clave1234");

        when(jwtService.generateToken(anyMap(), any())).thenReturn("jwt-token-valido");
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.findByEmail("emanuel@test.com")).thenReturn(Optional.of(lector));
    }

    // ---------- Authenticate (Login) ----------

    @Test
    void autenticarDevuelveAuthResponseConToken() {
        AuthResponse response = authService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token-valido", response.token());
        assertEquals(Rol.LECTOR, response.rol());
        assertEquals("Emanuel Gomez", response.nombreCompleto());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void autenticarUsuarioNoEncontradoDa404() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        AuthRequest requestInvalido = new AuthRequest("noexiste@test.com", "clave1234");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            authService.authenticate(requestInvalido);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Usuario no encontrado", ex.getReason());
    }

    // ---------- Register ----------

    private RegisterRequestDTO crearRegistroDTO() {
        return new RegisterRequestDTO(
                "Carlos",
                "Perez",
                "39888777",
                "carlos@test.com",
                "clave1234",
                "clave1234",
                1L
        );
    }

    @Test
    void registrarUsuarioConExito() {
        RegisterRequestDTO dto = crearRegistroDTO();
        Localidad loc = Localidad.builder().id(1L).nombre("Localidad Test").build();

        when(usuarioRepository.existsByDni(dto.dni())).thenReturn(false);
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(localidadRepository.findById(dto.localidadId())).thenReturn(Optional.of(loc));

        AuthResponse response = authService.register(dto);

        assertNotNull(response);
        assertEquals("jwt-token-valido", response.token());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(usuarioRepository).flush();
    }

    @Test
    void registrarConPasswordsDistintasLanzaExcepcion() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Carlos", "Perez", "39888777", "carlos@test.com", "clave1", "clave2", 1L
        );

        assertThrows(PasswordInvalidaException.class, () -> {
            authService.register(dto);
        });
    }

    @Test
    void registrarDniDuplicadoLanzaExcepcion() {
        RegisterRequestDTO dto = crearRegistroDTO();
        when(usuarioRepository.existsByDni(dto.dni())).thenReturn(true);

        assertThrows(DniYaRegistradoException.class, () -> {
            authService.register(dto);
        });
    }

    @Test
    void registrarEmailDuplicadoLanzaExcepcion() {
        RegisterRequestDTO dto = crearRegistroDTO();
        when(usuarioRepository.existsByDni(dto.dni())).thenReturn(false);
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(EmailYaRegistradoException.class, () -> {
            authService.register(dto);
        });
    }

    // ---------- Request Password Reset (Olvido de contraseña) ----------

    @Test
    void solicitarRecuperacionPasswordUsuarioInexistenteNoHaceNada() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            authService.requestPasswordReset(new OlvidoPasswordRequest("nadie@test.com"));
        });

        verify(resetTokenRepository, never()).save(any());
    }

    @Test
    void solicitarRecuperacionPasswordEnviaCorreoYGuardaToken() {
        OlvidoPasswordRequest request = new OlvidoPasswordRequest("emanuel@test.com");
        when(resetTokenRepository.findByUsuario(lector)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            authService.requestPasswordReset(request);
        });

        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("emanuel@test.com"), anyString());
    }
}
