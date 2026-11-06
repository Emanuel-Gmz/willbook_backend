package edu.willbook_backend.service;

import edu.willbook_backend.dto.NotificacionResponseDTO;
import java.util.List;
import java.util.Map;

public interface NotificacionService {
    Map<String, Object> obtenerBandejaNotificaciones(String emailUsuario);
    Map<String, String> marcarComoLeida(Long notificacionId, String emailUsuario);
    Map<String, Object> marcarTodasComoLeidas(String emailUsuario);
}