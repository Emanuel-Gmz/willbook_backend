package edu.willbook_backend.model;

import edu.willbook_backend.enums.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void testGettersSettersYBuilder() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .email("juan@test.com")
                .password("secret")
                .rol(Rol.LECTOR)
                .activo(true)
                .build();

        assertEquals(1L, usuario.getId());
        assertEquals("Juan", usuario.getNombre());
        assertEquals("Perez", usuario.getApellido());
        assertEquals("12345678", usuario.getDni());
        assertEquals("juan@test.com", usuario.getEmail());
        assertEquals("secret", usuario.getPassword());
        assertEquals(Rol.LECTOR, usuario.getRol());
        assertTrue(usuario.getActivo());

        // Validar contratos de UserDetails de Spring Security
        assertEquals("juan@test.com", usuario.getUsername());
        assertNotNull(usuario.getAuthorities());
        assertTrue(usuario.isAccountNonExpired());
        assertTrue(usuario.isAccountNonLocked());
        assertTrue(usuario.isCredentialsNonExpired());
        assertTrue(usuario.isEnabled());
    }

    @Test
    void testConstructorVacioYSetters() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Ana");
        usuario.setApellido("Gomez");
        usuario.setDni("87654321");
        usuario.setEmail("ana@test.com");
        usuario.setPassword("1234");
        usuario.setRol(Rol.ADMIN);
        usuario.setActivo(false);

        assertEquals(2L, usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("Gomez", usuario.getApellido());
        assertEquals("87654321", usuario.getDni());
        assertEquals("ana@test.com", usuario.getEmail());
        assertEquals("1234", usuario.getPassword());
        assertEquals(Rol.ADMIN, usuario.getRol());
        assertFalse(usuario.getActivo());
    }
}