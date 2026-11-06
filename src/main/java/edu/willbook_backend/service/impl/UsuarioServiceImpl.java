package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.PerfilResponseDTO;
import edu.willbook_backend.dto.PerfilUpdateDTO;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
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

@Service
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, LocalidadRepository localidadRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.localidadRepository = localidadRepository;
        this.passwordEncoder = passwordEncoder;
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
}