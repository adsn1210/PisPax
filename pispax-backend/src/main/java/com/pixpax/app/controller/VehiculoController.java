package com.pixpax.app.controller;

import com.pixpax.app.dto.VehiculoDTO;
import com.pixpax.app.dto.request.CrearVehiculoRequest;
import com.pixpax.app.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@PreAuthorize("hasRole('TRANSPORTISTA')")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping("/mis-vehiculos")
    public ResponseEntity<List<VehiculoDTO>> getMisVehiculos(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(vehiculoService.getMisVehiculos(userId));
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crearVehiculo(@Valid @RequestBody CrearVehiculoRequest request,
                                                      Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crearVehiculo(request, userId));
    }
}
