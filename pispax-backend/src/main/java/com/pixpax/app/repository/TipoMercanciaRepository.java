package com.pixpax.app.repository;

// Repositorio del catálogo de tipos de mercancía.
// Solo se usa para leer los datos, nunca se insertan desde la app (los carga data.sql).

import com.pixpax.app.entity.TipoMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMercanciaRepository extends JpaRepository<TipoMercancia, Long> {
    // Busca un tipo de mercancía por su nombre interno, ej: "paqueteria_general"
    // Se usa en DataInitializer para crear el viaje demo con el tipo correcto
    Optional<TipoMercancia> findByNombreDb(String nombreDb);
}
