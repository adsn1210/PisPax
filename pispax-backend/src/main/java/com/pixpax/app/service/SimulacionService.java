package com.pixpax.app.service;

// Servicio de simulación del viaje.
// Cuando el transportista pulsa "Simular viaje" en la app, este servicio ejecuta
// automáticamente todos los cambios de estado del viaje con pausas entre ellos,
// como si fuera un viaje real acelerado para la demo.
//
// @Async hace que se ejecute en un hilo aparte: el servidor responde al cliente
// inmediatamente y la simulación va avanzando sola por detrás.

import com.pixpax.app.entity.Usuario;
import com.pixpax.app.entity.Vehiculo;
import com.pixpax.app.entity.Viaje;
import com.pixpax.app.enums.EstadoViaje;
import com.pixpax.app.repository.UsuarioRepository;
import com.pixpax.app.repository.VehiculoRepository;
import com.pixpax.app.repository.ViajeRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimulacionService {

    // Tiempo de espera (en milisegundos) entre cada cambio de estado
    private static final int DELAY_ACEPTADO           = 4000; // 4 segundos
    private static final int DELAY_SALIDA_RECOGIDA    = 5000;
    private static final int DELAY_LLEGADA_RECOGIDA   = 4000;
    private static final int DELAY_MERCANCIA_RECOGIDA = 5000;
    private static final int DELAY_SALIDA_ENTREGA     = 6000;
    private static final int DELAY_LLEGADA_ENTREGA    = 4000;

    private final ViajeRepository viajeRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public SimulacionService(ViajeRepository viajeRepository,
                             VehiculoRepository vehiculoRepository,
                             UsuarioRepository usuarioRepository) {
        this.viajeRepository = viajeRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // @Async = este método se ejecuta en un hilo separado y no bloquea la respuesta HTTP
    @Async
    public void simularViaje(Long viajeId, Long transportistaId) {
        try {
            Viaje viaje = viajeRepository.findById(viajeId).orElse(null);
            if (viaje == null) return;

            // Si el viaje está PENDIENTE, lo aceptamos automáticamente con el primer vehículo del transportista
            if (viaje.getEstado() == EstadoViaje.PENDIENTE) {
                List<Vehiculo> vehiculos = vehiculoRepository.findByTransportistaId(transportistaId);
                if (vehiculos.isEmpty()) return; // sin vehículo no podemos simular

                Usuario transportista = usuarioRepository.findById(transportistaId).orElse(null);
                if (transportista == null) return;

                viaje.setTransportista(transportista);
                viaje.setVehiculo(vehiculos.get(0)); // usamos el primer vehículo disponible
                viaje.setEstado(EstadoViaje.ACEPTADO);
                viaje.setFechaInicio(LocalDateTime.now());
                viajeRepository.save(viaje);
            }

            // Avanzamos el estado con pausas para simular el tiempo real del viaje
            Thread.sleep(DELAY_ACEPTADO);
            cambiarEstado(viajeId, EstadoViaje.SALIDA_RECOGIDA);

            Thread.sleep(DELAY_SALIDA_RECOGIDA);
            cambiarEstado(viajeId, EstadoViaje.LLEGADA_RECOGIDA);

            Thread.sleep(DELAY_LLEGADA_RECOGIDA);
            cambiarEstado(viajeId, EstadoViaje.MERCANCIA_RECOGIDA);

            Thread.sleep(DELAY_MERCANCIA_RECOGIDA);
            cambiarEstado(viajeId, EstadoViaje.SALIDA_ENTREGA);

            Thread.sleep(DELAY_SALIDA_ENTREGA);
            cambiarEstado(viajeId, EstadoViaje.LLEGADA_ENTREGA);

            Thread.sleep(DELAY_LLEGADA_ENTREGA);
            cambiarEstado(viajeId, EstadoViaje.COMPLETADO);

        } catch (InterruptedException e) {
            // Si el hilo se interrumpe, restauramos la bandera de interrupción y salimos
            Thread.currentThread().interrupt();
        }
    }

    // Cambia el estado del viaje en la base de datos
    private void cambiarEstado(Long viajeId, EstadoViaje nuevoEstado) {
        Viaje viaje = viajeRepository.findById(viajeId).orElse(null);
        if (viaje == null) return;

        viaje.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoViaje.COMPLETADO) {
            viaje.setFechaFin(LocalDateTime.now());
        }
        viajeRepository.save(viaje);
    }
}
