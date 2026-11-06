package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.MultaCancelacionDTO;
import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.exception.MultaNoActivaException;
import edu.willbook_backend.exception.MultaYaProcesadaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Multa;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.MultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MultaServiceImpl implements MultaService {

    private final MultaRepository multaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public Map<String, Object> crearMultaManual(MultaCreateDTO request) {
        // 1. Buscar al usuario por ID (que previamente fue seleccionado mediante DNI o Nombre en el frontend)
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(request.usuarioId()));

        // 2. Registrar la multa con estado PENDIENTE / ACTIVA (Bloquea automáticamente nuevos préstamos)
        Multa multa = Multa.builder()
                .usuario(usuario)
                .motivo(request.motivo().trim())
                .monto(request.monto())
                .estado(EstadoMulta.PENDIENTE) // Estado activo que bloquea nuevos retiros
                .build();

        Multa guardada = multaRepository.save(multa);

        return Map.of(
                "message", "Multa cargada exitosamente",
                "multaId", guardada.getId(),
                "usuario", usuario.getNombre() + " " + usuario.getApellido()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> cancelarMulta(Long multaId, MultaCancelacionDTO request) {
        Multa multa = multaRepository.findById(multaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Multa no encontrada"));

        // Regla: No es posible anular una multa que ya ha sido procesada o pagada (Escenario 2)
        if (multa.getEstado() == EstadoMulta.PAGADA || multa.getEstado() == EstadoMulta.CANCELADA) {
            throw new MultaYaProcesadaException("No es posible anular una multa que ya ha sido procesada o pagada");
        }

        // 1. Cambiar estado de la multa a CANCELADA (no se contabiliza como deuda)
        multa.setEstado(EstadoMulta.CANCELADA);
        multaRepository.save(multa);

        // 2. Verificar si al usuario le quedan otras multas pendientes/activas
        boolean tieneOtrasMultas = multaRepository.existsByUsuarioIdAndEstado(
                multa.getUsuario().getId(),
                EstadoMulta.PENDIENTE
        );

        String mensajeRehabilitacion = "";
        // Si no tiene otras multas pendientes, el sistema rehabilita su cuenta para nuevos préstamos
        if (!tieneOtrasMultas) {
            mensajeRehabilitacion = " La cuenta del usuario ha sido rehabilitada para nuevos préstamos.";
        }

        return Map.of(
                "message", "La multa ha sido anulada correctamente." + mensajeRehabilitacion,
                "multaId", multa.getId(),
                "nuevoEstado", multa.getEstado().name()
        );
    }

    @Override
    @Transactional
    public Map<String, Object> registrarPagoMulta(Long multaId, MultaPagoDTO request) {
        Multa multa = multaRepository.findById(multaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Multa no encontrada"));

        // Validar que la multa esté activa (PENDIENTE)
        if (multa.getEstado() != EstadoMulta.PENDIENTE) {
            throw new MultaNoActivaException("Solo se pueden registrar pagos de multas que se encuentran activas/pendientes");
        }

        // 1. Actualizar estado a PAGADA
        multa.setEstado(EstadoMulta.PAGADA);
        multa.setMetodoPago(request.metodoPago());
        multa.setFechaPago(LocalDateTime.now());
        multaRepository.save(multa);

        // 2. Verificar si al usuario le quedan otras multas pendientes (Escenario 2)
        boolean tieneOtrasMultas = multaRepository.existsByUsuarioIdAndEstado(
                multa.getUsuario().getId(),
                EstadoMulta.PENDIENTE
        );

        String mensajeRespuesta;
        if (!tieneOtrasMultas) {
            // Si era su única multa, se desbloquea automáticamente (Escenario 1)
            mensajeRespuesta = "Pago registrado exitosamente. El usuario ya puede solicitar préstamos";
        } else {
            // Si le quedan multas, se mantiene bloqueado (Escenario 2)
            mensajeRespuesta = "Pago registrado exitosamente. El usuario aún mantiene multas pendientes de pago";
        }

        return Map.of(
                "message", mensajeRespuesta,
                "multaId", multa.getId(),
                "estado", multa.getEstado().name(),
                "usuarioBloqueado", tieneOtrasMultas
        );
    }
}