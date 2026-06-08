package com.pispax.app.model;

// Respuesta de POST /api/auth/login.
// Contiene el token JWT para las siguientes peticiones y los datos del usuario que ha entrado.
// SessionManager guarda ambos en SharedPreferences al hacer login.
public class LoginResponse {
    // Token JWT que se añade como cabecera "Authorization: Bearer <token>" en cada peticion
    private String token;

    // Datos del usuario autenticado (id, nombre, email, rol…)
    private UsuarioDTO usuario;

    public String getToken()       { return token; }
    public UsuarioDTO getUsuario() { return usuario; }
}
