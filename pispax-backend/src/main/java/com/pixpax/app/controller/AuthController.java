package com.pixpax.app.controller;

// Controller de autenticación. Maneja el registro y el login de usuarios.
// Estos endpoints son públicos: no necesitan token JWT para usarse.
// Ruta base: /api/auth

import com.pixpax.app.dto.LoginResponse;
import com.pixpax.app.dto.UsuarioDTO;
import com.pixpax.app.dto.request.LoginRequest;
import com.pixpax.app.dto.request.RegistroRequest;
import com.pixpax.app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController = controller que devuelve JSON automáticamente (incluye Jackson)
// @RequestMapping = prefijo de ruta para todos los métodos de esta clase
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Inyección por constructor (recomendada sobre @Autowired)
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/registro — crea un nuevo usuario (CLIENTE o TRANSPORTISTA)
    // @Valid = activa las validaciones definidas en RegistroRequest antes de ejecutar
    // ResponseEntity.status(201) = devolvemos 201 Created en vez del 200 por defecto
    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registro(request));
    }

    // POST /api/auth/login — devuelve un token JWT si las credenciales son correctas
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
