package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.AutorCreateDTO;
import edu.willbook_backend.dto.AutorResponseDTO;
import edu.willbook_backend.dto.AutorUpdateDTO;
import edu.willbook_backend.exception.AutorConLibrosException;
import edu.willbook_backend.exception.AutorDuplicadoException;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.service.AutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutorServiceImpl implements AutorService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AutorResponseDTO> listarOFiltrarAutores(String query) {
        List<Autor> autores;
        if (query == null || query.isBlank()) {
            autores = autorRepository.findAllByOrderByApellidoAscNombreAsc();
        } else {
            autores = autorRepository.buscarPorNombreOApellido(query.trim());
        }

        return autores.stream()
                .map(a -> new AutorResponseDTO(
                        a.getId(),
                        a.getNombre(),
                        a.getApellido(),
                        a.getBiografia(),
                        a.getNacionalidad()
                ))
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
                .nacionalidad(request.paisOrigen())
                .build();

        Autor guardado = autorRepository.save(autor);

        return Map.of(
                "message", "Autor guardado exitosamente",
                "autor", new AutorResponseDTO(
                        guardado.getId(),
                        guardado.getNombre(),
                        guardado.getApellido(),
                        guardado.getBiografia(),
                        guardado.getNacionalidad()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AutorResponseDTO obtenerAutorPorId(Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor no encontrado"));
        return new AutorResponseDTO(autor.getId(), autor.getNombre(), autor.getApellido(), autor.getBiografia(), autor.getNacionalidad());
    }

    @Override
    @Transactional
    public Map<String, Object> actualizarAutor(Long id, AutorUpdateDTO request) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor no encontrado"));

        // Validar si cambia nombre/apellido que no colisione con otro existente
        String nuevoNombre = request.nombre() != null ? request.nombre().trim() : autor.getNombre();
        String nuevoApellido = request.apellido() != null ? request.apellido().trim() : autor.getApellido();

        if ((request.nombre() != null || request.apellido() != null) &&
                (!nuevoNombre.equalsIgnoreCase(autor.getNombre()) || !nuevoApellido.equalsIgnoreCase(autor.getApellido()))) {
            if (autorRepository.existsByNombreIgnoreCaseAndApellidoIgnoreCase(nuevoNombre, nuevoApellido)) {
                throw new AutorDuplicadoException("Ya existe otro autor registrado con ese nombre y apellido");
            }
        }

        autor.setNombre(nuevoNombre);
        autor.setApellido(nuevoApellido);
        if (request.biografia() != null) {
            autor.setBiografia(request.biografia());
        }
        if (request.paisOrigen() != null) {
            autor.setNacionalidad(request.paisOrigen());
        }

        Autor actualizado = autorRepository.save(autor);

        return Map.of(
                "message", "Autor actualizado correctamente",
                "autor", new AutorResponseDTO(
                        actualizado.getId(),
                        actualizado.getNombre(),
                        actualizado.getApellido(),
                        actualizado.getBiografia(),
                        actualizado.getNacionalidad()
                )
        );
    }

    @Override
    @Transactional
    public Map<String, String> eliminarAutor(Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor no encontrado"));

        // Regla de integridad: Validar si tiene libros asociados activos (Escenario 2)
        if (libroRepository.existsByAutorId(autor.getId())) {
            throw new AutorConLibrosException(
                    "No se puede eliminar el autor porque tiene libros asociados en el catálogo. Debe reasignarlos o borrarlos primero"
            );
        }

        // Si no tiene libros, se elimina definitivamente (Escenario 1)
        autorRepository.delete(autor);

        return Map.of("message", "Autor eliminado correctamente");
    }
}