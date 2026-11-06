package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {

    private final LibroRepository libroRepository;

    @Override
    public List<LibroResponseDTO> buscarLibros(String query) {
        if (query == null || query.isBlank()) {
            return listarCatalogo();
        }
        List<Libro> libros = libroRepository.buscarPorTituloOAutor(query.trim());
        return libros.stream()
                .map(l -> new LibroResponseDTO(
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getDescripcion(),
                        l.getDisponible()
                ))
                .toList();
    }

    @Override
    public List<LibroResponseDTO> listarCatalogo() {
        return libroRepository.findAll().stream()
                .map(l -> new LibroResponseDTO(
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getDescripcion(),
                        l.getDisponible()
                ))
                .toList();
    }
}