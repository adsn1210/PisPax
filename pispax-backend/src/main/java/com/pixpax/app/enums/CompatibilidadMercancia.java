package com.pixpax.app.enums;

// Dice si un vehículo es compatible con un tipo de mercancía concreto.
// Se guarda en la tabla vehiculo_tipo_mercancia (relación muchos a muchos).
public enum CompatibilidadMercancia {
    SI,              // Compatible sin ninguna restricción
    CON_REQUISITOS   // Compatible solo bajo condiciones especiales (ej: temperatura controlada)
}
