package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Multa;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.MultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}