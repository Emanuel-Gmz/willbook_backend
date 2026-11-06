package edu.willbook_backend.service;

import edu.willbook_backend.dto.LocalidadResponseDTO;
import java.util.List;

public interface LocalidadService {
    List<LocalidadResponseDTO> listarLocalidades();
}