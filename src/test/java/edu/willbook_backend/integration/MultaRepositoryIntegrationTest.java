package edu.willbook_backend.integration;

import edu.willbook_backend.BaseIntegrationTest;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.Rol;
import edu.willbook_backend.model.Localidad;
import edu.willbook_backend.model.Multa;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class MultaRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MultaRepository multaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Test
    void verificarExistenciaDeMultasPendientesPorUsuarioEnBaseDeDatosReal() {
        Localidad localidad = localidadRepository.save(Localidad.builder()
                .nombre("Santa Fe")
                .provincia("Santa Fe")
                .direccionSucursal("Calle Principal 100")
                .build());

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Mariana")
                .apellido("Benítez")
                .dni("35987654")
                .email("mariana@test.com")
                .password("hash")
                .rol(Rol.LECTOR)
                .localidad(localidad)
                .activo(true)
                .build());

        multaRepository.save(Multa.builder()
                .usuario(usuario)
                .motivo("Demora excesiva en la devolución del libro")
                .monto(new BigDecimal("1500.00"))
                .estado(EstadoMulta.PENDIENTE)
                .build());

        boolean tieneMultasPendientes = multaRepository.existsByUsuarioIdAndEstado(usuario.getId(), EstadoMulta.PENDIENTE);
        List<Multa> multasDelUsuario = multaRepository.findByUsuarioIdOrderByFechaGeneracionDesc(usuario.getId());

        assertTrue(tieneMultasPendientes, "El usuario debería tener multas pendientes registradas");
        assertFalse(multasDelUsuario.isEmpty());
        assertEquals(EstadoMulta.PENDIENTE, multasDelUsuario.get(0).getEstado());
        assertEquals(new BigDecimal("1500.00"), multasDelUsuario.get(0).getMonto());
    }
}