package edu.willbook_backend.security.auth;

import edu.willbook_backend.dto.RegisterRequestDTO;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.exception.DniYaRegistradoException;
import edu.willbook_backend.exception.EmailYaRegistradoException;
import edu.willbook_backend.exception.PasswordInvalidaException;
import edu.willbook_backend.model.Localidad;
import edu.willbook_backend.model.PasswordResetToken;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.PasswordResetTokenRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.security.dto.AuthRequest;
import edu.willbook_backend.security.dto.AuthResponse;
import edu.willbook_backend.security.dto.OlvidoPasswordRequest;
import edu.willbook_backend.security.jwt.JwtService;
import edu.willbook_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("id", usuario.getId());
        claims.put("nombre", usuario.getNombre() + " " + usuario.getApellido());

        String token = jwtService.generateToken(claims, usuario);

        // Retornamos el token, mensaje, rol y nombre para la redirección en el cliente
        return new AuthResponse(
                token,
                "Inicio de sesión exitoso",
                usuario.getRol(),
                usuario.getNombre() + " " + usuario.getApellido()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequestDTO request) {
        // Lanzamos las excepciones personalizadas
        if (!request.password().equals(request.confirmacionPassword())) {
            throw new PasswordInvalidaException("Las contraseñas no coinciden");
        }

        if (usuarioRepository.existsByDni(request.dni())) {
            throw new DniYaRegistradoException("Este DNI ya se encuentra registrado en WillBook");
        }

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailYaRegistradoException("El correo electrónico ya se encuentra registrado");
        }

        Localidad localidad = localidadRepository.findById(request.localidadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada"));

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .dni(request.dni())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.LECTOR)
                .localidad(localidad)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("id", usuario.getId());
        claims.put("nombre", usuario.getNombre() + " " + usuario.getApellido());

        String token = jwtService.generateToken(claims, usuario);

        return new AuthResponse(
                token,
                "Registro exitoso. Ya puedes iniciar sesión",
                usuario.getRol(),
                usuario.getNombre() + " " + usuario.getApellido()
        );
    }

    @Transactional
    public void requestPasswordReset(OlvidoPasswordRequest request) {
        Usuario user = usuarioRepository.findByEmail(request.email()).orElse(null);

        // Por seguridad, si el usuario no existe, no lanzamos excepción para no revelar qué correos están registrados
        if (user == null) {
            return;
        }

        // Evitar spam de correos verificando si se solicitó uno recientemente (ej. menos de 2 minutos)
        Optional<PasswordResetToken> tokenPrevio = resetTokenRepository.findByUsuario(user);

        if (tokenPrevio.isPresent()) {
            LocalDateTime fechaCreacionEstimada = tokenPrevio.get().getExpiryDate().minusMinutes(15);
            if (fechaCreacionEstimada.plusMinutes(2).isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Ya enviamos un enlace a tu correo. Por favor, espera al menos 2 minutos antes de solicitar otro."
                );
            }
        }

        // Limpiar tokens anteriores de este usuario
        resetTokenRepository.deleteByUsuario(user);
        resetTokenRepository.flush();

        // Generar un token aleatorio con vigencia de 15 minutos
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        resetTokenRepository.save(resetToken);

        // Enviar correo de forma asíncrona
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "No se pudo enviar el correo en este momento. Inténtalo más tarde."
            );
        }
    }
}