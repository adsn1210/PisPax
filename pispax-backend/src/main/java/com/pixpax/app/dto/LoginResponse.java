package com.pixpax.app.dto;

// Lo que devuelve el servidor cuando el login es correcto.
// El cliente Android guarda el token y lo envía en cada petición posterior
// en la cabecera: "Authorization: Bearer <token>".

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;       // token JWT que identifica al usuario en cada petición
    private UsuarioDTO usuario; // datos del usuario logueado (para mostrar en la app)
}
