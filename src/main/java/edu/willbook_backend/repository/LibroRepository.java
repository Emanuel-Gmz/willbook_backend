package edu.willbook_backend.repository;

import edu.willbook_backend.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Query("SELECT l FROM Libro l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :filtro, '%')) OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Libro> buscarPorTituloOAutor(@Param("filtro") String filtro);

    boolean existsByIsbn(String isbn);
}