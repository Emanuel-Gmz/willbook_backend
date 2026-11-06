package edu.willbook_backend.service;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import java.util.List;
import java.util.Map;

public interface CategoriaService {
    List<CategoriaResponseDTO> listarCategorias();
    Map<String, Object> crearCategoria(CategoriaCreateDTO request);
}