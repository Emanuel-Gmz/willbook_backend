package edu.willbook_backend.security.jwt;

import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    JwtService jwtService;
    @Mock UserDetailsService userDetailsService;
    @InjectMocks
    JwtAuthFilter jwtAuthFilter;

    MockHttpServletResponse respuesta = new MockHttpServletResponse();
    MockFilterChain cadena = new MockFilterChain();

    Usuario lector = Usuario.builder()
            .id(1L)
            .email("emanuel@test.com")
            .rol(Rol.LECTOR)
            .build();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest pedido(String uri, String header) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
        req.setRequestURI(uri);
        if (header != null) {
            req.addHeader("Authorization", header);
        }
        return req;
    }

    @Test
    void sinHeaderAuthorizationPasaSinAutenticar() throws Exception {
        jwtAuthFilter.doFilter(pedido("/api/v1/catalogo", null), respuesta, cadena);
        assertNotNull(cadena.getRequest());
        verifyNoInteractions(jwtService);
    }

    @Test
    void headerAuthorizationSinBearerIgnoraToken() throws Exception {
        // Cubre la rama donde el header no comienza con "Bearer " (ej. Basic token)
        jwtAuthFilter.doFilter(pedido("/api/v1/catalogo", "Basic dXNlcjpwYXNz"), respuesta, cadena);
        assertNotNull(cadena.getRequest());
        verifyNoInteractions(jwtService);
    }

    @Test
    void tokenValidoAutenticaEnContexto() throws Exception {
        when(jwtService.extractUsername("tok-valido")).thenReturn("emanuel@test.com");
        when(userDetailsService.loadUserByUsername("emanuel@test.com")).thenReturn(lector);
        when(jwtService.isTokenValid("tok-valido", lector)).thenReturn(true);

        jwtAuthFilter.doFilter(pedido("/api/v1/catalogo", "Bearer tok-valido"), respuesta, cadena);

        assertSame(lector, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertNotNull(cadena.getRequest());
    }

    @Test
    void tokenInvalidoNoAutentica() throws Exception {
        when(jwtService.extractUsername("tok-invalido")).thenReturn("emanuel@test.com");
        when(userDetailsService.loadUserByUsername("emanuel@test.com")).thenReturn(lector);
        when(jwtService.isTokenValid("tok-invalido", lector)).thenReturn(false);

        jwtAuthFilter.doFilter(pedido("/api/v1/catalogo", "Bearer tok-invalido"), respuesta, cadena);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}