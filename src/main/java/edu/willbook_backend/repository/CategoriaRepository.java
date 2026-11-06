package edu.willbook_backend.repository;

import edu.willbook_backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Valida unicidad ignorando mayúsculas/minúsculas
    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Listado ordenado alfabéticamente para desplegables
    List<Categoria> findAllByOrderByNombreAsc();
}