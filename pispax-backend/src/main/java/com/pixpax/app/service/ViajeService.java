package com.pixpax.app.service;

// Servicio central de la aplicación: contiene toda la lógica de negocio de los viajes.
// Aquí se crean viajes, se aceptan, se avanza su estado y se valida que nadie haga
// transiciones ilegales (ej: saltar de PENDIENTE a COMPLETADO directamente).

import com.pixpax.app.dto.ViajeDTO;
import com.pixpax.app.dto.request.AceptarViajeRequest;
import com.pixpax.app.dto.request.ActualizarEstadoRequest;
import com.pixpax.app.dto.request.CrearViajeRequest;
import com.pixpax.app.entity.*;
import com.pixpax.app.enums.EstadoViaje;
import com.pixpax.app.enums.Rol;
import com.pixpax.app.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ViajeService {

    // El orden correcto de estados de un viaje. Se usa para validar que la transición
    // pedida es la siguiente del flujo y no un salto o un retroceso.
    private static final List<EstadoViaje> FLUJO_NORMAL = List.of(
            EstadoViaje.PENDIENTE,
            EstadoViaje.ACEPTADO,
            EstadoViaje.SALIDA_RECOGIDA,
            EstadoViaje.LLEGADA_RECOGIDA,
            EstadoViaje.MERCANCIA_RECOGIDA,
            EstadoViaje.SALIDA_ENTREGA,
            EstadoViaje.LLEGADA_ENTREGA,
            EstadoViaje.COMPLETADO
    );

    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TipoMercanciaRepository tipoMercanciaRepository;

    public ViajeService(ViajeRepository viajeRepository,
                        UsuarioRepository usuarioRepository,
                        VehiculoRepository vehiculoRepository,
                        TipoMercanciaRepository tipoMercanciaRepository) {
        this.viajeRepository = viajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.tipoMercanciaRepository = tipoMercanciaRepository;
    }

    @Transactional(readOnly = true)
    public List<ViajeDTO> getMisViajes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() == Rol.CLIENTE) {
            return viajeRepository.findByClienteId(usuarioId).stream().map(ViajeDTO::new).toList();
        } else {
            return viajeRepository.findByTransportistaId(usuarioId).stream().map(ViajeDTO::new).toList();
        }
    }

    @Transactional(readOnly = true)
    public List<ViajeDTO> getViajesDisponibles(Long transportistaId) {
        return viajeRepository.findByEstado(EstadoViaje.PENDIENTE)
                .stream().map(ViajeDTO::new).toList();
    }

    @Transactional
    public ViajeDTO crearViaje(CrearViajeRequest request, Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        if (cliente.getRol() != Rol.CLIENTE) {
            throw new IllegalArgumentException("Solo los clientes pueden crear viajes");
        }

        TipoMercancia tipoMercancia = tipoMercanciaRepository.findById(request.getTipoMercanciaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de mercancía no encontrado"));

        Viaje viaje = Viaje.builder()
                .cliente(cliente)
                .tipoMercancia(tipoMercancia)
                .descripcionMercancia(request.getDescripcionMercancia())
                .direccionRecogida(request.getDireccionRecogida())
                .direccionEntrega(request.getDireccionEntrega())
                .pesoKg(request.getPesoKg())
                .estado(EstadoViaje.PENDIENTE)
                .build();

        return new ViajeDTO(viajeRepository.save(viaje));
    }

    @Transactional
    public ViajeDTO aceptarViaje(Long viajeId, AceptarViajeRequest request, Long transportistaId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (viaje.getEstado() != EstadoViaje.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aceptar viajes en estado PENDIENTE");
        }

        Usuario transportista = usuarioRepository.findById(transportistaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transportista no encontrado"));

        Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado"));

        if (!vehiculo.getTransportista().getId().equals(transportistaId)) {
            throw new IllegalArgumentException("El vehículo no pertenece a este transportista");
        }

        if (viaje.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException("El peso del viaje excede la capacidad del vehículo");
        }

        viaje.setTransportista(transportista);
        viaje.setVehiculo(vehiculo);
        viaje.setEstado(EstadoViaje.ACEPTADO);
        viaje.setFechaInicio(LocalDateTime.now());

        return new ViajeDTO(viajeRepository.save(viaje));
    }

    @Transactional
    public ViajeDTO actualizarEstado(Long viajeId, ActualizarEstadoRequest request, Long transportistaId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (viaje.getTransportista() == null || !viaje.getTransportista().getId().equals(transportistaId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para actualizar este viaje");
        }

        EstadoViaje estadoActual = viaje.getEstado();
        EstadoViaje nuevoEstado = request.getNuevoEstado();

        validarTransicion(estadoActual, nuevoEstado);

        viaje.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoViaje.COMPLETADO || nuevoEstado == EstadoViaje.CANCELADO) {
            viaje.setFechaFin(LocalDateTime.now());
        }

        return new ViajeDTO(viajeRepository.save(viaje));
    }

    @Transactional(readOnly = true)
    public ViajeDTO getDetalle(Long viajeId, Long usuarioId, Rol rolUsuario) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        boolean tieneAcceso;
        if (rolUsuario == Rol.CLIENTE) {
            tieneAcceso = viaje.getCliente().getId().equals(usuarioId);
        } else {
            // TRANSPORTISTA: puede ver sus viajes asignados o cualquier viaje disponible (PENDIENTE)
            boolean esTransportistaAsignado = viaje.getTransportista() != null
                    && viaje.getTransportista().getId().equals(usuarioId);
            tieneAcceso = esTransportistaAsignado || viaje.getEstado() == EstadoViaje.PENDIENTE;
        }

        if (!tieneAcceso) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este viaje");
        }

        return new ViajeDTO(viaje);
    }

    public void verificarPuedeSimular(Long viajeId, Long transportistaId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        EstadoViaje estado = viaje.getEstado();

        if (estado != EstadoViaje.PENDIENTE && estado != EstadoViaje.ACEPTADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede simular un viaje en estado PENDIENTE o ACEPTADO");
        }

        // Si ya está aceptado, solo el transportista asignado puede simularlo
        if (estado == EstadoViaje.ACEPTADO) {
            boolean esElAsignado = viaje.getTransportista() != null
                    && viaje.getTransportista().getId().equals(transportistaId);
            if (!esElAsignado) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Solo el transportista asignado puede simular este viaje");
            }
        }
    }

    private void validarTransicion(EstadoViaje actual, EstadoViaje nuevo) {
        if (nuevo == EstadoViaje.CANCELADO) {
            if (actual == EstadoViaje.COMPLETADO || actual == EstadoViaje.CANCELADO) {
                throw new IllegalStateException("No se puede cancelar un viaje ya finalizado");
            }
            return;
        }

        int indexActual = FLUJO_NORMAL.indexOf(actual);
        int indexNuevo = FLUJO_NORMAL.indexOf(nuevo);

        if (indexNuevo != indexActual + 1) {
            throw new IllegalStateException(
                    "Transición inválida: " + actual + " → " + nuevo);
        }
    }
}
