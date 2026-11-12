package edu.willbook_backend.impl;

import edu.willbook_backend.dto.MenuItemDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock Principal principal;
    @InjectMocks
    MenuServiceImpl servicio;

    @BeforeEach
    void setUp() {
        when(principal.getName()).thenReturn("test@willbook.com");
    }

    @Test
    void obtenerMenuParaLector() {
        Usuario lector = Usuario.builder().email("test@willbook.com").rol(Rol.LECTOR).build();
        when(usuarioRepository.findByEmail("test@willbook.com")).thenReturn(Optional.of(lector));

        List<MenuItemDTO> menu = servicio.obtenerMenuPorUsuario(principal);

        assertFalse(menu.isEmpty());
        assertEquals("/catalogo", menu.get(1).path());
    }

    @Test
    void obtenerMenuParaAdmin() {
        Usuario admin = Usuario.builder().email("test@willbook.com").rol(Rol.ADMIN).build();
        when(usuarioRepository.findByEmail("test@willbook.com")).thenReturn(Optional.of(admin));

        List<MenuItemDTO> menu = servicio.obtenerMenuPorUsuario(principal);

        assertFalse(menu.isEmpty());
        assertEquals("/dashboard", menu.get(0).path());
    }

    @Test
    void usuarioNoEncontradoDa404() {
        when(usuarioRepository.findByEmail("test@willbook.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> servicio.obtenerMenuPorUsuario(principal));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}