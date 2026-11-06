package edu.willbook_backend.service;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import java.util.List;
import java.util.Map;

public interface AutorService {
    List<AutorResponseDTO> listarAutores();
    Map<String, Object> crearAutor(AutorCreateDTO request);
}