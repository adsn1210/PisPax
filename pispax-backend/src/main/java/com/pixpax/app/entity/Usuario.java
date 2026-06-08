package com.pixpax.app.entity;

// Entidad que representa a un usuario registrado en la aplicación.
// Puede ser CLIENTE (pide transportes) o TRANSPORTISTA (los realiza).
// @Entity indica a JPA que esta clase es una tabla en MySQL.
// Las anotaciones de Lombok (@Getter, @Setter, @Builder, etc.) generan
// automáticamente getters, setters y el patrón Builder para crear objetos.

import com.pixpax.app.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity                         // Esta clase mapea con la tabla "usuario" de MySQL
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // el ID lo genera MySQL automáticamente
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 200)
    private String email; // único en toda la tabla, se usa para hacer login

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // la contraseña nunca se guarda en texto plano, solo el hash BCrypt

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)  // guarda el enum como texto ("CLIENTE" o "TRANSPORTISTA") en vez de número
    @Column(nullable = false)
    private Rol rol;

    @CreationTimestamp  // Hibernate pone la fecha actual automáticamente al crear el registro
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Un transportista puede tener muchos vehículos registrados
    // FetchType.LAZY = no carga los vehículos de la BD hasta que se pidan explícitamente (más eficiente)
    @OneToMany(mappedBy = "transportista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehiculo> vehiculos;
}
