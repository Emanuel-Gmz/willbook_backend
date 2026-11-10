package willbook.backend.controller;

import edu.willbook_backend.controller.LocalidadController;
import edu.willbook_backend.dto.LocalidadResponseDTO;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.LocalidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocalidadControllerTest {

    @Mock
    private LocalidadService localidadService;

    @InjectMocks
    private LocalidadController localidadController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(localidadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarLocalidadesDevuelveLista() throws Exception {
        LocalidadResponseDTO dto = new LocalidadResponseDTO(1L, "Buenos Aires", "Buenos Aires", "Calle 123");
        when(localidadService.listarLocalidades()).thenReturn(List.of(dto));

        mvc.perform(get("/api/v1/localidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Buenos Aires"));
    }
}
