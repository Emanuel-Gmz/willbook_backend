package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.*;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.PrestamoRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, LocalidadRepository localidadRepository, @Lazy PasswordEncoder passwordEncoder, PrestamoRepository prestamoRepository, MultaRepository multaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.localidadRepository = localidadRepository;
        this.passwordEncoder = passwordEncoder;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
    }

    @Override
    @Transactional
    public void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser) {
        String email = connectedUser.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 1. Validar que la contraseña actual ingresada coincida con la de la base de datos
        if (!passwordEncoder.matches(request.currentPassword(), usuario.getPassword())) {
            throw new PasswordInvalidaException("La contraseña actual es incorrecta");
        }

        // 2. Validar que la nueva y la confirmación coincidan
        if (!request.newPassword().equals(request.confirmationPassword())) {
            throw new PasswordInvalidaException("Las nuevas contraseñas no coinciden");
        }

        // 3. Validar que no ponga la misma contraseña que ya tenía
        if (passwordEncoder.matches(request.newPassword(), usuario.getPassword())) {
            throw new PasswordInvalidaException("La nueva contraseña no puede ser igual a la anterior");
        }

        // 4. Actualizar la contraseña en la base de datos
        usuario.setPassword(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public PerfilResponseDTO actualizarPerfil(String emailActual, PerfilUpdateDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(emailActual)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Validar si el nuevo correo ya está en uso por otro usuario
        if (request.email() != null && !request.email().equalsIgnoreCase(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya se encuentra registrado por otra cuenta");
            }
            usuario.setEmail(request.email());
        }

        // Actualizar campos independientes
        if (request.nombre() != null && !request.nombre().isBlank()) {
            usuario.setNombre(request.nombre());
        }
        if (request.apellido() != null && !request.apellido().isBlank()) {
            usuario.setApellido(request.apellido());
        }
        if (request.telefono() != null) {
            usuario.setTelefono(request.telefono());
        }
        if (request.direccion() != null) {
            usuario.setDireccion(request.direccion()); // o setDireccion segun tu atributo
        }

        if (request.localidadId() != null) {
            var localidad = localidadRepository.findById(request.localidadId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada"));
            usuario.setLocalidad(localidad);
        }

        usuarioRepository.save(usuario);

        String localidadStr = usuario.getLocalidad() != null ? usuario.getLocalidad().getNombre() : null;

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRol(),
                localidadStr
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioBusquedaResponseDTO> buscarLectores(String query) {
        List<Usuario> usuarios;
        if (query == null || query.isBlank()) {
            usuarios = usuarioRepository.findAll();
        } else {
            usuarios = usuarioRepository.buscarPorDniNombreOApellido(query.trim());
        }

        return usuarios.stream()
                .map(u -> new UsuarioBusquedaResponseDTO(
                        u.getId(),
                        u.getNombre(),
                        u.getApellido(),
                        u.getDni(),
                        u.getEmail(),
                        u.getActivo()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilLectorCompletoDTO obtenerPerfilLector(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        // 1. Obtener todos los préstamos del usuario
        List<PrestamoPersonalDTO> todosLosPrestamos = prestamoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(usuario.getId())
                .stream().map(p -> {
                    String estadoVisual;
                    boolean esAtrasado = false;
                    LocalDateTime ahora = LocalDateTime.now();

                    if (p.getEstado() == EstadoPrestamo.PENDIENTE_RETIRO) {
                        estadoVisual = "Pendiente de Retiro";
                    } else if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
                        estadoVisual = "Devuelto";
                    } else if (p.getEstado() == EstadoPrestamo.PRESTADO || p.getEstado() == EstadoPrestamo.RETIRADO) {
                        if (p.getFechaDevolucionEsperada() != null && ahora.isAfter(p.getFechaDevolucionEsperada())) {
                            estadoVisual = "Vencido/Atrasado";
                            esAtrasado = true;
                        } else {
                            estadoVisual = "Vigente";
                        }
                    } else {
                        estadoVisual = p.getEstado().name();
                    }

                    return new PrestamoPersonalDTO(
                            p.getId(),
                            p.getEjemplar().getLibro().getId(),
                            p.getEjemplar().getLibro().getTitulo(),
                            p.getEjemplar().getLibro().getPortadaUrl(),
                            p.getEjemplar().getCodigoInventario(),
                            p.getFechaSolicitud(),
                            p.getFechaRetiro(),
                            p.getFechaDevolucionEsperada(),
                            p.getFechaDevolucionReal(),
                            p.getEstado(),
                            estadoVisual,
                            esAtrasado
                    );
                }).toList();

        // 2. Segmentar entre Activos (en curso) e Historial (devueltos/cancelados)
        List<PrestamoPersonalDTO> activos = todosLosPrestamos.stream()
                .filter(p -> p.estadoInterno() == EstadoPrestamo.PRESTADO || p.estadoInterno() == EstadoPrestamo.RETIRADO || p.estadoInterno() == EstadoPrestamo.PENDIENTE_RETIRO)
                .toList();

        List<PrestamoPersonalDTO> historial = todosLosPrestamos.stream()
                .filter(p -> p.estadoInterno() == EstadoPrestamo.DEVUELTO || p.estadoInterno() == EstadoPrestamo.CANCELADO || p.estadoInterno() == EstadoPrestamo.EXTRAVIADO)
                .toList();

        // 3. Obtener multas del usuario
        List<MultaDetalleDTO> multas = multaRepository.findAll().stream()
                .filter(m -> m.getUsuario().getId().equals(usuario.getId()))
                .map(m -> new MultaDetalleDTO(
                        m.getId(),
                        m.getMotivo(),
                        m.getMonto(),
                        m.getEstado(),
                        m.getFechaGeneracion()
                ))
                .toList();

        // Determinar si la cuenta está bloqueada por multas pendientes
        boolean tieneMultasPendientes = multas.stream().anyMatch(m -> m.estado() == EstadoMulta.PENDIENTE);
        boolean cuentaActiva = (usuario.getActivo() != null && usuario.getActivo()) && !tieneMultasPendientes;

        String nombreLocalidad = usuario.getLocalidad() != null ? usuario.getLocalidad().getNombre() : "Sin localidad";

        return new PerfilLectorCompletoDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                nombreLocalidad,
                cuentaActiva,
                activos,
                historial,
                multas
        );
    }
}