package com.pixpax.app.repository;

// Repositorio de usuarios. Spring Data JPA genera automáticamente el SQL
// a partir del nombre de los métodos, sin necesidad de escribir consultas.

import com.pixpax.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca un usuario por email. Devuelve Optional porque puede no existir
    Optional<Usuario> findByEmail(String email);

    // Comprueba si ya existe un usuario con ese email (para evitar duplicados en el registro)
    boolean existsByEmail(String email);
}
