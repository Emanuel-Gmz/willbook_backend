package edu.willbook_backend.service;

import edu.willbook_backend.dto.LibroResponseDTO;
import java.util.List;

public interface LibroService {
    List<LibroResponseDTO> buscarLibros(String query);
    List<LibroResponseDTO> listarCatalogo();
}