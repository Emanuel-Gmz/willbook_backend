package edu.willbook_backend.security.auth;

import edu.willbook_backend.dto.RegisterRequestDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.security.dto.AuthRequest;
import edu.willbook_backend.security.dto.AuthResponse;
import edu.willbook_backend.security.dto.OlvidoPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginDevuelveTokenYOk() throws Exception {
        AuthResponse response = new AuthResponse("jwt-token-123", "Inicio de sesión exitoso", Rol.LECTOR, "Juan Pérez");
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        String json = "{\"username\":\"juan@test.com\",\"password\":\"password123\"}";

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.message").value("Inicio de sesión exitoso"));
    }

    @Test
    void registerDevuelveCreated() throws Exception {
        AuthResponse response = new AuthResponse("jwt-token-456", "Registro exitoso", Rol.LECTOR, "Ana Gómez");
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        String json = "{"
                + "\"nombre\":\"Ana\","
                + "\"apellido\":\"Gómez\","
                + "\"dni\":\"12345678\","
                + "\"email\":\"ana@test.com\","
                + "\"password\":\"password123\","
                + "\"confirmacionPassword\":\"password123\","
                + "\"localidadId\":1"
                + "}";

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.message").value("Registro exitoso"));
    }

    @Test
    void forgotPasswordDevuelveOk() throws Exception {
        String json = "{\"email\":\"juan@test.com\"}";

        mvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Si tu correo está registrado, recibirás las instrucciones para restablecer tu contraseña"));
    }
}