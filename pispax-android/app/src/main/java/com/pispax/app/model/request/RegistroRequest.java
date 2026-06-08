package com.pispax.app.model.request;

// Cuerpo de la peticion POST /auth/registro.
// Contiene todos los datos del formulario de creacion de cuenta.
public class RegistroRequest {
    private String nombre;
    private String apellidos;
    private String email;
    private String password;
    private String telefono;
    private String rol; // "CLIENTE" o "TRANSPORTISTA" segun el RadioButton seleccionado

    public RegistroRequest(String nombre, String apellidos, String email,
                           String password, String telefono, String rol) {
        this.nombre    = nombre;
        this.apellidos = apellidos;
        this.email     = email;
        this.password  = password;
        this.telefono  = telefono;
        this.rol       = rol;
    }
}
