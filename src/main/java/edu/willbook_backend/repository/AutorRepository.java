package edu.willbook_backend.repository;

import edu.willbook_backend.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {

    // Lista ordenada alfabéticamente por apellido y luego por nombre
    List<Autor> findAllByOrderByApellidoAscNombreAsc();

    // Búsqueda rápida por filtro de texto en nombre o apellido (insensible a mayúsculas/minúsculas)
    @Query("SELECT a FROM Autor a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(a.apellido) LIKE LOWER(CONCAT('%', :filtro, '%')) ORDER BY a.apellido ASC, a.nombre ASC")
    List<Autor> buscarPorNombreOApellido(@Param("filtro") String filtro);

    boolean existsByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);
}