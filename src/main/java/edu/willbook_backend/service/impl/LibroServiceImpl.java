package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.exception.IsbnDuplicadoException;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        return libros.stream().map(this::mapearADTO).toList();
    }

    @Override
    public List<LibroResponseDTO> listarCatalogo() {
        return libroRepository.findAll().stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> crearLibro(LibroCreateDTO request) {
        // Validación de ISBN duplicado (Escenario 2)
        if (libroRepository.existsByIsbn(request.isbn())) {
            throw new IsbnDuplicadoException("Error: Ya existe un libro registrado con este ISBN");
        }

        Libro libro = Libro.builder()
                .titulo(request.titulo())
                .autor(request.autor())
                .isbn(request.isbn())
                .editorial(request.editorial())
                .anioPublicacion(request.anioPublicacion())
                .categoria(request.categoria())
                .portadaUrl(request.portadaUrl())
                .descripcion(request.descripcion())
                .disponible(true)
                .build();

        Libro guardado = libroRepository.save(libro);

        return Map.of(
                "message", "Libro cargado exitosamente",
                "libro", mapearADTO(guardado)
        );
    }

    private LibroResponseDTO mapearADTO(Libro l) {
        return new LibroResponseDTO(
                l.getId(),
                l.getTitulo(),
                l.getAutor(),
                l.getIsbn(),
                l.getEditorial(),
                l.getAnioPublicacion(),
                l.getCategoria(),
                l.getPortadaUrl(),
                l.getDescripcion(),
                l.getDisponible()
        );
    }
}