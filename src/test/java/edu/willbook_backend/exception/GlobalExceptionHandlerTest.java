package edu.willbook_backend.exception;

import edu.willbook_backend.controller.HomeController;
import edu.willbook_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

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
    void manejaResponseStatusException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No encontrado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No encontrado"));
    }

    @Test
    void manejaBadCredentialsException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new BadCredentialsException("Bad credentials"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void manejaEmailYaRegistradoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new EmailYaRegistradoException("Email duplicado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void manejaDniYaRegistradoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new DniYaRegistradoException("DNI duplicado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaPasswordInvalidaException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new PasswordInvalidaException("Password inválida"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manejaIsbnDuplicadoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new IsbnDuplicadoException("ISBN duplicado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaCategoriaDuplicadaException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new CategoriaDuplicadaException("Categoría duplicada"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaAutorDuplicadoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new AutorDuplicadoException("Autor duplicado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaAutorConLibrosException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new AutorConLibrosException("Autor con libros"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaAutorNoEncontradoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new AutorNoEncontradoException("Autor no encontrado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void manejaCategoriaNoEncontradaException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new CategoriaNoEncontradaException("Categoría no encontrada"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void manejaLibroSinStockException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new LibroSinStockException("Sin stock"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manejaUsuarioConMultasPendientesException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new UsuarioConMultasPendientesException("Multas pendientes"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isForbidden());
    }

    @Test
    void manejaEjemplarPrestadoException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new EjemplarPrestadoException("Ejemplar prestado"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaMultaYaProcesadaException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new MultaYaProcesadaException("Multa procesada"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }

    @Test
    void manejaMultaNoActivaException() throws Exception {
        when(principal.getName()).thenReturn("test@willbook.com");
        when(usuarioRepository.findByEmail(anyString())).thenThrow(new MultaNoActivaException("Multa no activa"));

        mvc.perform(get("/api/v1/home").principal(principal))
                .andExpect(status().isConflict());
    }
}