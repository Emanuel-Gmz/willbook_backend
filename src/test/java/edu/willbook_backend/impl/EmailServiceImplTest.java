package edu.willbook_backend.impl;

import edu.willbook_backend.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // Inyectamos los valores simulados de las propiedades @Value
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@willbook.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://willbook.com/");
    }

    @Test
    void sendPasswordResetEmailExitoso() {
        emailService.sendPasswordResetEmail("user@test.com", "token123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertNotNull(sentMessage.getTo());
        assertEquals("user@test.com", sentMessage.getTo()[0]);
        assertEquals("noreply@willbook.com", sentMessage.getFrom());
        assertEquals("Recuperación de Contraseña - WillBook", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("https://willbook.com/reset-password?token=token123"));
    }

    @Test
    void sendPasswordResetEmailSinBarraAlFinalEnFrontendUrl() {
        // Probamos cuando la URL no termina en barra para validar el operador ternario de limpieza
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://willbook.com");

        emailService.sendPasswordResetEmail("user@test.com", "token456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertTrue(sentMessage.getText().contains("https://willbook.com/reset-password?token=token456"));
    }

    @Test
    void sendPasswordResetEmailManejaExcepcionInterna() {
        doThrow(new RuntimeException("Error SMTP")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@test.com", "token123"));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}