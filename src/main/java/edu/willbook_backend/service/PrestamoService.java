package edu.willbook_backend.service;

import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.dto.PrestamoResponseDTO;

public interface PrestamoService {
    PrestamoResponseDTO solicitarRetiro(PrestamoRequestDTO request, String emailUsuario);
}