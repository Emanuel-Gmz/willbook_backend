package edu.willbook_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "libros")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(length = 100)
    private String editorial;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(name = "portada_url")
    private String portadaUrl;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean disponible = true;
}