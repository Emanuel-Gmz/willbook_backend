package edu.willbook_backend.repository;

import edu.willbook_backend.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    // Lista todas las notificaciones del usuario ordenadas de la más reciente a la más antigua
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    // Contador numérico de notificaciones no leídas para el ícono de la campana
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    // Buscar todas las no leídas de un usuario para la acción masiva
    List<Notificacion> findByUsuarioIdAndLeidaFalse(Long usuarioId);
}