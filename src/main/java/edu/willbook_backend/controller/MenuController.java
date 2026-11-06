package edu.willbook_backend.controller;

import edu.willbook_backend.dto.MenuItemDTO;
import edu.willbook_backend.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "Obtener opciones del menú de navegación")
    public ResponseEntity<List<MenuItemDTO>> obtenerMenu(Principal principal) {
        List<MenuItemDTO> menuItems = menuService.obtenerMenuPorUsuario(principal);
        return ResponseEntity.ok(menuItems);
    }
}