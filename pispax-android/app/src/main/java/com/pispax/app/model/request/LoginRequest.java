package com.pispax.app.model.request;

// Cuerpo de la peticion POST /auth/login.
// Gson lo serializa a JSON: { "email": "...", "password": "..." }
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email    = email;
        this.password = password;
    }
}
