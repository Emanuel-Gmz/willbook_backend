package edu.willbook_backend.model;

import edu.willbook_backend.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ejemplar_id", nullable = false)
    private Ejemplar ejemplar;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_retiro")
    private LocalDateTime fechaRetiro;

    @Column(name = "fecha_devolucion_esperada")
    private LocalDateTime fechaDevolucionEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.PENDIENTE_RETIRO;
}