package edu.willbook_backend.service;

import edu.willbook_backend.dto.PrestamoDetalleDTO;
import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.dto.PrestamoResponseDTO;

import java.util.List;
import java.util.Map;

public interface PrestamoService {
    PrestamoResponseDTO solicitarRetiro(PrestamoRequestDTO request, String emailUsuario);
    List<PrestamoDetalleDTO> listarSolicitudesPendientes();
    Map<String, String> confirmarPrestamo(Long prestamoId);
    Map<String, String> cancelarReserva(Long prestamoId);
}