package com.pixpax.app.dto.request;

import com.pixpax.app.enums.Rol;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 150)
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email con formato inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Pattern(regexp = "^[0-9+\\s()-]{7,20}$", message = "Teléfono inválido")
    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
