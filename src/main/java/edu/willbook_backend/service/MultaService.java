package edu.willbook_backend.service;

import edu.willbook_backend.dto.MultaCreateDTO;
import java.util.Map;

public interface MultaService {
    Map<String, Object> crearMultaManual(MultaCreateDTO request);
}