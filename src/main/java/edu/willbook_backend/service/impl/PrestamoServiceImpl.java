package edu.willbook_backend.service.impl;

import edu.willbook_backend.dto.PrestamoDetalleDTO;
import edu.willbook_backend.dto.PrestamoPersonalDTO;
import edu.willbook_backend.dto.PrestamoRequestDTO;
import edu.willbook_backend.dto.PrestamoResponseDTO;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.exception.LibroSinStockException;
import edu.willbook_backend.exception.UsuarioConMultasPendientesException;
import edu.willbook_backend.exception.UsuarioNoEncontradoException;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.EjemplarRepository;
import edu.willbook_backend.repository.LibroRepository;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.PrestamoRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.PrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final MultaRepository multaRepository;

    @Override
    @Transactional
    public PrestamoResponseDTO solicitarRetiro(PrestamoRequestDTO request, String emailUsuario) {
        // 1. Obtener usuario autenticado
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(emailUsuario));

        // 2. Validar que el usuario no tenga multas pendientes (Escenario 3)
        if (multaRepository.existsByUsuarioIdAndEstado(usuario.getId(), EstadoMulta.PENDIENTE)) {
            throw new UsuarioConMultasPendientesException(
                    "No puedes solicitar nuevos libros porque tienes multas pendientes de regularizar"
            );
        }

        // 3. Verificar que el libro exista en el catálogo
        Libro libro = libroRepository.findById(request.libroId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Libro no encontrado"));

        // 4. Buscar un ejemplar disponible (Escenario 1 y 2)
        Ejemplar ejemplar = ejemplarRepository.findFirstByLibroIdAndEstado(libro.getId(), EstadoEjemplar.DISPONIBLE)
                .orElseThrow(() -> new LibroSinStockException("No hay ejemplares disponibles para este libro"));

        // 5. Descontar stock temporalmente cambiando el estado del ejemplar a RESERVADO
        ejemplar.setEstado(EstadoEjemplar.RESERVADO);
        ejemplarRepository.save(ejemplar);

        // 6. Registrar la solicitud de préstamo
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuario)
                .ejemplar(ejemplar)
                .estado(EstadoPrestamo.PENDIENTE_RETIRO)
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(7))
                .build();

        Prestamo guardado = prestamoRepository.save(prestamo);

        // 7. Retornar DTO con el mensaje exacto exigido (Escenario 1)
        return new PrestamoResponseDTO(
                guardado.getId(),
                libro.getId(),
                libro.getTitulo(),
                ejemplar.getCodigoInventario(),
                guardado.getFechaSolicitud(),
                guardado.getEstado(),
                "Solicitud enviada. Tienes 48hs para retirar tu libro"
        );
    }
    @Override
    @Transactional(readOnly = true)
    public List<PrestamoDetalleDTO> listarSolicitudesPendientes() {
        return prestamoRepository.findByEstado(EstadoPrestamo.PENDIENTE_RETIRO).stream()
                .map(p -> new PrestamoDetalleDTO(
                        p.getId(),
                        p.getUsuario().getId(),
                        p.getUsuario().getNombre() + " " + p.getUsuario().getApellido(),
                        p.getUsuario().getEmail(),
                        p.getEjemplar().getLibro().getId(),
                        p.getEjemplar().getLibro().getTitulo(),
                        p.getEjemplar().getCodigoInventario(),
                        p.getFechaSolicitud(),
                        p.getEstado()
                ))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, String> confirmarPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud de préstamo no encontrada"));

        if (prestamo.getEstado() != EstadoPrestamo.PENDIENTE_RETIRO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta solicitud no se encuentra pendiente de retiro");
        }

        // 1. Actualizar estado del préstamo, fijar fecha de retiro y cálculo límite (14 días) (Escenario 1)
        prestamo.setEstado(EstadoPrestamo.PRESTADO);
        prestamo.setFechaRetiro(LocalDateTime.now());
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().plusDays(14));
        prestamoRepository.save(prestamo);

        // 2. Cambiar estado del ejemplar de RESERVADO a PRESTADO
        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);

        return Map.of("message", "Préstamo registrado exitosamente");
    }

    @Override
    @Transactional
    public Map<String, String> cancelarReserva(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud de préstamo no encontrada"));

        if (prestamo.getEstado() != EstadoPrestamo.PENDIENTE_RETIRO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden cancelar solicitudes pendientes de retiro");
        }

        // 1. Cambiar estado del préstamo a cancelado (Escenario 2)
        prestamo.setEstado(EstadoPrestamo.CANCELADO);
        prestamoRepository.save(prestamo);

        // 2. Liberar el ejemplar devolviéndolo a estado DISPONIBLE
        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplarRepository.save(ejemplar);

        return Map.of("message", "Reserva cancelada y ejemplar liberado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrestamoPersonalDTO> consultarMisPrestamos(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        List<Prestamo> prestamos = prestamoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(usuario.getId());

        LocalDateTime ahora = LocalDateTime.now();

        return prestamos.stream().map(p -> {
            String titulo = p.getEjemplar().getLibro().getTitulo();
            String portada = p.getEjemplar().getLibro().getPortadaUrl();
            String codigo = p.getEjemplar().getCodigoInventario();

            String estadoVisual;
            boolean esAtrasado = false;

            // Lógica de cálculo de estado visual según Criterios de Aceptación
            if (p.getEstado() == EstadoPrestamo.PENDIENTE_RETIRO) {
                estadoVisual = "Pendiente de Retiro";
            } else if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
                estadoVisual = "Devuelto";
            } else if (p.getEstado() == EstadoPrestamo.PRESTADO || p.getEstado() == EstadoPrestamo.RETIRADO) {
                // Verificar si superó la fecha límite esperada
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
                    titulo,
                    portada,
                    codigo,
                    p.getFechaSolicitud(),
                    p.getFechaRetiro(),
                    p.getFechaDevolucionEsperada(),
                    p.getFechaDevolucionReal(),
                    p.getEstado(),
                    estadoVisual,
                    esAtrasado
            );
        }).toList();
    }

    @Override
    @Transactional
    public Map<String, Object> reportarLossOrDamage(Long prestamoId, String emailUsuario) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Préstamo no encontrado"));

        // Validar que el préstamo pertenezca al usuario autenticado (o sea ejecutado por un bibliotecario)
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() == edu.willbook_backend.enums.Rol.LECTOR && !prestamo.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos sobre este préstamo");
        }

        if (prestamo.getEstado() != EstadoPrestamo.PRESTADO && prestamo.getEstado() != EstadoPrestamo.RETIRADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden reportar como perdidos préstamos activos en curso");
        }

        // 1. Cambiar estado del préstamo a EXTRAVIADO (frena multas por atraso)
        prestamo.setEstado(EstadoPrestamo.EXTRAVIADO);
        prestamoRepository.save(prestamo);

        // 2. Dar de baja automática el ejemplar físico del stock
        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setEstado(EstadoEjemplar.EXTRAVIADO);
        ejemplarRepository.save(ejemplar);

        // 3. Generar sanción administrativa / Multa de reposición requerida que bloquea nuevos retiros
        Multa sancionReposicion = Multa.builder()
                .usuario(prestamo.getUsuario())
                .prestamo(prestamo)
                .monto(java.math.BigDecimal.ZERO) // O el costo del libro si está definido
                .estado(EstadoMulta.PENDIENTE)
                .build();
        multaRepository.save(sancionReposicion);

        return Map.of(
                "message", "Reporte registrado con éxito. El estado cambió a Extraviado y se generó una sanción de reposición pendiente. Debes comunicarte con administración.",
                "prestamoId", prestamo.getId()
        );
    }
}