package com.pixpax.app.repository;

import com.pixpax.app.entity.TipoMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMercanciaRepository extends JpaRepository<TipoMercancia, Long> {
}
