package edu.willbook_backend.controller;

import edu.willbook_backend.controller.EjemplarController;
import edu.willbook_backend.exception.GlobalExceptionHandler;
import edu.willbook_backend.service.EjemplarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EjemplarControllerTest {

    @Mock
    private EjemplarService ejemplarService;

    @InjectMocks
    private EjemplarController ejemplarController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(ejemplarController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void darDeBajaEjemplarDevuelveOk() throws Exception {
        when(ejemplarService.darDeBajaEjemplar(eq("INV-001"))).thenReturn(Map.of("message", "Ejemplar dado de baja exitosamente"));

        mvc.perform(delete("/api/v1/ejemplares/INV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ejemplar dado de baja exitosamente"));
    }
}
