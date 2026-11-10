package edu.willbook_backend;

import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.*;
import edu.willbook_backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BackendApplication.class
)
@ActiveProfiles("test")
@Tag("integracion")
public abstract class BaseIntegrationTest {

    static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("willbook_test_db")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL_CONTAINER.start(); // singleton: Ryuk lo limpia al terminar la JVM
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "20");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "5");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "60000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "30000");
    }

    @Autowired WebApplicationContext contexto;
    @Autowired protected JwtService jwtService;
    @Autowired protected PasswordEncoder passwordEncoder;

    @Autowired protected UsuarioRepository usuarioRepository;
    @Autowired protected LibroRepository libroRepository;
    @Autowired protected AutorRepository autorRepository;
    @Autowired protected CategoriaRepository categoriaRepository;
    @Autowired protected EjemplarRepository ejemplarRepository;
    @Autowired protected PrestamoRepository prestamoRepository;
    @Autowired protected MultaRepository multaRepository;

    protected MockMvc mvc;

    @BeforeEach
    void armarMockMvc() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    protected MockHttpServletRequestBuilder como(Usuario usuario, MockHttpServletRequestBuilder pedido) {
        String token = jwtService.generateToken(Map.of("rol", usuario.getRol().name()), usuario);
        return pedido.header("Authorization", "Bearer " + token);
    }

    protected Usuario usuario(String email, String dni, Rol rol) {
        return usuarioRepository.save(Usuario.builder()
                .nombre("Test")
                .apellido("User")
                .email(email)
                .dni(dni)
                .password(passwordEncoder.encode("clave1234"))
                .rol(rol)
                .activo(true)
                .build());
    }
}