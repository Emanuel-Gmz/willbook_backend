package edu.willbook_backend.impl;

import edu.willbook_backend.dto.MultaCreateDTO;
import edu.willbook_backend.dto.MultaPagoDTO;
import edu.willbook_backend.enums.EstadoMulta;
import edu.willbook_backend.enums.MetodoPago;
import edu.willbook_backend.model.Multa;
import edu.willbook_backend.model.Usuario;
import edu.willbook_backend.repository.MultaRepository;
import edu.willbook_backend.repository.UsuarioRepository;
import edu.willbook_backend.service.impl.MultaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultaServiceImplTest {

    @Mock MultaRepository multaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks
    MultaServiceImpl servicio;

    Usuario usuario;
    Multa multa;

    @BeforeEach
    void armar() {
        usuario = Usuario.builder().id(1L).nombre("Juan").apellido("Perez").email("juan@test.com").build();
        multa = Multa.builder().id(1L).usuario(usuario).monto(BigDecimal.TEN).estado(EstadoMulta.PENDIENTE).build();
    }

    @Test
    void crearMultaManualExitosa() {
        MultaCreateDTO dto = new MultaCreateDTO(1L, "Demora", BigDecimal.TEN);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(multaRepository.save(any())).thenReturn(multa);

        Map<String, Object> resp = servicio.crearMultaManual(dto);

        assertEquals("Multa cargada exitosamente", resp.get("message"));
        verify(multaRepository).save(any());
    }

    @Test
    void registrarPagoMultaActivaExitoso() {
        MultaPagoDTO pagoDTO = new MultaPagoDTO(MetodoPago.EFECTIVO);
        when(multaRepository.findById(1L)).thenReturn(Optional.of(multa));
        when(multaRepository.existsByUsuarioIdAndEstado(1L, EstadoMulta.PENDIENTE)).thenReturn(false);

        Map<String, Object> resp = servicio.registrarPagoMulta(1L, pagoDTO);

        assertEquals(EstadoMulta.PAGADA.name(), resp.get("estado"));
        verify(multaRepository).save(multa);
    }

    @Test
    void consultarMisMultasVacias() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(multaRepository.findByUsuarioIdOrderByFechaGeneracionDesc(1L)).thenReturn(java.util.List.of());

        var misMultas = servicio.consultarMisMultas("juan@test.com");
        assertFalse(misMultas.cuentaRestringida());
    }
}