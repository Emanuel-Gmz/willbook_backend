package edu.willbook_backend.repository;

import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByEstado(EstadoPrestamo estado);
}