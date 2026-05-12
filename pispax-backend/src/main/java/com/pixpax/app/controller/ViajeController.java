package com.pixpax.app.controller;

import com.pixpax.app.dto.ViajeDTO;
import com.pixpax.app.dto.request.AceptarViajeRequest;
import com.pixpax.app.dto.request.ActualizarEstadoRequest;
import com.pixpax.app.dto.request.CrearViajeRequest;
import com.pixpax.app.service.ViajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

    private final ViajeService viajeService;

    public ViajeController(ViajeService viajeService) {
        this.viajeService = viajeService;
    }

    @GetMapping
    public ResponseEntity<List<ViajeDTO>> getMisViajes(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(viajeService.getMisViajes(userId));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<List<ViajeDTO>> getViajesDisponibles(Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(viajeService.getViajesDisponibles(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViajeDTO> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(viajeService.getDetalle(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ViajeDTO> crearViaje(@Valid @RequestBody CrearViajeRequest request,
                                               Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.status(HttpStatus.CREATED).body(viajeService.crearViaje(request, userId));
    }

    @PutMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<ViajeDTO> aceptarViaje(@PathVariable Long id,
                                                  @Valid @RequestBody AceptarViajeRequest request,
                                                  Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(viajeService.aceptarViaje(id, request, userId));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<ViajeDTO> actualizarEstado(@PathVariable Long id,
                                                      @Valid @RequestBody ActualizarEstadoRequest request,
                                                      Authentication auth) {
        Long userId = (Long) auth.getCredentials();
        return ResponseEntity.ok(viajeService.actualizarEstado(id, request, userId));
    }
}
