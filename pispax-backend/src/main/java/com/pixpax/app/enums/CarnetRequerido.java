package com.pixpax.app.enums;

// Tipos de carnet de conducir según la clasificación europea.
// Se asigna al registrar un vehículo para saber qué permiso necesita el conductor.
public enum CarnetRequerido {
    B,   // Carnet de coche normal, válido para furgonetas hasta 3.500 kg
    C1,  // Camiones medianos entre 3.500 y 7.500 kg
    C,   // Camiones pesados de más de 7.500 kg
    C_E  // Camión articulado con remolque
}
