package edu.willbook_backend.repository;

import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.model.Multa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {
    // Valida si el usuario tiene multas pendientes (Escenario 3)
    boolean existsByUsuarioIdAndEstado(Long usuarioId, EstadoMulta estado);

    // Buscar todas las multas de un usuario ordenadas por fecha de generación descendente
    List<Multa> findByUsuarioIdOrderByFechaGeneracionDesc(Long usuarioId);
}