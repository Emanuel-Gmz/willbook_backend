package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.CategoriaCreateDTO;
import edu.willbook_backend.dto.CategoriaResponseDTO;
import edu.willbook_backend.exception.CategoriaDuplicadaException;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

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
}