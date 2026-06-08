package com.pixpax.app.service;

// Servicio del catálogo de mercancías. Solo tiene una función: devolver los 12 tipos
// que se cargaron en data.sql al arrancar. Es un endpoint público, sin token JWT.

import com.pixpax.app.dto.TipoMercanciaDTO;
import com.pixpax.app.repository.TipoMercanciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoMercanciaService {

    private final TipoMercanciaRepository tipoMercanciaRepository;

    public TipoMercanciaService(TipoMercanciaRepository tipoMercanciaRepository) {
        this.tipoMercanciaRepository = tipoMercanciaRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoMercanciaDTO> getCatalogo() {
        // Coge todos los tipos de mercancía de la BD y los convierte a DTO
        // TipoMercanciaDTO::new es una referencia al constructor (equivalente a t -> new TipoMercanciaDTO(t))
        return tipoMercanciaRepository.findAll()
                .stream()
                .map(TipoMercanciaDTO::new)
                .toList();
    }
}
