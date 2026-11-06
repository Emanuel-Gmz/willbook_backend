package edu.willbook_backend.repository;

import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.model.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {
    Optional<Ejemplar> findFirstByLibroIdAndEstado(Long libroId, EstadoEjemplar estado);

    // Buscar todos los ejemplares de un libro
    List<Ejemplar> findByLibroId(Long libroId);
}