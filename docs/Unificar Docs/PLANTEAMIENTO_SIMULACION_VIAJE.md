# Planteamiento — Simulación Visual de Viaje
### PisPax TFG DAM 2025/2026 — Adrián Salazar

> Este documento recoge el planteamiento acordado para implementar una simulación
> visual de un viaje completo cuando se desarrolle el frontend Android.
> **No hay código implementado aún.** Es la hoja de ruta para esa fase.

---

## El problema que resuelve

En producción, el transportista actualiza el estado del viaje manualmente desde
el móvil conforme avanza físicamente (sale a recoger, llega, carga, etc.).

Para el TFG no vamos a tener un vehículo real moviéndose, pero sí necesitamos
demostrar que el sistema funciona de principio a fin de forma visual. La solución
es una **simulación automática**: el backend avanza los estados con delays
programados y el Android anima el recorrido en pantalla en tiempo real.

---

## Arquitectura general del sistema de simulación

```
┌─────────────────────────────────────────────────────────────┐
│                     ANDROID (Frontend)                       │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  MapaViajeActivity                                   │   │
│  │                                                      │   │
│  │  ┌─────────────────────┐   ┌──────────────────────┐ │   │
│  │  │  Mapa OSMDroid      │   │  Timeline de estados  │ │   │
│  │  │                     │   │                       │ │   │
│  │  │  📍 Origen          │   │  ✅ ACEPTADO           │ │   │
│  │  │       🚛 ──────►    │   │  ✅ SALIDA_RECOGIDA    │ │   │
│  │  │             📦      │   │  🔄 LLEGADA_RECOGIDA   │ │   │
│  │  │                     │   │  ○  ...               │ │   │
│  │  └─────────────────────┘   └──────────────────────┘ │   │
│  │                                                      │   │
│  │       [ Botón: Iniciar simulación ]                  │   │
│  └──────────────────────────────────────────────────────┘   │
│                         │                                    │
│              polling GET cada 3 segundos                     │
└─────────────────────────│───────────────────────────────────┘
                          │
                          │  POST /api/viajes/{id}/simular
                          │  GET  /api/viajes/{id}
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     SPRING BOOT (Backend)                    │
│                                                              │
│  SimulacionService  (@Async — proceso en segundo plano)      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Paso 1 → ACEPTADO            espera 4 segundos      │   │
│  │  Paso 2 → SALIDA_RECOGIDA     espera 5 segundos      │   │
│  │  Paso 3 → LLEGADA_RECOGIDA    espera 4 segundos      │   │
│  │  Paso 4 → MERCANCIA_RECOGIDA  espera 5 segundos      │   │
│  │  Paso 5 → SALIDA_ENTREGA      espera 6 segundos      │   │
│  │  Paso 6 → LLEGADA_ENTREGA     espera 4 segundos      │   │
│  │  Paso 7 → COMPLETADO                                 │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  Nuevo endpoint:  POST /api/viajes/{id}/simular              │
│  Responde 200 OK inmediatamente y el proceso corre solo      │
└─────────────────────────────────────────────────────────────┘
```

---

## Opciones de animación evaluadas

### Opción 1 — OSMDroid + marcador animado ⭐ Recomendada

**Qué es OSMDroid:** librería Android para mostrar mapas de OpenStreetMap.
Gratuita, sin API key, sin tarjeta de crédito, sin límite de uso.

**Cómo funciona la animación:**
El camión no tiene GPS real. Se usan dos coordenadas hardcodeadas (origen y
destino de la simulación) y se interpola la posición del marcador según el
estado actual del viaje:

| Estado del viaje   | Posición del camión en el mapa         |
|--------------------|----------------------------------------|
| ACEPTADO           | Punto de origen (parado)               |
| SALIDA_RECOGIDA    | Animando hacia el punto de recogida    |
| LLEGADA_RECOGIDA   | Parado en el punto de recogida         |
| MERCANCIA_RECOGIDA | Parado en recogida (animación de carga)|
| SALIDA_ENTREGA     | Animando hacia el punto de destino     |
| LLEGADA_ENTREGA    | Parado en el punto de destino          |
| COMPLETADO         | Destino + icono de check verde         |

**Ventaja:** Mapa real con calles, aspecto profesional, muy reconocible.
**Desventaja:** Hay que coordinar el movimiento suave del marcador con el
polling al backend (requiere interpolación con `ValueAnimator`).

---

### Opción 2 — Lottie + timeline de estados

**Qué es Lottie:** librería de Airbnb para reproducir animaciones JSON
(descargables gratis en lottiefiles.com). Se añade como dependencia en
`build.gradle` y se reproduce con una línea de código.

**Cómo quedaría la pantalla:**

```
     ┌──────────────────────────────────────────┐
     │                                          │
     │     🚛 ~~~ animación camión Lottie ~~~   │
     │                                          │
     ├──────────────────────────────────────────┤
     │                                          │
     │  ✅  VIAJE ACEPTADO          20:46:00    │
     │  ✅  SALIDA HACIA RECOGIDA   20:46:04    │
     │  🔄  EN CAMINO A RECOGIDA    ...         │
     │  ○   MERCANCÍA RECOGIDA                  │
     │  ○   SALIDA HACIA ENTREGA                │
     │  ○   LLEGADO A DESTINO                   │
     │  ○   COMPLETADO                          │
     │                                          │
     └──────────────────────────────────────────┘
```

**Ventaja:** Muy fácil y rápido de implementar. No requiere permisos de
ubicación. Muy visual y limpio.
**Desventaja:** No hay mapa real. Es más abstracto, parece menos una app
de transporte.

---

### Opción 3 — OSMDroid + Lottie combinados

La pantalla se divide en dos secciones:
- Parte superior: mapa OSMDroid con el camión moviéndose entre dos puntos
- Parte inferior: timeline de estados con Lottie de celebración al completar

Es la opción más completa e impresionante, pero requiere más tiempo de
desarrollo.

---

## Decisión recomendada

**Para el TFG, implementar Opción 1 (OSMDroid).**

Razones:
- Un mapa real con un camión moviéndose es exactamente lo que esperaría
  un profesor de DAM en una app de transporte de mercancías
- OpenStreetMap es gratuito y no requiere ninguna configuración de cuentas
- El movimiento del camión es una interpolación matemática simple, no GPS real
- Se pueden usar coordenadas reales de Madrid o la ciudad que se quiera
- Añade muy poco tiempo de desarrollo respecto a un simple timeline

Si el tiempo aprieta al llegar a esa fase, se puede hacer primero la
**Opción 2** (Lottie + timeline) en pocas horas y dejar el mapa para pulir.

---

## Trabajo a realizar cuando llegue el momento

El desarrollo se divide en dos bloques independientes. El backend debe
hacerse primero porque el Android depende del endpoint `/simular`.

### Bloque 1 — Backend (estimado: 1-2 horas)

- Crear `SimulacionService.java` con `@Async` y la secuencia de estados
  con delays configurables
- Habilitar `@EnableAsync` en la clase principal o en una clase de config
- Crear el endpoint `POST /api/viajes/{id}/simular` en `ViajeController`
- Decidir si añadir coordenadas de origen/destino al viaje (lat/lon) para
  que el frontend sepa dónde dibujar los puntos del mapa, o hardcodearlas
  directamente en el Android

### Bloque 2 — Android (estimado: 3-4 horas)

- Añadir dependencia OSMDroid en `build.gradle`
- Crear `MapaViajeActivity` con el mapa centrado en las coordenadas del viaje
- Añadir icono de camión personalizado como marcador en el mapa
- Implementar la interpolación de posición según el estado actual
  (usando `ValueAnimator` de Android para el movimiento suave)
- Implementar el Handler de polling: cada 3 segundos hace
  `GET /api/viajes/{id}`, compara el estado anterior con el nuevo
  y actualiza el mapa y el timeline
- Botón "Iniciar simulación" que llama a `POST /api/viajes/{id}/simular`
  y arranca el polling

---

## Flujo de uso en la demo del TFG

```
1. Abrir la app Android como CLIENTE
2. Hacer login
3. Crear un viaje nuevo (origen, destino, tipo de mercancía, peso)
4. Pulsar "Iniciar simulación"
   → La app llama a POST /api/viajes/{id}/simular
   → El backend empieza a avanzar los estados en segundo plano
   → La app empieza a hacer polling cada 3 segundos
5. En pantalla se ve el camión moviéndose en el mapa
   y el timeline actualizándose estado por estado
6. Tras ~38 segundos el viaje llega a COMPLETADO
   → Animación de celebración / icono de check en el mapa
```

---

## Tiempos de la simulación (configurables en el backend)

| Transición                               | Delay sugerido |
|------------------------------------------|----------------|
| Creado → ACEPTADO                        | 4 segundos     |
| ACEPTADO → SALIDA_RECOGIDA               | 5 segundos     |
| SALIDA_RECOGIDA → LLEGADA_RECOGIDA       | 4 segundos     |
| LLEGADA_RECOGIDA → MERCANCIA_RECOGIDA    | 5 segundos     |
| MERCANCIA_RECOGIDA → SALIDA_ENTREGA      | 6 segundos     |
| SALIDA_ENTREGA → LLEGADA_ENTREGA         | 4 segundos     |
| LLEGADA_ENTREGA → COMPLETADO             | 3 segundos     |
| **Total**                                | **~38 segundos** |

Los delays son configurables en `application.properties` para poder
ajustarlos sin recompilar:
```properties
simulacion.delay.aceptado=4000
simulacion.delay.salida-recogida=5000
# etc.
```

---

## Notas importantes para cuando se implemente

- El endpoint `/simular` debe estar **protegido por JWT** igual que el resto.
  Solo el transportista asignado al viaje debería poder iniciarlo.
- Si se llama a `/simular` sobre un viaje que no está en estado PENDIENTE
  o ACEPTADO, el backend debe rechazarlo con 409.
- En el Android, el polling debe **parar automáticamente** cuando el estado
  llega a COMPLETADO o CANCELADO para no hacer peticiones innecesarias.
- Las coordenadas de origen y destino para la simulación pueden ser
  hardcodeadas en el Android (ej: Plaza Mayor Madrid → Sagrada Familia
  Barcelona) ya que son ficticias de todas formas.

---

*Planteamiento acordado el 28/05/2026 — pendiente de implementar en fase Frontend*
