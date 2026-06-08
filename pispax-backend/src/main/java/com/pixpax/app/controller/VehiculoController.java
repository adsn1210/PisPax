package com.pixpax.app.controller;

// Controller de vehículos. Permite a los transportistas gestionar sus vehículos.
// @PreAuthorize a nivel de clase = TODOS los endpoints de este controller requieren rol TRANSPORTISTA.
// Ruta base: /api/vehiculos

import com.pixpax.app.dto.VehiculoDTO;
import com.pixpax.app.dto.request.CrearVehiculoRequest;
import com.pixpax.app.security.AuthUtils;
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
@PreAuthorize("hasRole('TRANSPORTISTA')") // protección a nivel de clase: aplica a todos los métodos
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // GET /api/vehiculos/mis-vehiculos — lista de vehículos del transportista autenticado
    @GetMapping("/mis-vehiculos")
    public ResponseEntity<List<VehiculoDTO>> getMisVehiculos(Authentication auth) {
        return ResponseEntity.ok(vehiculoService.getMisVehiculos(AuthUtils.getUserId(auth)));
    }

    // POST /api/vehiculos — el transportista da de alta un nuevo vehículo
    @PostMapping
    public ResponseEntity<VehiculoDTO> crearVehiculo(@Valid @RequestBody CrearVehiculoRequest request,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehiculoService.crearVehiculo(request, AuthUtils.getUserId(auth)));
    }
}
