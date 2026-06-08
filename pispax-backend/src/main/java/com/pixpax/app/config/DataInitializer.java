package com.pixpax.app.config;

// Crea datos de prueba en la base de datos al arrancar la aplicación.
// Solo se ejecuta si los datos no existen ya (comprueba el email del cliente demo).
// Esto permite arrancar el servidor y tener de inmediato un cliente, un transportista,
// un vehículo y un viaje listos para hacer la demo del TFG sin configurar nada a mano.

import com.pixpax.app.entity.*;
import com.pixpax.app.enums.*;
import com.pixpax.app.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    // CommandLineRunner = Spring ejecuta este método una vez al arrancar, después de todo lo demás
    @Bean
    public CommandLineRunner initDemoData(
            UsuarioRepository usuarioRepository,
            VehiculoRepository vehiculoRepository,
            ViajeRepository viajeRepository,
            TipoMercanciaRepository tipoMercanciaRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (usuarioRepository.existsByEmail("demo.cliente@pispax.com")) {
                return;
            }

            // ── Usuario cliente demo ──────────────────────────────────────────────
            Usuario cliente = usuarioRepository.save(Usuario.builder()
                    .nombre("María")
                    .apellidos("López Fernández")
                    .email("demo.cliente@pispax.com")
                    .passwordHash(passwordEncoder.encode("Demo1234"))
                    .telefono("612345678")
                    .rol(Rol.CLIENTE)
                    .build());

            // ── Usuario transportista demo ────────────────────────────────────────
            Usuario transportista = usuarioRepository.save(Usuario.builder()
                    .nombre("Carlos")
                    .apellidos("García Martínez")
                    .email("demo.transportista@pispax.com")
                    .passwordHash(passwordEncoder.encode("Demo1234"))
                    .telefono("698765432")
                    .rol(Rol.TRANSPORTISTA)
                    .build());

            // ── Vehículo demo ─────────────────────────────────────────────────────
            vehiculoRepository.save(Vehiculo.builder()
                    .matricula("1234ABC")
                    .marca("Renault")
                    .modelo("Master")
                    .tipoVehiculo(TipoVehiculo.FURGON_GRANDE)
                    .subtipo("Caja cerrada isotérmica")
                    .taraKg(new BigDecimal("1850.00"))
                    .capacidadKg(new BigDecimal("1500.00"))
                    .mmaKg(new BigDecimal("3500.00"))
                    .carnetRequerido(CarnetRequerido.B)
                    .transportista(transportista)
                    .build());

            // ── Viaje ficticio de presentación (PENDIENTE) ────────────────────────
            // El transportista lo encontrará en /disponibles, lo aceptará y
            // avanzará por todos los estados durante la demo.
            TipoMercancia paqueteria = tipoMercanciaRepository.findByNombreDb("paqueteria_general")
                    .orElseThrow(() -> new IllegalStateException(
                            "Catálogo tipo_mercancia no inicializado — revisa data.sql"));

            viajeRepository.save(Viaje.builder()
                    .cliente(cliente)
                    .tipoMercancia(paqueteria)
                    .descripcionMercancia("12 palés de ropa deportiva temporada verano — frágil, no apilar más de 2 alturas")
                    .direccionRecogida("Polígono Industrial Can Fontanet, C/ de la Indústria 8, 08225 Terrassa, Barcelona")
                    .direccionEntrega("Centro Logístico Mercamadrid, Parcela M-40, 28053 Madrid")
                    .pesoKg(new BigDecimal("480.00"))
                    .estado(EstadoViaje.PENDIENTE)
                    .build());
        };
    }
}
