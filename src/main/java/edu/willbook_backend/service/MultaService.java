package edu.willbook_backend.service;

import edu.willbook_backend.dto.MisMultasResponseDTO;
import edu.willbook_backend.dto.MultaCancelacionDTO;
import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;

import java.util.Map;

public interface MultaService {
    Map<String, Object> crearMultaManual(MultaCreateDTO request);
    Map<String, Object> cancelarMulta(Long multaId, MultaCancelacionDTO request);
    Map<String, Object> registrarPagoMulta(Long multaId, MultaPagoDTO request);
    MisMultasResponseDTO consultarMisMultas(String emailUsuario);
}