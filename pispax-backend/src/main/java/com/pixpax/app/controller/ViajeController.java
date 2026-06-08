package com.pixpax.app.controller;

// Controller de viajes. Gestiona todas las operaciones sobre viajes.
// Todos los endpoints requieren token JWT (configurado en SecurityConfig).
// Algunos endpoints tienen restricción de rol adicional con @PreAuthorize.
// Ruta base: /api/viajes

import com.pixpax.app.dto.ViajeDTO;
import com.pixpax.app.dto.request.AceptarViajeRequest;
import com.pixpax.app.dto.request.ActualizarEstadoRequest;
import com.pixpax.app.dto.request.CrearViajeRequest;
import com.pixpax.app.enums.Rol;
import com.pixpax.app.security.AuthUtils;
import com.pixpax.app.service.SimulacionService;
import com.pixpax.app.service.ViajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

    private final ViajeService viajeService;
    private final SimulacionService simulacionService;

    public ViajeController(ViajeService viajeService, SimulacionService simulacionService) {
        this.viajeService = viajeService;
        this.simulacionService = simulacionService;
    }

    // GET /api/viajes — el cliente ve sus viajes, el transportista los suyos
    // "auth" contiene los datos del usuario sacados del token JWT
    @GetMapping
    public ResponseEntity<List<ViajeDTO>> getMisViajes(Authentication auth) {
        return ResponseEntity.ok(viajeService.getMisViajes(AuthUtils.getUserId(auth)));
    }

    // GET /api/viajes/disponibles — viajes PENDIENTES que puede aceptar un transportista
    // @PreAuthorize = solo pasa si el token tiene rol TRANSPORTISTA, si no devuelve 403
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<List<ViajeDTO>> getViajesDisponibles(Authentication auth) {
        return ResponseEntity.ok(viajeService.getViajesDisponibles(AuthUtils.getUserId(auth)));
    }

    // GET /api/viajes/{id} — detalle de un viaje concreto
    @GetMapping("/{id}")
    public ResponseEntity<ViajeDTO> getDetalle(@PathVariable Long id, Authentication auth) {
        // Sacamos el rol del usuario del token para pasárselo al servicio
        // auth.getAuthorities() devuelve la lista de roles con prefijo "ROLE_"
        String autoridad = auth.getAuthorities().iterator().next().getAuthority();
        Rol rol = Rol.valueOf(autoridad.replace("ROLE_", ""));
        return ResponseEntity.ok(viajeService.getDetalle(id, AuthUtils.getUserId(auth), rol));
    }

    // POST /api/viajes — el cliente crea un nuevo viaje
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ViajeDTO> crearViaje(@Valid @RequestBody CrearViajeRequest request,
                                               Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(viajeService.crearViaje(request, AuthUtils.getUserId(auth)));
    }

    // PUT /api/viajes/{id}/aceptar — el transportista acepta un viaje PENDIENTE con su vehículo
    @PutMapping("/{id}/aceptar")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<ViajeDTO> aceptarViaje(@PathVariable Long id,
                                                  @Valid @RequestBody AceptarViajeRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(viajeService.aceptarViaje(id, request, AuthUtils.getUserId(auth)));
    }

    // PUT /api/viajes/{id}/estado — el transportista avanza el estado del viaje
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<ViajeDTO> actualizarEstado(@PathVariable Long id,
                                                      @Valid @RequestBody ActualizarEstadoRequest request,
                                                      Authentication auth) {
        return ResponseEntity.ok(viajeService.actualizarEstado(id, request, AuthUtils.getUserId(auth)));
    }

    // POST /api/viajes/{id}/simular — lanza la simulación automática del viaje para la demo
    // SimulacionService ejecuta los cambios de estado en un hilo aparte (@Async),
    // por lo que este endpoint responde 200 inmediatamente sin esperar a que termine.
    @PostMapping("/{id}/simular")
    @PreAuthorize("hasRole('TRANSPORTISTA')")
    public ResponseEntity<Void> simularViaje(@PathVariable Long id, Authentication auth) {
        Long transportistaId = AuthUtils.getUserId(auth);
        viajeService.verificarPuedeSimular(id, transportistaId); // comprobaciones antes de lanzar
        simulacionService.simularViaje(id, transportistaId);     // arranca en segundo plano
        return ResponseEntity.ok().build();
    }
}
