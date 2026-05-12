package com.pixpax.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email con formato inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
