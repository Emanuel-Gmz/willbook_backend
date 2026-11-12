package edu.willbook_backend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void testGettersSettersYConstructores() {
        Usuario usuario = Usuario.builder().id(1L).email("test@test.com").build();
        LocalDateTime ahora = LocalDateTime.now();

        PasswordResetToken token = new PasswordResetToken();
        token.setId(10L);
        token.setToken("token-abc-123");
        token.setUsuario(usuario);
        token.setExpiryDate(ahora);

        assertEquals(10L, token.getId());
        assertEquals("token-abc-123", token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertEquals(ahora, token.getExpiryDate());

        // Test constructor con argumentos (si Lombok lo genera o constructor manual)
        PasswordResetToken tokenArgs = PasswordResetToken.builder()
                .id(1L)
                .token("token-xyz")
                .usuario(usuario)
                .expiryDate(ahora)
                .build();

        assertNotNull(tokenArgs);
        assertEquals("token-xyz", tokenArgs.getToken());
    }
}