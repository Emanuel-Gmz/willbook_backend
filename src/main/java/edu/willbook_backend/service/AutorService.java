package edu.willbook_backend.service;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.dto.AutorUpdateDTO;

import java.util.List;
import java.util.Map;

public interface AutorService {
    List<AutorResponseDTO> listarAutores();
    AutorResponseDTO obtenerAutorPorId(Long id);
    Map<String, Object> crearAutor(AutorCreateDTO request);
    Map<String, Object> actualizarAutor(Long id, AutorUpdateDTO request);
    Map<String, String> eliminarAutor(Long id);
}