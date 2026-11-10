package willbook.backend.security.jwt;

import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.security.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    // Clave Base64 válida de 32 bytes para HS256
    static final String CLAVE = "dGVzdC1zZWNyZXQta2V5LXdpbGxib29rLXNvbG8tcGFyYS10ZXN0cy1zZWN1cmU=";

    JwtService jwtService = new JwtService();

    Usuario lector = Usuario.builder()
            .id(1L)
            .email("emanuel@test.com")
            .rol(Rol.LECTOR)
            .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", CLAVE);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);
    }

    @Test
    void generaYExtraeUsernameDelToken() {
        String token = jwtService.generateToken(Map.of("rol", "LECTOR"), lector);

        assertEquals("emanuel@test.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, lector));

        Usuario otroUsuario = Usuario.builder().email("otro@test.com").build();
        assertFalse(jwtService.isTokenValid(token, otroUsuario));
    }

    @Test
    void tokenVencidoLanzaExcepcion() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1_000L);
        String tokenVencido = jwtService.generateToken(lector);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenValid(tokenVencido, lector);
        });
    }
}
