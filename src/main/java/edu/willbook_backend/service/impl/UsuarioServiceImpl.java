package edu.willbook_backend.service.impl;

import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.security.dto.CambiarPasswordRequest;
import edu.willbook_backend.service.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Utilizamos @Lazy en el PasswordEncoder para evitar dependencias circulares con SecurityConfig
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
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
}