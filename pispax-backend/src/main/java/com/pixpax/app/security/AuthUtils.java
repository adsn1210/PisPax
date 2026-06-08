package com.pixpax.app.security;

// Utilidad para sacar el ID del usuario autenticado en cualquier controller.
// Cuando JwtAuthFilter valida el token, guarda el userId en el campo "credentials"
// del objeto Authentication. Esta clase lo recupera de forma centralizada.

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

// final = no se puede heredar. Constructor privado = no se puede instanciar (solo métodos estáticos)
public final class AuthUtils {

    private AuthUtils() {}

    // Extrae el ID del usuario del token JWT procesado por JwtAuthFilter.
    // Los controllers lo usan así: Long userId = AuthUtils.getUserId(auth);
    public static Long getUserId(Authentication auth) {
        if (auth == null || !(auth.getCredentials() instanceof Long)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticación inválida");
        }
        return (Long) auth.getCredentials();
    }
}
