package edu.willbook_backend.service;

import edu.willbook_backend.security.dto.CambiarPasswordRequest;

import java.security.Principal;

public interface UsuarioService {
    void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser);
}
