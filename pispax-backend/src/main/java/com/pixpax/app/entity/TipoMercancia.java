package com.pixpax.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_mercancia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoMercancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "nombre_db", nullable = false, unique = true, length = 100)
    private String nombreDb;

    @Column(length = 500)
    private String descripcion;
}
