package com.pixpax.app.exception;

// Formato estándar de los errores que devuelve la API.
// Cuando algo falla, el cliente Android siempre recibe un JSON con esta estructura:
// { "mensaje": "descripción del error", "codigo": 400 }
// Así el front sabe exactamente qué mostrar al usuario.

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String mensaje; // descripción legible del error
    private int codigo;     // código HTTP (400, 401, 403, 404, 409, 500...)
}
