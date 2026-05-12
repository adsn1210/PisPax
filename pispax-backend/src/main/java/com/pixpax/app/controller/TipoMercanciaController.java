package com.pixpax.app.controller;

import com.pixpax.app.entity.TipoMercancia;
import com.pixpax.app.repository.TipoMercanciaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-mercancia")
public class TipoMercanciaController {

    private final TipoMercanciaRepository tipoMercanciaRepository;

    public TipoMercanciaController(TipoMercanciaRepository tipoMercanciaRepository) {
        this.tipoMercanciaRepository = tipoMercanciaRepository;
    }

    @GetMapping
    public ResponseEntity<List<TipoMercancia>> getCatalogo() {
        return ResponseEntity.ok(tipoMercanciaRepository.findAll());
    }
}
