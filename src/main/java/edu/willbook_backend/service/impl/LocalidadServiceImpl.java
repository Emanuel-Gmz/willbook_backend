package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.LocalidadResponseDTO;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.service.LocalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalidadServiceImpl implements LocalidadService {

    private final LocalidadRepository localidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> listarLocalidades() {
        return localidadRepository.findAllByOrderByNombreAsc().stream()
                .map(l -> new LocalidadResponseDTO(
                        l.getId(),
                        l.getNombre(),
                        l.getProvincia(),
                        l.getDireccionSucursal()
                ))
                .toList();
    }
}