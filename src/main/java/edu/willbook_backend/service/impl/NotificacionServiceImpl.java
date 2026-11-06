package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.NotificacionResponseDTO;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Notificacion;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.NotificacionRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerBandejaNotificaciones(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailUsuario));

        // 1. Obtener listado ordenado de la más reciente a la más antigua
        List<NotificacionResponseDTO> notificaciones = notificacionRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(n -> new NotificacionResponseDTO(
                        n.getId(),
                        n.getMensaje(),
                        n.getLeida(),
                        n.getFechaCreacion()
                ))
                .toList();

        // 2. Obtener el contador numérico de no leídas para el globo de la campana
        long noLeidasCount = notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId());

        return Map.of(
                "contadorNoLeidas", noLeidasCount,
                "notificaciones", notificaciones
        );
    }

    @Override
    @Transactional
    public Map<String, String> marcarComoLeida(Long notificacionId, String emailUsuario) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailUsuario));

        // Validar que la notificación pertenezca al usuario autenticado
        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos sobre esta notificación");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);

        return Map.of("message", "Notificación marcada como leida");
    }

    @Override
    @Transactional
    public Map<String, Object> marcarTodasComoLeidas(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailUsuario));

        // Obtener todas las notificaciones pendientes del usuario
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioIdAndLeidaFalse(usuario.getId());

        for (Notificacion n : noLeidas) {
            n.setLeida(true);
            notificacionRepository.save(n);
        }

        return Map.of(
                "message", "Todas las notificaciones han sido marcadas como leídas",
                "contadorNoLeidas", 0
        );
    }
}