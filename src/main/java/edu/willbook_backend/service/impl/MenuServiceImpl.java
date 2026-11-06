package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.MenuItemDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<MenuItemDTO> obtenerMenuPorUsuario(Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Rol rol = usuario.getRol();

        // Menú dinámico para el rol LECTOR (Escenario 1)
        if (rol == Rol.LECTOR) {
            return List.of(
                    new MenuItemDTO("Inicio", "/home", "home"),
                    new MenuItemDTO("Catálogo", "/catalogo", "book"),
                    new MenuItemDTO("Mis Préstamos", "/mis-prestamos", "calendar"),
                    new MenuItemDTO("Mi Perfil", "/perfil", "user")
            );
        }

        // Menú dinámico para roles BIBLIOTECARIO o ADMIN
        else if (rol == Rol.BIBLIOTECARIO || rol == Rol.ADMIN) {
            return List.of(
                    new MenuItemDTO("Dashboard", "/dashboard", "dashboard"),
                    new MenuItemDTO("Gestión de Libros", "/admin/libros", "bookshelf"),
                    new MenuItemDTO("Retiros Pendientes", "/admin/retiros", "clock"),
                    new MenuItemDTO("Gestión de Multas", "/admin/multas", "alert-triangle"),
                    new MenuItemDTO("Mi Perfil", "/perfil", "user")
            );
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rol no autorizado para visualizar el menú");
    }
}