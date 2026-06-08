package com.pixpax.app.enums;

// Tipos de vehículo que se pueden registrar en la plataforma.
// El transportista elige uno de estos al dar de alta su vehículo.
public enum TipoVehiculo {
    FURGONETA,          // Vehículo ligero, necesita carnet B
    FURGON_GRANDE,      // Furgón de mayor tamaño y capacidad
    FRIGORIFICO,        // Lleva cámara de frío para mercancía perecedera
    CAMION_LIGERO,      // Camión de hasta 7.500 kg
    CAMION_PESADO,      // Camión grande, necesita carnet C
    CAMION_ARTICULADO,  // Tráiler con semirremolque, carnet C+E
    PLATAFORMA          // Plataforma abierta para cargas especiales o voluminosas
}
