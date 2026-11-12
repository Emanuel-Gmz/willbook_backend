package edu.willbook_backend.integration;

import edu.willbook_backend.BaseIntegrationTest;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Localidad;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class UsuarioIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Test
    void buscarUsuarioPorEmailYDniDebeRetornarResultadosReales() {
        // 1. Crear y persistir una localidad de prueba
        Localidad localidad = localidadRepository.save(Localidad.builder()
                .nombre("Santa Fe")
                .provincia("Santa Fe")
                .direccionSucursal("Calle Principal 100")
                .build());

        // 2. Crear y persistir un usuario de prueba en MySQL mediante Testcontainers
        usuarioRepository.save(Usuario.builder()
                .nombre("Emanuel")
                .apellido("Gomez")
                .dni("40123456")
                .email("emanuel.gomez@test.com")
                .password("hashpassword")
                .rol(Rol.LECTOR)
                .localidad(localidad)
                .activo(true)
                .build());

        // 3. Ejecutar las consultas del repositorio
        Optional<Usuario> encontradoPorEmail = usuarioRepository.findByEmail("emanuel.gomez@test.com");
        Optional<Usuario> encontradoPorDni = usuarioRepository.findByDni("40123456");
        boolean existeDni = usuarioRepository.existsByDni("40123456");
        boolean existeEmail = usuarioRepository.existsByEmail("emanuel.gomez@test.com");

        // 4. Verificaciones
        assertTrue(encontradoPorEmail.isPresent());
        assertEquals("Emanuel", encontradoPorEmail.get().getNombre());

        assertTrue(encontradoPorDni.isPresent());
        assertEquals("Gomez", encontradoPorDni.get().getApellido());

        assertTrue(existeDni);
        assertTrue(existeEmail);
    }
}