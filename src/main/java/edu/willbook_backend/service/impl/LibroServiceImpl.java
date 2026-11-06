package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.exception.AutorNoEncontradoException;
import edu.willbook_backend.exception.CategoriaNoEncontradaException;
import edu.willbook_backend.exception.IsbnDuplicadoException;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.repository.CategoriaRepository;
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
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponseDTO> buscarLibros(String query) {
        if (query == null || query.isBlank()) {
            return listarCatalogo();
        }
        List<Libro> libros = libroRepository.buscarPorTituloOAutor(query.trim());
        return libros.stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LibroResponseDTO> listarCatalogo() {
        return libroRepository.findAll().stream().map(this::mapearADTO).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> crearLibro(LibroCreateDTO request) {
        // Validación de ISBN duplicado
        if (libroRepository.existsByIsbn(request.isbn())) {
            throw new IsbnDuplicadoException("Error: Ya existe un libro registrado con este ISBN");
        }

        // Buscar Autor usando excepción personalizada en lugar de ResponseStatusException
        Autor autor = autorRepository.findById(request.autorId())
                .orElseThrow(() -> new AutorNoEncontradoException(request.autorId()));

        // Buscar Categoría usando excepción personalizada
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new CategoriaNoEncontradaException(request.categoriaId()));

        Libro libro = Libro.builder()
                .titulo(request.titulo())
                .isbn(request.isbn())
                .editorial(request.editorial())
                .anioPublicacion(request.anioPublicacion())
                .portadaUrl(request.portadaUrl())
                .descripcion(request.descripcion())
                .disponible(true)
                .autor(autor)         // Asignamos la entidad real mapeada
                .categoria(categoria) // Asignamos la entidad real mapeada
                .build();

        Libro guardado = libroRepository.save(libro);

        return Map.of(
                "message", "Libro cargado exitosamente",
                "libro", mapearADTO(guardado)
        );
    }

    private LibroResponseDTO mapearADTO(Libro l) {
        // Extraemos el nombre completo del autor y el nombre de la categoría relacionados
        String autorNombreCompleto = l.getAutor() != null ? l.getAutor().getNombre() + " " + l.getAutor().getApellido() : "Sin autor";
        String categoriaNombre = l.getCategoria() != null ? l.getCategoria().getNombre() : "Sin categoría";

        return new LibroResponseDTO(
                l.getId(),
                l.getTitulo(),
                autorNombreCompleto,
                l.getIsbn(),
                l.getEditorial(),
                l.getAnioPublicacion(),
                categoriaNombre,
                l.getPortadaUrl(),
                l.getDescripcion(),
                l.getDisponible()
        );
    }
}