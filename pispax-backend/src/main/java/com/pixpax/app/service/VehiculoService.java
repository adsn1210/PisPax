package com.pixpax.app.service;

// Servicio de vehículos: permite a los transportistas ver y registrar sus vehículos.
// Solo pueden acceder usuarios con rol TRANSPORTISTA (lo controla el controller con @PreAuthorize).

import com.pixpax.app.dto.VehiculoDTO;
import com.pixpax.app.dto.request.CrearVehiculoRequest;
import com.pixpax.app.entity.Usuario;
import com.pixpax.app.entity.Vehiculo;
import com.pixpax.app.repository.UsuarioRepository;
import com.pixpax.app.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, UsuarioRepository usuarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Devuelve todos los vehículos registrados por este transportista
    @Transactional(readOnly = true)
    public List<VehiculoDTO> getMisVehiculos(Long transportistaId) {
        return vehiculoRepository.findByTransportistaId(transportistaId)
                .stream()
                .map(VehiculoDTO::new)
                .toList();
    }

    @Transactional
    public VehiculoDTO crearVehiculo(CrearVehiculoRequest request, Long transportistaId) {
        // No puede haber dos vehículos con la misma matrícula en toda la plataforma
        if (vehiculoRepository.existsByMatricula(request.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un vehículo con esa matrícula");
        }

        Usuario transportista = usuarioRepository.findById(transportistaId)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado"));

        Vehiculo vehiculo = Vehiculo.builder()
                .matricula(request.getMatricula().trim().toUpperCase()) // normalizamos la matrícula a mayúsculas
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .tipoVehiculo(request.getTipoVehiculo())
                .subtipo(request.getSubtipo())
                .taraKg(request.getTaraKg())
                .capacidadKg(request.getCapacidadKg())
                .mmaKg(request.getMmaKg())
                .carnetRequerido(request.getCarnetRequerido())
                .transportista(transportista)
                .build();

        return new VehiculoDTO(vehiculoRepository.save(vehiculo));
    }
}
