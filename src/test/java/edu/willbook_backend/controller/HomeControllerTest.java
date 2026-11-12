package edu.willbook_backend.controller;

import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HomeController homeController;

    @Mock
    private Principal principal;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(homeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerInicioParaLectorDevuelveOk() throws Exception {
        Usuario lector = Usuario.builder()
                .email("lector@willbook.com")
                .rol(Rol.LECTOR)
                .build();

        when(principal.getName()).thenReturn("lector@willbook.com");
        when(usuarioRepository.findByEmail("lector@willbook.com")).thenReturn(Optional.of(lector));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vista").value("CATALOGO"))
                .andExpect(jsonPath("$.mensaje").value("Bienvenido al catálogo de libros de WillBook"));
    }

    @Test
    void obtenerInicioParaAdminDevuelveOk() throws Exception {
        Usuario admin = Usuario.builder()
                .email("admin@willbook.com")
                .rol(Rol.ADMIN)
                .build();

        when(principal.getName()).thenReturn("admin@willbook.com");
        when(usuarioRepository.findByEmail("admin@willbook.com")).thenReturn(Optional.of(admin));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vista").value("DASHBOARD"))
                .andExpect(jsonPath("$.mensaje").value("Panel de control administrativo"));
    }

    @Test
    void obtenerInicioParaBibliotecarioDevuelveOk() throws Exception {
        Usuario biblio = Usuario.builder()
                .email("biblio@willbook.com")
                .rol(Rol.BIBLIOTECARIO)
                .build();

        when(principal.getName()).thenReturn("biblio@willbook.com");
        when(usuarioRepository.findByEmail("biblio@willbook.com")).thenReturn(Optional.of(biblio));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerInicioUsuarioNoEncontradoDevuelve404() throws Exception {
        when(principal.getName()).thenReturn("noexiste@willbook.com");
        when(usuarioRepository.findByEmail("noexiste@willbook.com")).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isNotFound());
    }
}