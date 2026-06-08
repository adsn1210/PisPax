package com.pixpax.app;

// Clase principal de la aplicación PisPax.
// Cuando ejecutas "mvn spring-boot:run", Java entra por este main().
// Spring Boot se encarga de levantar el servidor, conectar con MySQL
// y registrar todos los controladores y servicios automáticamente.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

// @SpringBootApplication = arranca todo Spring Boot de golpe (configuración, componentes, etc.)
// @EnableAsync = permite que SimulacionService ejecute el viaje en un hilo aparte sin bloquear la respuesta
@SpringBootApplication
@EnableAsync
public class PispaxApplication {
    public static void main(String[] args) {
        SpringApplication.run(PispaxApplication.class, args);
    }
}
