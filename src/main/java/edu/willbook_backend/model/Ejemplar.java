package edu.willbook_backend.model;

import edu.willbook_backend.enums.EstadoEjemplar;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ejemplares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ejemplar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 50)
    private String codigoInventario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private EstadoEjemplar estado = EstadoEjemplar.DISPONIBLE;
}