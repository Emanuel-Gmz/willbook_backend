package edu.willbook_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.willbook_backend.dto.PerfilLectorCompletoDTO;
import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.dto.UsuarioBusquedaResponseDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Mock
    private Principal principal;

    private MockMvc mvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void cambiarPasswordExitoso() throws Exception {
        when(principal.getName()).thenReturn("usuario@willbook.com");
        doNothing().when(usuarioService).cambiarPassword(any(CambiarPasswordRequest.class), eq(principal));

        // Enviamos los tres campos requeridos y con más de 8 caracteres para superar las validaciones (@Size y @NotBlank)
        Map<String, String> requestMap = Map.of(
                "currentPassword", "oldPassword123",
                "newPassword", "newPassword123",
                "confirmationPassword", "newPassword123"
        );
        String json = objectMapper.writeValueAsString(requestMap);

        mvc.perform(patch("/api/v1/usuarios/cambiar-password")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada correctamente"));
    }

    @Test
    void actualizarPerfilExitoso() throws Exception {
        when(principal.getName()).thenReturn("usuario@willbook.com");

        PerfilResponseDTO responseDTO = new PerfilResponseDTO(
                1L, "Mariana", "Benítez", "35987654", "mariana@test.com", "123456789", "Calle 100", Rol.LECTOR, "Santa Fe"
        );

        when(usuarioService.actualizarPerfil(eq("usuario@willbook.com"), any(PerfilUpdateDTO.class)))
                .thenReturn(responseDTO);

        Map<String, String> requestMap = Map.of(
                "nombre", "Mariana",
                "apellido", "Benítez"
        );
        String json = objectMapper.writeValueAsString(requestMap);

        mvc.perform(put("/api/v1/usuarios/perfil")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Perfil actualizado con éxito"))
                .andExpect(jsonPath("$.perfil.nombre").value("Mariana"));
    }

    @Test
    void buscarLectoresDevuelveLista() throws Exception {
        UsuarioBusquedaResponseDTO busquedaDTO = new UsuarioBusquedaResponseDTO(
                1L, "Mariana", "Benítez", "35987654", "mariana@test.com", true
        );
        when(usuarioService.buscarLectores("Mariana")).thenReturn(List.of(busquedaDTO));

        mvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "Mariana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Mariana"));
    }

    @Test
    void obtenerPerfilLectorExitoso() throws Exception {
        PerfilLectorCompletoDTO perfilDTO = new PerfilLectorCompletoDTO(
                1L, "Mariana", "Benítez", "35987654", "mariana@test.com", "123456789", "Calle 100", "Santa Fe", true, List.of(), List.of(), List.of()
        );
        when(usuarioService.obtenerPerfilLector(1L)).thenReturn(perfilDTO);

        mvc.perform(get("/api/v1/usuarios/1/perfil"))
                .andExpect(status().isOk());
    }
}