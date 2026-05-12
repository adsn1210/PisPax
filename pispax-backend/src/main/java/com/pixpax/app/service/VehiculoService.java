package com.pixpax.app.service;

import com.pixpax.app.dto.VehiculoDTO;
import com.pixpax.app.dto.request.CrearVehiculoRequest;
import com.pixpax.app.entity.Usuario;
import com.pixpax.app.entity.Vehiculo;
import com.pixpax.app.repository.UsuarioRepository;
import com.pixpax.app.repository.VehiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, UsuarioRepository usuarioRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<VehiculoDTO> getMisVehiculos(Long transportistaId) {
        return vehiculoRepository.findByTransportistaId(transportistaId)
                .stream()
                .map(VehiculoDTO::new)
                .toList();
    }

    public VehiculoDTO crearVehiculo(CrearVehiculoRequest request, Long transportistaId) {
        if (vehiculoRepository.existsByMatricula(request.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un vehículo con esa matrícula");
        }

        Usuario transportista = usuarioRepository.findById(transportistaId)
                .orElseThrow(() -> new IllegalArgumentException("Transportista no encontrado"));

        Vehiculo vehiculo = Vehiculo.builder()
                .matricula(request.getMatricula().toUpperCase())
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
