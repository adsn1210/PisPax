package com.pixpax.app.service;

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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ViajeService {

    // Transiciones válidas en orden
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

    public List<ViajeDTO> getMisViajes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRol() == Rol.CLIENTE) {
            return viajeRepository.findByClienteId(usuarioId).stream().map(ViajeDTO::new).toList();
        } else {
            return viajeRepository.findByTransportistaId(usuarioId).stream().map(ViajeDTO::new).toList();
        }
    }

    public List<ViajeDTO> getViajesDisponibles(Long transportistaId) {
        return viajeRepository.findViajesDisponiblesParaTransportista(transportistaId)
                .stream().map(ViajeDTO::new).toList();
    }

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

    public ViajeDTO actualizarEstado(Long viajeId, ActualizarEstadoRequest request, Long transportistaId) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));

        if (!viaje.getTransportista().getId().equals(transportistaId)) {
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

    public ViajeDTO getDetalle(Long viajeId) {
        return viajeRepository.findById(viajeId)
                .map(ViajeDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Viaje no encontrado"));
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
