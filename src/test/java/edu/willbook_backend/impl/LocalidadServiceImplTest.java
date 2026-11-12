package edu.willbook_backend.impl;

import edu.willbook_backend.dto.LocalidadResponseDTO;
import edu.willbook_backend.model.Localidad;
import edu.willbook_backend.repository.LocalidadRepository;
import edu.willbook_backend.service.impl.LocalidadServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalidadServiceImplTest {

    @Mock LocalidadRepository localidadRepository;
    @InjectMocks
    LocalidadServiceImpl servicio;

    @Test
    void listarLocalidadesDevuelveLista() {
        Localidad loc = Localidad.builder().id(1L).nombre("Buenos Aires").provincia("Buenos Aires").direccionSucursal("Calle 1").build();
        when(localidadRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(loc));

        List<LocalidadResponseDTO> resultado = servicio.listarLocalidades();

        assertEquals(1, resultado.size());
        assertEquals("Buenos Aires", resultado.get(0).nombre());
    }
}
