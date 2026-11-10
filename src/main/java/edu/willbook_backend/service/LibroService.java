package edu.willbook_backend.service;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.dto.LibroUpdateDTO;

import java.util.List;
import java.util.Map;

public interface LibroService {
    List<LibroResponseDTO> buscarLibros(String query);
    List<LibroResponseDTO> listarCatalogo();
    LibroResponseDTO obtenerLibroPorId(Long id); // Para la precarga de datos
    Map<String, Object> crearLibro(LibroCreateDTO request);
    Map<String, Object> actualizarLibro(Long id, LibroUpdateDTO request);
    Map<String, Object> agregarEjemplar(Long libroId, String codigoInventario);
    Map<String, String> eliminarLibro(Long id);
}