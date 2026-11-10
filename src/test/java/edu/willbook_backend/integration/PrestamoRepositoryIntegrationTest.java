package edu.willbook_backend.integration;

import edu.willbook_backend.BaseIntegrationTest;
import edu.willbook_backend.enums.EstadoEjemplar;
import edu.willbook_backend.enums.EstadoPrestamo;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.*;
import edu.willbook_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional; // <-- Importante

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional // <-- Mantiene la sesión abierta para evitar LazyInitializationException
class PrestamoRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EjemplarRepository ejemplarRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void buscarPrestamosPorEstadoPendienteDeRetiroDebeRetornarResultadosReales() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Emanuel")
                .apellido("Gomez")
                .dni("40123456")
                .email("emanuel@test.com")
                .password("hash")
                .rol(Rol.LECTOR)
                .activo(true)
                .build());

        Autor autor = autorRepository.save(Autor.builder()
                .nombre("Jorge Luis")
                .apellido("Borges")
                .build());

        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Literatura")
                .build());

        Libro libro = libroRepository.save(Libro.builder()
                .titulo("Ficciones")
                .isbn("978-9500419918")
                .anioPublicacion(1944)
                .autor(autor)
                .categoria(categoria)
                .disponible(true)
                .build());

        Ejemplar ejemplar = ejemplarRepository.save(Ejemplar.builder()
                .libro(libro)
                .codigoInventario("INV-999")
                .estado(EstadoEjemplar.RESERVADO)
                .build());

        prestamoRepository.save(Prestamo.builder()
                .usuario(usuario)
                .ejemplar(ejemplar)
                .estado(EstadoPrestamo.PENDIENTE_RETIRO)
                .fechaSolicitud(LocalDateTime.now())
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(7))
                .build());

        List<Prestamo> pendientes = prestamoRepository.findByEstado(EstadoPrestamo.PENDIENTE_RETIRO);

        assertFalse(pendientes.isEmpty());
        assertEquals(EstadoPrestamo.PENDIENTE_RETIRO, pendientes.get(0).getEstado());
        assertEquals("40123456", pendientes.get(0).getUsuario().getDni());
        assertEquals("INV-999", pendientes.get(0).getEjemplar().getCodigoInventario());
        assertEquals("Ficciones", pendientes.get(0).getEjemplar().getLibro().getTitulo());
    }
}