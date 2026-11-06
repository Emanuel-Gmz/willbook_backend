package edu.willbook_backend.service.impl;

import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.exception.EjemplarPrestadoException;
import edu.willbook_backend.model.Ejemplar;
import edu.willbook_backend.repository.EjemplarRepository;
import edu.willbook_backend.service.EjemplarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EjemplarServiceImpl implements EjemplarService {

    private final EjemplarRepository ejemplarRepository;

    @Override
    @Transactional
    public Map<String, String> darDeBajaEjemplar(String codigoInventario) {
        // 1. Buscar el ejemplar por su código único de inventario
        Ejemplar ejemplar = ejemplarRepository.findByCodigoInventario(codigoInventario.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ejemplar no encontrado con el código: " + codigoInventario));

        // 2. Regla de integridad: No permitir eliminar si se encuentra en estado PRESTADO (Escenario 2)
        if (ejemplar.getEstado() == EstadoEjemplar.PRESTADO) {
            throw new EjemplarPrestadoException(
                    "Error: No se puede dar de baja un ejemplar que se encuentra prestado. Registre la devolución primero"
            );
        }

        // 3. Eliminar el ejemplar de la base de datos (se descuenta automáticamente del stock total y disponible)
        ejemplarRepository.delete(ejemplar);

        return Map.of("message", "Ejemplar dado de baja correctamente");
    }
}