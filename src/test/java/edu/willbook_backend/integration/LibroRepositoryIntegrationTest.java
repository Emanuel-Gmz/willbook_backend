package edu.willbook_backend.integration;

import edu.willbook_backend.BaseIntegrationTest;
import edu.willbook_backend.model.Autor;
import edu.willbook_backend.model.Categoria;
import edu.willbook_backend.model.Libro;
import edu.willbook_backend.repository.AutorRepository;
import edu.willbook_backend.repository.CategoriaRepository;
import edu.willbook_backend.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class LibroRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void buscarPorTituloOAutorDebeRetornarCoincidenciasReales() {
        Autor autor = autorRepository.save(Autor.builder()
                .nombre("Gabriel")
                .apellido("García Márquez")
                .build());

        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Ficción")
                .build());

        libroRepository.save(Libro.builder()
                .titulo("Cien Años de Soledad")
                .isbn("978-0307474728")
                .anioPublicacion(1967)
                .autor(autor)
                .categoria(categoria)
                .disponible(true)
                .build());

        List<Libro> resultados = libroRepository.buscarPorTituloOAutor("García");

        assertFalse(resultados.isEmpty());
        assertEquals("Cien Años de Soledad", resultados.get(0).getTitulo());
        assertEquals("Gabriel García Márquez", resultados.get(0).getAutor().getNombre() + " " + resultados.get(0).getAutor().getApellido());
    }
}