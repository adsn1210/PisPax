package com.pixpax.app.dto;

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
