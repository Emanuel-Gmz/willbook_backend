package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.LibroCreateDTO;
import edu.willbook_backend.dto.LibroResponseDTO;
import edu.willbook_backend.dto.LibroUpdateDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Override
    @Transactional(readOnly = true)
    public LibroResponseDTO obtenerLibroPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));
        return mapearADTO(libro);
    }

    @Override
    @Transactional
    public Map<String, Object> actualizarLibro(Long id, LibroUpdateDTO request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));

        // Validar si se modifica el ISBN que no pertenezca a otro libro (Escenario 2)
        if (request.isbn() != null && !request.isbn().equals(libro.getIsbn())) {
            if (libroRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
                throw new IsbnDuplicadoException("El ISBN ingresado ya corresponde a otro libro");
            }
            libro.setIsbn(request.isbn());
        }

        if (request.titulo() != null && !request.titulo().isBlank()) {
            libro.setTitulo(request.titulo());
        }
        if (request.editorial() != null) {
            libro.setEditorial(request.editorial());
        }
        if (request.anioPublicacion() != null) {
            libro.setAnioPublicacion(request.anioPublicacion());
        }
        if (request.portadaUrl() != null) {
            libro.setPortadaUrl(request.portadaUrl());
        }
        if (request.descripcion() != null) {
            libro.setDescripcion(request.descripcion());
        }
        if (request.disponible() != null) {
            libro.setDisponible(request.disponible());
        }

        // Actualizar autor si viene especificado
        if (request.autorId() != null) {
            Autor autor = autorRepository.findById(request.autorId())
                    .orElseThrow(() -> new AutorNoEncontradoException(request.autorId()));
            libro.setAutor(autor);
        }

        // Actualizar categoría si viene especificada
        if (request.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new CategoriaNoEncontradaException(request.categoriaId()));
            libro.setCategoria(categoria);
        }

        Libro actualizado = libroRepository.save(libro);

        return Map.of(
                "message", "Libro actualizado correctamente",
                "libro", mapearADTO(actualizado)
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