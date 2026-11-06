package edu.willbook_backend.service;

import edu.willbook_backend.dto.PerfilLectorCompletoDTO;
import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.dto.UsuarioBusquedaResponseDTO;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;

import java.security.Principal;
import java.util.List;

public interface UsuarioService {
    void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser);
    PerfilResponseDTO actualizarPerfil(String username, PerfilUpdateDTO request);
    List<UsuarioBusquedaResponseDTO> buscarLectores(String query);
    PerfilLectorCompletoDTO obtenerPerfilLector(Long usuarioId);
}
