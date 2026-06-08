package com.pixpax.app.entity;

// Catálogo fijo de tipos de mercancía que se pueden transportar.
// Los 12 registros los carga data.sql al arrancar la aplicación.
// El cliente elige uno de estos tipos al crear un viaje.
// Mapea con la tabla "tipo_mercancia" en MySQL.

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
    private String nombre; // nombre visible al usuario, ej: "Paquetería general"

    @Column(name = "nombre_db", nullable = false, unique = true, length = 100)
    private String nombreDb; // nombre interno para búsquedas en código, ej: "paqueteria_general"

    @Column(length = 500)
    private String descripcion; // explicación breve del tipo de mercancía
}
