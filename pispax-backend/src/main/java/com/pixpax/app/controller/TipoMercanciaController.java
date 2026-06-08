package com.pixpax.app.controller;

// Controller del catálogo de tipos de mercancía.
// Solo tiene un endpoint público (sin token) que devuelve los 12 tipos disponibles.
// El cliente Android lo llama al abrir el formulario de crear viaje para mostrar el desplegable.
// Ruta base: /api/tipo-mercancia

import com.pixpax.app.dto.TipoMercanciaDTO;
import com.pixpax.app.service.TipoMercanciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-mercancia")
public class TipoMercanciaController {

    private final TipoMercanciaService tipoMercanciaService;

    public TipoMercanciaController(TipoMercanciaService tipoMercanciaService) {
        this.tipoMercanciaService = tipoMercanciaService;
    }

    // GET /api/tipo-mercancia — devuelve los 12 tipos de mercancía del catálogo
    // Endpoint público: no hace falta token JWT para llamarlo
    @GetMapping
    public ResponseEntity<List<TipoMercanciaDTO>> getCatalogo() {
        return ResponseEntity.ok(tipoMercanciaService.getCatalogo());
    }
}
