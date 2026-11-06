package edu.willbook_backend.repository;

import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByEstado(EstadoPrestamo estado);

    // Buscar todos los préstamos de un usuario ordenados por fecha de solicitud descendente
    List<Prestamo> findByUsuarioIdOrderByFechaSolicitudDesc(Long usuarioId);
}