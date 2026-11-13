package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.exception.CategoriaDuplicadaException;
import edu.willbook_backend.exception.CategoriaNoEncontradaException;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaResponseDTO(c.getId(), c.getNombre(), c.getDescripcion()))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, Object> crearCategoria(CategoriaCreateDTO request) {
        // Validar que no exista previamente (ignorando mayúsculas/minúsculas - Escenario 2)
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new CategoriaDuplicadaException("Esta categoría ya existe en el sistema");
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion())
                .build();

        Categoria guardada = categoriaRepository.save(categoria);

        return Map.of(
                "message", "Categoría creada",
                "categoria", new CategoriaResponseDTO(guardada.getId(), guardada.getNombre(), guardada.getDescripcion())
        );
    }

    @Override
    @Transactional
    public Map<String, Object> actualizarCategoria(Long id, CategoriaCreateDTO request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));

        String nuevoNombre = request.nombre().trim();

        // Validar si el nombre cambia y ya existe en otra categoría
        if (!categoria.getNombre().equalsIgnoreCase(nuevoNombre)) {
            if (categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new CategoriaDuplicadaException("Ya existe otra categoría con ese nombre");
            }
        }

        categoria.setNombre(nuevoNombre);
        categoria.setDescripcion(request.descripcion());

        Categoria actualizada = categoriaRepository.save(categoria);

        return Map.of(
                "message", "Categoría actualizada correctamente",
                "categoria", new CategoriaResponseDTO(actualizada.getId(), actualizada.getNombre(), actualizada.getDescripcion())
        );
    }

    @Override
    @Transactional
    public Map<String, String> eliminarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));

        // Regla de integridad: No permitir eliminar si tiene libros asociados
        boolean tieneLibros = libroRepository.findAll().stream()
                .anyMatch(l -> l.getCategoria() != null && l.getCategoria().getId().equals(id));

        if (tieneLibros) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar la categoría porque tiene libros asociados en el catálogo.");
        }

        categoriaRepository.delete(categoria);
        return Map.of("message", "Categoría eliminada correctamente");
    }
}
