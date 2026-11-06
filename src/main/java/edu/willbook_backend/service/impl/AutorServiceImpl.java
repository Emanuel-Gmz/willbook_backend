package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.exception.AutorDuplicadoException;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.service.AutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AutorResponseDTO> listarAutores() {
        return autorRepository.findAllByOrderByApellidoAscNombreAsc().stream()
                .map(a -> new AutorResponseDTO(a.getId(), a.getNombre(), a.getApellido(), a.getBiografia(), a.getPaisOrigen()))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, Object> crearAutor(AutorCreateDTO request) {
        String nombreTrim = request.nombre().trim();
        String apellidoTrim = request.apellido().trim();

        // Validar si ya existe el autor exacto (Escenario 2)
        if (autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase(nombreTrim, apellidoTrim)) {
            throw new AutorDuplicadoException("Este autor ya se encuentra registrado en el sistema");
        }

        Autor autor = Autor.builder()
                .nombre(nombreTrim)
                .apellido(apellidoTrim)
                .biografia(request.biografia())
                .paisOrigen(request.paisOrigen())
                .build();

        Autor guardado = autorRepository.save(autor);

        return Map.of(
                "message", "Autor guardado exitosamente",
                "autor", new AutorResponseDTO(
                        guardado.getId(),
                        guardado.getNombre(),
                        guardado.getApellido(),
                        guardado.getBiografia(),
                        guardado.getPaisOrigen()
                )
        );
    }
}