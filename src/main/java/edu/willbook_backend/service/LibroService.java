package edu.willbook_backend.service;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import java.util.List;
import java.util.Map;

public interface LibroService {
    List<LibroResponseDTO> buscarLibros(String query);
    List<LibroResponseDTO> listarCatalogo();
    Map<String, Object> crearLibro(LibroCreateDTO request);
}