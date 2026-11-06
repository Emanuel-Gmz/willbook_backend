package edu.willbook_backend.repository;

import edu.willbook_backend.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    // Valida unicidad combinada de nombre y apellido ignorando mayúsculas/minúsculas
    boolean existsByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);

    // Listado ordenado alfabéticamente por apellido y nombre para desplegables
    List<Autor> findAllByOrderByApellidoAscNombreAsc();
}