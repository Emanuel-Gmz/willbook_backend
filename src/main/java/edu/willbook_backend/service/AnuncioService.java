package edu.willbook_backend.service;

import edu.willbook_backend.dto.AnuncioCreateDTO;
import edu.willbook_backend.dto.AnuncioResponseDTO;
import java.util.List;
import java.util.Map;

public interface AnuncioService {
    List<AnuncioResponseDTO> listarAnunciosVigentes();
    Map<String, Object> crearAnuncio(AnuncioCreateDTO request, String emailUsuario);
}