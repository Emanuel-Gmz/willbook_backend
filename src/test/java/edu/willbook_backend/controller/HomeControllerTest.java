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
    void obtenerInicioLectorDevuelveCatalogo() throws Exception {
        when(principal.getName()).thenReturn("lector@willbook.com");
        Usuario lector = Usuario.builder().id(1L).email("lector@willbook.com").rol(Rol.LECTOR).build();
        when(usuarioRepository.findByEmail("lector@willbook.com")).thenReturn(Optional.of(lector));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vista").value("CATALOGO"));
    }

    @Test
    void obtenerInicioAdminDevuelveDashboard() throws Exception {
        when(principal.getName()).thenReturn("admin@willbook.com");
        Usuario admin = Usuario.builder().id(2L).email("admin@willbook.com").rol(Rol.ADMIN).build();
        when(usuarioRepository.findByEmail("admin@willbook.com")).thenReturn(Optional.of(admin));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vista").value("DASHBOARD"));
    }

    @Test
    void obtenerInicioBibliotecarioDevuelveDashboard() throws Exception {
        when(principal.getName()).thenReturn("biblio@willbook.com");
        Usuario biblio = Usuario.builder().id(3L).email("biblio@willbook.com").rol(Rol.BIBLIOTECARIO).build();
        when(usuarioRepository.findByEmail("biblio@willbook.com")).thenReturn(Optional.of(biblio));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vista").value("DASHBOARD"));
    }

    @Test
    void obtenerInicioUsuarioNoEncontradoLanza404() throws Exception {
        when(principal.getName()).thenReturn("noexiste@willbook.com");
        when(usuarioRepository.findByEmail("noexiste@willbook.com")).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerInicioRolNoAutorizadoLanza403() throws Exception {
        when(principal.getName()).thenReturn("otro@willbook.com");
        Usuario usuarioSinRolValido = Usuario.builder().id(4L).email("otro@willbook.com").rol(null).build();
        when(usuarioRepository.findByEmail("otro@willbook.com")).thenReturn(Optional.of(usuarioSinRolValido));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isForbidden());
    }
}