package com.pixpax.app.dto;

// DTO (Data Transfer Object) del usuario: lo que se devuelve al cliente Android.
// Nunca incluye el passwordHash, así la contraseña jamás sale del servidor.
// Se construye a partir de la entidad Usuario (ver constructor de abajo).

import com.pixpax.app.entity.Usuario;
import com.pixpax.app.enums.Rol;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private Rol rol;

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.apellidos = usuario.getApellidos();
        this.email = usuario.getEmail();
        this.telefono = usuario.getTelefono();
        this.rol = usuario.getRol();
    }
}
