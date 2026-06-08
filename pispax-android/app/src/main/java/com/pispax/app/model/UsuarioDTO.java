package com.pispax.app.model;

// Datos del usuario que devuelve la API.
// Se usa tanto al hacer login (dentro de LoginResponse) como para mostrar info en pantalla.
public class UsuarioDTO {

    // Identificador unico en base de datos
    private Long id;

    // Nombre de pila (ej: "Adrian")
    private String nombre;

    // Apellidos (ej: "Salazar")
    private String apellidos;

    // Email, tambien sirve como nombre de usuario para el login
    private String email;

    // Telefono opcional que el usuario puso al registrarse
    private String telefono;

    // "CLIENTE" o "TRANSPORTISTA" — determina que home se muestra al entrar
    private String rol;

    public Long getId()           { return id; }
    public String getNombre()     { return nombre; }
    public String getApellidos()  { return apellidos; }
    public String getEmail()      { return email; }
    public String getTelefono()   { return telefono; }
    public String getRol()        { return rol; }

    // Devuelve nombre + apellidos para mostrar en cabeceras (ej: "Adrian Salazar")
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
}
