package edu.willbook_backend.service;

import edu.willbook_backend.dto.MenuItemDTO;
import java.security.Principal;
import java.util.List;

public interface MenuService {
    List<MenuItemDTO> obtenerMenuPorUsuario(Principal principal);
}