package edu.willbook_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = BackendApplication.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// Verifica que el contexto de Spring Boot cargue correctamente
	}

	@Test
	void mainMethodTest() {
		assertDoesNotThrow(() -> BackendApplication.main(new String[]{}));
	}

}