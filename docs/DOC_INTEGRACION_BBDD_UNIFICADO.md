# Documentación Técnica Unificada — Integración, Conexión y Base de Datos PisPax
### TFG DAM 2025/2026 — Adrián Salazar Nicolás
### Versión unificada — Junio 2026

---

## Índice

1. [Visión de la integración completa del sistema](#1-visión-de-la-integración-completa-del-sistema)
2. [Base de datos MySQL — Esquema completo](#2-base-de-datos-mysql--esquema-completo)
3. [Modelo Entidad-Relación y relaciones entre tablas](#3-modelo-entidad-relación-y-relaciones-entre-tablas)
4. [ENUMs de la base de datos](#4-enums-de-la-base-de-datos)
5. [Catálogo de mercancías y matriz de compatibilidad](#5-catálogo-de-mercancías-y-matriz-de-compatibilidad)
6. [Comunicación HTTP entre Frontend y Backend](#6-comunicación-http-entre-frontend-y-backend)
7. [JWT — Explicación técnica completa](#7-jwt--explicación-técnica-completa)
8. [Guía completa de pruebas con Postman](#8-guía-completa-de-pruebas-con-postman)
9. [Flujo de datos completo de extremo a extremo](#9-flujo-de-datos-completo-de-extremo-a-extremo)
10. [Checklist de arranque del sistema completo](#10-checklist-de-arranque-del-sistema-completo)

---

## 1. Visión de la integración completa del sistema

PisPax es un sistema de tres capas conectadas en serie:

```
┌─────────────────────────────────────────────────────────────────┐
│                   CAPA DE PRESENTACIÓN                           │
│                   App Android (Java 17)                          │
│                                                                  │
│  Activities (UI) ←→ RetrofitClient + OkHttp (HTTP)              │
│  SessionManager (SharedPreferences: JWT, userId, rol)            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │  HTTP/1.1 + JSON + JWT
                             │  Authorization: Bearer {token}
                             │
                             │  Emulador: http://10.0.2.2:8080/api/
                             │  Físico:   http://192.168.X.X:8080/api/
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                   CAPA DE LÓGICA DE NEGOCIO                      │
│                   Spring Boot 3.2.5 (Java 17)                    │
│                                                                  │
│  JwtAuthFilter → SecurityConfig → Controller                     │
│               → Service (lógica) → Repository                   │
│  Puerto: 8080                                                    │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │  JDBC (HikariCP pool)
                             │  JPA/Hibernate (ORM)
                             │  jdbc:mysql://localhost:3306/pispax
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                   CAPA DE PERSISTENCIA                           │
│                   MySQL 8.0 (localhost:3306)                     │
│                                                                  │
│  5 tablas: usuario, vehiculo, tipo_mercancia,                    │
│            vehiculo_tipo_mercancia, viaje                        │
│  3 ENUMs: rol, tipo_vehiculo, estado                             │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de una petición de extremo a extremo

Tomando como ejemplo la creación de un viaje por un cliente:

```
[App Android]
  1. Usuario rellena el formulario y pulsa "Solicitar viaje"
  2. CrearViajeActivity construye un CrearViajeRequest
  3. RetrofitClient.getApi().crearViaje(request).enqueue(...)
  4. Interceptor JWT añade: Authorization: Bearer eyJhbGci...
  5. OkHttp envía: POST http://10.0.2.2:8080/api/viajes
     Body: {"tipoMercanciaId":1,"direccionRecogida":"...","pesoKg":50.0}

[Red: HTTP sobre TCP]

[Spring Boot Backend]
  6. JwtAuthFilter intercepta la petición
  7. Verifica firma HMAC-SHA256 del token → válido
  8. Extrae userId=1, rol="CLIENTE" → inyecta en SecurityContext
  9. SecurityConfig verifica: /api/viajes requiere auth → OK
  10. @PreAuthorize("hasRole('CLIENTE')") en ViajeController → OK
  11. ViajeController.crearViaje() se ejecuta
  12. @Valid valida los campos del body → OK
  13. ViajeService.crearViaje(userId, request) aplica lógica:
       → Busca TipoMercancia con id=1 en BD
       → Crea entidad Viaje con estado PENDIENTE
  14. ViajeRepository.save(viaje) → Hibernate genera SQL:
       INSERT INTO viaje (cliente_id, tipo_mercancia_id, ...) VALUES (1, 1, ...)
  15. MySQL ejecuta el INSERT y devuelve el ID generado
  16. ViajeService construye ViajeDTO desde la entidad guardada
  17. ViajeController devuelve: 201 Created con ViajeDTO como JSON

[Red: HTTP sobre TCP]

[App Android]
  18. Gson convierte el JSON → ViajeDTO Java
  19. onResponse() se ejecuta en el hilo principal
  20. Toast "Viaje creado" + finish() → HomeCliente que refresca en onResume()
```

---

## 2. Base de datos MySQL — Esquema completo

La base de datos `pispax` tiene 5 tablas con integridad referencial completa mediante FOREIGN KEYS. Hibernate la genera automáticamente con `ddl-auto=update`, pero este es el esquema completo que resulta:

### Tabla `usuario`

```sql
CREATE TABLE usuario (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)   NOT NULL,
    apellidos     VARCHAR(150)   NOT NULL,
    email         VARCHAR(200)   NOT NULL UNIQUE,
    password_hash VARCHAR(255)   NOT NULL,
    telefono      VARCHAR(20),
    rol           ENUM('CLIENTE','TRANSPORTISTA') NOT NULL,
    created_at    DATETIME       DEFAULT NOW(),

    PRIMARY KEY (id),
    UNIQUE INDEX uq_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Decisiones de diseño**: tabla única para clientes y transportistas, diferenciados por el campo `rol`. Esto evita duplicidades (un usuario no puede ser cliente y transportista a la vez en el MVP) y simplifica el login (un único endpoint para todos los roles). La contraseña se guarda como hash BCrypt de 60 caracteres — nunca en texto plano.

### Tabla `vehiculo`

```sql
CREATE TABLE vehiculo (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    matricula          VARCHAR(15)   NOT NULL UNIQUE,
    marca              VARCHAR(80)   NOT NULL,
    modelo             VARCHAR(80)   NOT NULL,
    tipo_vehiculo      ENUM('FURGONETA','FURGON_GRANDE','FRIGORIFICO',
                            'CAMION_LIGERO','CAMION_PESADO',
                            'CAMION_ARTICULADO','PLATAFORMA')  NOT NULL,
    subtipo            VARCHAR(50),
    tara_kg            DECIMAL(8,2),
    capacidad_kg       DECIMAL(8,2)  NOT NULL,
    mma_kg             DECIMAL(8,2),
    carnet_requerido   ENUM('B','C1','C','C_E')  NOT NULL DEFAULT 'B',
    transportista_id   BIGINT        NOT NULL,

    PRIMARY KEY (id),
    UNIQUE INDEX uq_vehiculo_matricula (matricula),
    CONSTRAINT fk_vehiculo_transportista
        FOREIGN KEY (transportista_id) REFERENCES usuario(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Campo clave**: `capacidad_kg` es el campo que usa el sistema para validar que el viaje no supere la capacidad del vehículo al aceptarlo. La validación es: `viaje.pesoKg <= vehiculo.capacidadKg`.

Los campos `tara_kg` y `mma_kg` son opcionales pero permiten al sistema calcular la carga útil real (`mma_kg - tara_kg`) y son base para futuras validaciones más estrictas según normativa.

### Tabla `tipo_mercancia`

```sql
CREATE TABLE tipo_mercancia (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL UNIQUE,
    nombre_db    VARCHAR(100) NOT NULL UNIQUE,
    descripcion  VARCHAR(500),

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

Esta tabla es un catálogo fijo precargado por `data.sql`. El campo `nombre` es el texto que ve el usuario en la app. El campo `nombre_db` es el identificador técnico snake_case usado internamente por la API para filtros y lógica de compatibilidad, desacoplando la presentación del identificador interno.

### Tabla `vehiculo_tipo_mercancia`

```sql
CREATE TABLE vehiculo_tipo_mercancia (
    vehiculo_id      BIGINT NOT NULL,
    tipo_mercancia_id BIGINT NOT NULL,
    compatible       ENUM('SI','CON_REQUISITOS') NOT NULL DEFAULT 'SI',

    PRIMARY KEY (vehiculo_id, tipo_mercancia_id),
    CONSTRAINT fk_vtm_vehiculo
        FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vtm_mercancia
        FOREIGN KEY (tipo_mercancia_id) REFERENCES tipo_mercancia(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

Tabla de unión N:M. La clave primaria compuesta `(vehiculo_id, tipo_mercancia_id)` garantiza que no puede haber registros duplicados para la misma combinación.

El campo `compatible` tiene dos valores:
- `SI`: el vehículo puede transportar este tipo de mercancía sin condiciones adicionales
- `CON_REQUISITOS`: es compatible, pero requiere verificación adicional en Service (ADR, refrigeración GDP, etc.)

> **Nota del MVP**: Esta tabla existe en el esquema pero no se popula automáticamente al crear vehículos en la versión actual. La lógica de filtrado de `/viajes/disponibles` aún no usa esta compatibilidad. Es una de las limitaciones conocidas documentadas.

### Tabla `viaje`

```sql
CREATE TABLE viaje (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    cliente_id            BIGINT        NOT NULL,
    transportista_id      BIGINT,                       -- NULL hasta que se acepta
    vehiculo_id           BIGINT,                       -- NULL hasta que se acepta
    tipo_mercancia_id     BIGINT,
    descripcion_mercancia VARCHAR(500),
    direccion_recogida    VARCHAR(300)  NOT NULL,
    direccion_entrega     VARCHAR(300)  NOT NULL,
    peso_kg               DECIMAL(8,2) NOT NULL,
    estado                ENUM('PENDIENTE','ACEPTADO','SALIDA_RECOGIDA','LLEGADA_RECOGIDA',
                               'MERCANCIA_RECOGIDA','SALIDA_ENTREGA','LLEGADA_ENTREGA',
                               'COMPLETADO','CANCELADO')  NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud       DATETIME     DEFAULT NOW(),
    fecha_inicio          DATETIME,                     -- Se rellena al ACEPTAR
    fecha_fin             DATETIME,                     -- Se rellena al COMPLETAR/CANCELAR

    PRIMARY KEY (id),
    CONSTRAINT fk_viaje_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuario(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_viaje_transportista
        FOREIGN KEY (transportista_id) REFERENCES usuario(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_viaje_vehiculo
        FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_viaje_mercancia
        FOREIGN KEY (tipo_mercancia_id) REFERENCES tipo_mercancia(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Campos nullables**: `transportista_id` y `vehiculo_id` son NULL cuando el viaje se crea (estado PENDIENTE) y se rellenan cuando un transportista lo acepta. Los NULL son semánticamente correctos aquí — el viaje existe desde que el cliente lo solicita, aunque nadie lo haya aceptado todavía.

**Timestamps de auditoría**:
- `fecha_solicitud`: se genera automáticamente al crear el viaje. Permite saber cuándo se solicitó.
- `fecha_inicio`: se rellena cuando el transportista acepta (estado ACEPTADO). Junto con `fecha_solicitud`, permite calcular el tiempo de espera hasta aceptación.
- `fecha_fin`: se rellena al completar o cancelar. Permite calcular la duración total del servicio.

---

## 3. Modelo Entidad-Relación y relaciones entre tablas

```
usuario (id, nombre, apellidos, email, password_hash, telefono, rol)
    │
    │  1:N (un transportista tiene muchos vehículos)
    ├─────────────────────────────────────────────────────────────
    │                                                            ▼
    │                vehiculo (id, matricula, marca, modelo,
    │                          tipo_vehiculo, capacidad_kg,
    │                          carnet_requerido, transportista_id →)
    │                    │
    │                    │  N:M (vehículo puede transportar múltiples tipos)
    │                    │─────────────────────────────────────────────────
    │                    ▼                                               │
    │              vehiculo_tipo_mercancia                               │
    │              (vehiculo_id →, tipo_mercancia_id →, compatible)     │
    │                                    │                              │
    │                                    ▼                              │
    │                          tipo_mercancia                           │
    │                          (id, nombre, nombre_db, descripcion) ◄───┘
    │
    │  1:N (un cliente puede crear muchos viajes)
    └─────────────────────────────────────────────────────────────
                                                                 ▼
                    viaje (id, cliente_id →, transportista_id →,
                           vehiculo_id →, tipo_mercancia_id →,
                           peso_kg, estado, fechas...)
```

### Descripción de las relaciones

**usuario → vehiculo (1:N)**: Un transportista puede tener registrados múltiples vehículos en la plataforma. Cada vehículo pertenece a un único transportista (la FK `transportista_id` no puede ser nula).

**vehiculo ↔ tipo_mercancia (N:M)**: Un vehículo puede transportar múltiples tipos de mercancía (un camión frigorífico transporta alimentos frescos y congelados). Un tipo de mercancía puede ser transportado por múltiples vehículos. La tabla intermedia `vehiculo_tipo_mercancia` resuelve esta relación con el campo adicional `compatible`.

**usuario → viaje como cliente (1:N)**: Un cliente puede solicitar múltiples viajes. Cada viaje tiene exactamente un cliente solicitante.

**usuario → viaje como transportista (0:N)**: Un transportista puede aceptar múltiples viajes. Un viaje puede no tener transportista asignado (cuando está PENDIENTE) o tener exactamente uno.

**vehiculo → viaje (0:N)**: Un vehículo puede usarse en múltiples viajes. Un viaje puede no tener vehículo asignado (cuando está PENDIENTE) o tener exactamente uno.

**tipo_mercancia → viaje (1:N)**: Cada viaje especifica exactamente un tipo de mercancía del catálogo.

---

## 4. ENUMs de la base de datos

Los ENUMs se definen como tipos ENUM nativos de MySQL, lo que garantiza integridad a nivel de motor de base de datos, sin depender exclusivamente de la validación en la capa de aplicación.

### ENUM `rol` (tabla `usuario`)

| Valor | Descripción |
|---|---|
| `CLIENTE` | Usuario que crea solicitudes de transporte y hace seguimiento |
| `TRANSPORTISTA` | Usuario que acepta viajes, los ejecuta y actualiza el estado |

### ENUM `tipo_vehiculo` (tabla `vehiculo`)

| Valor | Descripción real | MMA máx. | Carnet mínimo |
|---|---|---|---|
| `FURGONETA` | Vehículo de reparto compacto/mediano | 3.500 kg | B |
| `FURGON_GRANDE` | Furgón de alto volumen (mobiliario, paquetería) | 3.500 kg | B |
| `FRIGORIFICO` | Cámara de temperatura controlada (0°C a -18°C) | 3.500 kg | B |
| `CAMION_LIGERO` | Camión rígido pequeño, distribución regional | 3.500–7.500 kg | C1 |
| `CAMION_PESADO` | Camión rígido de gran tonelaje (requiere CAP) | > 7.500 kg | C + CAP |
| `CAMION_ARTICULADO` | Semirremolque de larga distancia (requiere CAP) | Hasta 42.000 kg | C+E + CAP |
| `PLATAFORMA` | Para cargas voluminosas, palets, maquinaria | Variable | C1/C |

### ENUM `carnet_requerido` (tabla `vehiculo`)

| Valor BD | Texto UI | Rango MMA | Descripción |
|---|---|---|---|
| `B` | B | Hasta 3.500 kg | Carnet estándar. La mayoría de furgonetas |
| `C1` | C1 | 3.500–7.500 kg | Requiere formación adicional |
| `C` | C | > 7.500 kg | Requiere CAP (Certificado de Aptitud Profesional) |
| `C_E` | C+E | Articulado remolque > 750 kg | Requiere haber obtenido C previamente + CAP |

### ENUM `estado` (tabla `viaje`)

| Valor | Descripción | Campos que cambian |
|---|---|---|
| `PENDIENTE` | Viaje creado por cliente, sin transportista asignado | — (estado inicial) |
| `ACEPTADO` | Transportista ha aceptado, vehículo asignado | `transportista_id`, `vehiculo_id`, `fecha_inicio` |
| `SALIDA_RECOGIDA` | Transportista sale hacia el punto de recogida | — |
| `LLEGADA_RECOGIDA` | Transportista llega al punto de recogida | — |
| `MERCANCIA_RECOGIDA` | Mercancía cargada en el vehículo | — |
| `SALIDA_ENTREGA` | Transportista sale hacia el destino de entrega | — |
| `LLEGADA_ENTREGA` | Transportista llega al punto de entrega | — |
| `COMPLETADO` | Entrega confirmada, viaje finalizado exitosamente | `fecha_fin` |
| `CANCELADO` | Viaje cancelado desde cualquier estado previo | `fecha_fin` |

### ENUM `compatible` (tabla `vehiculo_tipo_mercancia`)

| Valor | Descripción |
|---|---|
| `SI` | Compatible sin condiciones — el vehículo puede transportar este tipo de mercancía |
| `CON_REQUISITOS` | Compatible condicionalmente — requiere verificación adicional (ADR, frío, GDP...) |

---

## 5. Catálogo de mercancías y matriz de compatibilidad

### Catálogo de 12 tipos de mercancía (precargado por `data.sql`)

| ID | Nombre visible | nombre_db | Descripción | Requisitos especiales |
|---|---|---|---|---|
| 1 | Paquetería general | `paqueteria_general` | Paquetes, cajas, envíos estándar | Ninguno |
| 2 | Electrónica frágil | `electronica_fragil` | Dispositivos frágiles, equipos electrónicos | Amortiguación, protección |
| 3 | Alimentos frescos | `alimentos_frescos` | Productos perecederos (0–8°C) | Cadena de frío |
| 4 | Alimentos congelados | `alimentos_congelados` | Productos congelados (< -18°C) | Cadena de frío estricta |
| 5 | Materiales de construcción | `materiales_construccion` | Cemento, acero, materiales pesados | Ninguno |
| 6 | Mobiliario | `mobiliario` | Muebles, sofás, piezas grandes | Manipulación cuidadosa |
| 7 | Mercancía peligrosa (ADR) | `mercancia_peligrosa` | Sustancias químicas, inflamables, tóxicas | **Certificado ADR vigente** |
| 8 | Productos farmacéuticos | `productos_farmaceuticos` | Medicinas, biologics | **GDP** (recomendado) |
| 9 | Textil y moda | `textil_moda` | Ropa, telas, calzado | Ninguno |
| 10 | Maquinaria industrial | `maquinaria_industrial` | Máquinas, equipos pesados | Aseguramiento especial |
| 11 | Documentación y archivo | `documentacion_archivo` | Documentos, libros, archivos | Ninguno |
| 12 | Vehículos y automoción | `vehiculos_automocion` | Coches, motos, piezas vehiculares | Homologación transporte |

### Matriz de compatibilidad vehículo × mercancía

Esta matriz define qué tipos de mercancía puede transportar cada categoría de vehículo. Es la base de la lógica del endpoint `GET /api/viajes/disponibles` (pendiente de implementar en el MVP).

✅ Compatible | ⚠️ Compatible con requisitos adicionales | ❌ No compatible

| Mercancía ↓ / Vehículo → | Furgoneta | Furgón Grande | Frigorífico | Camión Ligero C1 | Camión Pesado C | Camión Articulado C+E | Plataforma |
|---|---|---|---|---|---|---|---|
| Paquetería general | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Electrónica frágil | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Alimentos frescos | ❌ | ❌ | ✅ | ⚠️ | ⚠️ | ⚠️ | ❌ |
| Alimentos congelados | ❌ | ❌ | ✅ | ⚠️ | ⚠️ | ⚠️ | ❌ |
| Materiales construcción | ❌ | ⚠️ | ❌ | ✅ | ✅ | ✅ | ✅ |
| Mobiliario | ❌ | ✅ | ❌ | ✅ | ✅ | ✅ | ⚠️ |
| Mercancía peligrosa (ADR) | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ❌ |
| Productos farmacéuticos | ❌ | ⚠️ GDP | ✅ | ⚠️ GDP | ⚠️ GDP | ⚠️ GDP | ❌ |
| Textil y moda | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Maquinaria industrial | ❌ | ❌ | ❌ | ⚠️ Seguro | ✅ | ✅ | ✅ |
| Documentación y archivo | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Vehículos y automoción | ❌ | ❌ | ❌ | ❌ | ⚠️ | ⚠️ | ✅ |

**Leyenda de requisitos adicionales**:
- ⚠️ ADR: Conductor con Certificado ADR vigente (transporte de mercancías peligrosas)
- ⚠️ GDP: Good Distribution Practice — farmacéuticos; recomendado pero no obligatorio en MVP
- ⚠️ Seguro: Póliza de seguro para mercancía de alto valor
- ⚠️ Frío: Módulo frigorífico homologado (para alimentos en vehículos no frigoríficos)

### Consulta SQL base para viajes disponibles con compatibilidad

Esta consulta (a implementar en el MVP final) filtraría los viajes disponibles para un transportista según la compatibilidad de sus vehículos:

```sql
SELECT v.* FROM viaje v
WHERE v.estado = 'PENDIENTE'
  AND v.tipo_mercancia_id IN (
    SELECT vtm.tipo_mercancia_id
    FROM vehiculo_tipo_mercancia vtm
    JOIN vehiculo veh ON vtm.vehiculo_id = veh.id
    WHERE veh.transportista_id = :transportistaId
      AND vtm.compatible IN ('SI', 'CON_REQUISITOS')
  );
```

---

## 6. Comunicación HTTP entre Frontend y Backend

### Protocolo de comunicación

El sistema usa **HTTP/1.1** estándar con **JSON** como formato de intercambio de datos. En desarrollo, la comunicación es en claro (sin TLS). En producción se requeriría HTTPS.

**Cabeceras estándar en peticiones autenticadas**:
```
POST http://10.0.2.2:8080/api/viajes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi...
Content-Type: application/json

{"tipoMercanciaId":1,"direccionRecogida":"...","pesoKg":50.0}
```

**Cabeceras en respuestas del servidor**:
```
HTTP/1.1 201 Created
Content-Type: application/json
Transfer-Encoding: chunked

{"id":15,"clienteId":1,"estado":"PENDIENTE",...}
```

### URLs de conexión según el entorno

| Entorno | URL base | Explicación |
|---|---|---|
| Emulador Android Studio | `http://10.0.2.2:8080/api/` | `10.0.2.2` es la IP especial que el emulador usa para alcanzar `localhost` del PC host |
| Dispositivo físico (misma WiFi) | `http://192.168.X.X:8080/api/` | La IP local del PC en la red WiFi (obtener con `ipconfig` en Windows) |
| Postman en el mismo PC | `http://localhost:8080/api/` | Postman corre en el mismo PC que el backend |

> Para cambiar entre emulador y dispositivo físico, modificar `Constants.BASE_URL` en el proyecto Android y limpiar la instancia singleton de Retrofit (`RetrofitClient.reset()`).

### Códigos HTTP usados en la API

| Código | Nombre | Cuándo el backend lo devuelve |
|---|---|---|
| 200 | OK | Petición exitosa (GET, PUT con respuesta) |
| 201 | Created | Recurso creado exitosamente (POST de registro, viaje, vehículo) |
| 400 | Bad Request | Validación fallida, email duplicado, credenciales incorrectas, peso excede capacidad |
| 401 | Unauthorized | Sin token, token inválido, token caducado |
| 403 | Forbidden | Token válido pero rol sin permisos para el endpoint, o acceso a recurso de otro usuario |
| 404 | Not Found | El recurso solicitado (viaje, usuario, vehículo) no existe en la BD |
| 409 | Conflict | Transición de estado inválida, matrícula duplicada, intentar aceptar viaje no PENDIENTE |
| 500 | Internal Server Error | Error inesperado del servidor (se loguea en consola) |

### Formato de error estándar

Cualquier error del backend (4xx o 5xx) devuelve siempre el mismo formato JSON:

```json
{
  "mensaje": "Descripción legible del error para el usuario",
  "codigo": 400
}
```

El Android puede leer `response.errorBody()` y parsear el `mensaje` para mostrarlo en un `Toast` o `AlertDialog`.

---

## 7. JWT — Explicación técnica completa

### El problema que JWT resuelve

Las aplicaciones web tradicionales usan **sesiones en servidor**: al hacer login, el servidor crea una sesión en memoria/BD y devuelve una cookie con el ID. En cada petición siguiente, el servidor busca esa sesión. Con miles de usuarios simultáneos y múltiples instancias del servidor, las sesiones deben sincronizarse entre instancias, lo que es complejo y costoso.

```
SISTEMA CON SESIONES (stateful):
  Cliente → login → servidor genera sesión y guarda en BD → devuelve cookie
  Cliente → petición con cookie → servidor busca sesión en BD → procesa

SISTEMA CON JWT (stateless):
  Cliente → login → servidor genera JWT firmado (sin guardar nada) → devuelve JWT
  Cliente → petición con JWT → servidor verifica firma del JWT (sin BD) → procesa
```

### Anatomía del token JWT

Un token JWT es una cadena de tres partes separadas por puntos:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSIsInVzZXJJZCI6MSwicm9sIjoiQ0xJRU5URSIsImlhdCI6MTc0ODQ1Njc4OSwiZXhwIjoxNzQ4NTQzMTg5fQ.XyZ_firma_aqui
│──────────────────│.│────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────│.│──────────────────│
      HEADER (B64)                                              PAYLOAD (B64)                                                                                                          SIGNATURE (B64url)
```

**HEADER** — decodificado del Base64:
```json
{ "alg": "HS256" }
```
Indica que la firma usa el algoritmo HMAC-SHA256.

**PAYLOAD** — decodificado del Base64 (en PisPax):
```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

| Campo | Nombre completo | Valor en PisPax |
|---|---|---|
| `sub` | Subject (estándar JWT) | Email del usuario |
| `userId` | Custom claim | ID del usuario en MySQL — lo leen los controllers |
| `rol` | Custom claim | `CLIENTE` o `TRANSPORTISTA` — controla el acceso |
| `iat` | Issued at (estándar) | Timestamp Unix del momento de emisión |
| `exp` | Expiration (estándar) | Timestamp Unix de caducidad (iat + 86400s = iat + 24h) |

> El payload está en **Base64** (no cifrado). Es visible para cualquiera con el token. Por eso NUNCA va información sensible como contraseñas o datos bancarios. JWT garantiza **integridad** (nadie modificó el contenido), no confidencialidad.

**SIGNATURE**:
```
HMAC-SHA256(base64url(HEADER) + "." + base64url(PAYLOAD), clave_secreta)
```
La firma se genera con la clave secreta del servidor (`jwt.secret` en `application.properties`). Sin conocer la clave secreta, es computacionalmente imposible generar una firma válida.

### SHA-256 y HMAC explicados

**SHA-256** es una función hash criptográfica con estas propiedades:
- **Determinista**: la misma entrada siempre produce la misma salida
- **Irreversible**: no se puede recuperar la entrada a partir de la salida
- **Efecto avalancha**: un bit diferente en la entrada cambia completamente la salida
- **Sin colisiones prácticas**: dos entradas distintas no producen la misma salida

```
SHA256("hola")  = "b94d27b9934d3e08a52e52d7da7dabfac484efe04294e576..."
SHA256("holA")  = "05e92e8a0e1f73b56c3b79e57c18fa2ee8e16d8ab23c76..."
                   ↑ Completamente diferente con solo cambiar una letra
```

**HMAC** (Hash-based Message Authentication Code) añade una **clave secreta** al proceso:
```
HMAC-SHA256(mensaje, clave_secreta) = firma
```
Solo quien conoce la `clave_secreta` puede generar o verificar la firma.

### ¿Qué pasa si alguien intenta falsificar un token?

Supón que un atacante intercepta el token y cambia `"rol":"CLIENTE"` a `"rol":"TRANSPORTISTA"` en el payload:

```
Token original:
eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJDTElFTlRFIn0.FIRMA_VALIDA

Token manipulado (payload cambiado):
eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJUUkFOU1BPUlRJU1RBIn0.FIRMA_VALIDA
                                                             ↑ La firma ya no corresponde al nuevo payload
```

El servidor recalcula `HMAC-SHA256(header + "." + payload_modificado, clave_secreta)` y el resultado **no coincide** con la firma original. El token se rechaza con 401/403. Sin la clave secreta del servidor, el atacante no puede recalcular una firma válida para el payload modificado.

### Ciclo de vida del token

```
T=0s           T=1s              T=86400s (24h)    T=86401s
 │                │                   │                │
 │ login          │ peticiones        │ token caduca   │ token inválido
 │ → token JWT    │ con token         │ (exp < ahora)  │ → 401/403
 │                │                   │                │ → nuevo login
```

Cuando `exp < ahora`, `JwtUtil.isTokenValid()` devuelve `false`, el filtro no inyecta la autenticación y Spring Security responde 403.

**No hay refresh token** en el MVP: cuando el token caduca, el usuario debe hacer login de nuevo. Para producción se debería implementar un sistema de refresh.

### BCrypt — Hashing de contraseñas

```
Contraseña en texto: "miPassword123"
Hash BCrypt (60 chars): "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKr.VJsgG"
```

BCrypt incluye automáticamente una "sal" aleatoria en cada hash, por lo que el mismo password siempre genera un hash diferente. Esto hace imposible los ataques por tabla rainbow. La verificación es `BCrypt.matches(rawPassword, storedHash)`.

---

## 8. Guía completa de pruebas con Postman

### Configuración inicial

#### Crear el entorno de variables

1. En Postman: botón de entornos (arriba a la derecha) → "Add"
2. Nombre: **PisPax Local**
3. Variables:

| Variable | Valor inicial | Descripción |
|---|---|---|
| `base_url` | `http://localhost:8080` | URL base del backend |
| `token_cliente` | *(vacío)* | Se rellena automáticamente al hacer login |
| `token_transportista` | *(vacío)* | Se rellena automáticamente al hacer login |
| `viaje_id` | *(vacío)* | ID del viaje creado |
| `vehiculo_id` | *(vacío)* | ID del vehículo creado |

4. Seleccionar el entorno en el desplegable de la esquina superior derecha

#### Crear la colección

Crear una colección llamada **PisPax API** con las siguientes carpetas:
- `Auth` (registro y login)
- `Catálogo` (tipos de mercancía)
- `Vehículos` (CRUD de vehículos)
- `Viajes` (todo el ciclo de un viaje)

---

### Endpoint 1 — Registro de cliente

```
POST {{base_url}}/api/auth/registro
Content-Type: application/json

{
    "nombre": "María",
    "apellidos": "García López",
    "email": "maria.cliente@pispax.com",
    "password": "Password123",
    "telefono": "600111222",
    "rol": "CLIENTE"
}
```

**Respuesta esperada — 201 Created:**
```json
{
    "id": 1,
    "nombre": "María",
    "apellidos": "García López",
    "email": "maria.cliente@pispax.com",
    "telefono": "600111222",
    "rol": "CLIENTE"
}
```
Observar que `password` NO aparece en la respuesta.

---

### Endpoint 2 — Registro de transportista

```
POST {{base_url}}/api/auth/registro
Content-Type: application/json

{
    "nombre": "Carlos",
    "apellidos": "Martínez Ruiz",
    "email": "carlos.transportista@pispax.com",
    "password": "Transport456",
    "telefono": "677888999",
    "rol": "TRANSPORTISTA"
}
```

---

### Endpoint 3 — Login (el más importante)

#### Login como CLIENTE

```
POST {{base_url}}/api/auth/login
Content-Type: application/json

{
    "email": "maria.cliente@pispax.com",
    "password": "Password123"
}
```

**Script en la pestaña "Tests" para guardar el token automáticamente:**
```javascript
if (pm.response.code === 200) {
    pm.environment.set("token_cliente", pm.response.json().token);
    console.log("Token cliente guardado: " + pm.response.json().token.substring(0, 30) + "...");
}
```

**Respuesta esperada — 200 OK:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJpYS5jbGllbnRlQHBpc3BheC5jb20iLCJ1c2VySWQiOjEsInJvbCI6IkNMSUVOVEUiLCJpYXQiOjE3NDg0NTY3ODksImV4cCI6MTc0ODU0MzE4OX0.firma",
    "usuario": {
        "id": 1,
        "nombre": "María",
        "apellidos": "García López",
        "email": "maria.cliente@pispax.com",
        "telefono": "600111222",
        "rol": "CLIENTE"
    }
}
```

#### Login como TRANSPORTISTA

Igual, con el body de Carlos. Script de Tests:
```javascript
if (pm.response.code === 200) {
    pm.environment.set("token_transportista", pm.response.json().token);
}
```

#### Cómo usar el token en Postman

En la pestaña **Authorization** de cada petición protegida:
- Type: **Bearer Token**
- Token: `{{token_cliente}}` o `{{token_transportista}}`

O en la pestaña **Headers**:
```
Key: Authorization
Value: Bearer {{token_cliente}}
```

#### Decodificar el token para inspeccionarlo

Copia el token y pégalo en **https://jwt.io**. Verás el payload decodificado:
```json
{
  "sub": "maria.cliente@pispax.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

Para convertir los timestamps a fecha legible (en la consola del navegador):
```javascript
new Date(1748456789 * 1000).toLocaleString()
// → "28/5/2026, 20:46:29"
```

---

### Endpoint 4 — Catálogo de tipos de mercancía (público)

```
GET {{base_url}}/api/tipo-mercancia
(sin token)
```

---

### Endpoint 5 — Registrar vehículo

```
POST {{base_url}}/api/vehiculos
Authorization: Bearer {{token_transportista}}
Content-Type: application/json

{
    "matricula": "1234ABC",
    "marca": "Mercedes",
    "modelo": "Sprinter 316",
    "tipoVehiculo": "FURGONETA",
    "subtipo": "Refrigerada",
    "taraKg": 2100.00,
    "capacidadKg": 1500.00,
    "mmaKg": 3500.00,
    "carnetRequerido": "B"
}
```

**Script de Tests para guardar el ID:**
```javascript
if (pm.response.code === 201) {
    pm.environment.set("vehiculo_id", pm.response.json().id);
}
```

**Valores válidos para los campos ENUM:**
- `tipoVehiculo`: `FURGONETA`, `FURGON_GRANDE`, `FRIGORIFICO`, `CAMION_LIGERO`, `CAMION_PESADO`, `CAMION_ARTICULADO`, `PLATAFORMA`
- `carnetRequerido`: `B`, `C1`, `C`, `C_E` (nota: `C_E`, no `C+E`)

---

### Endpoint 6 — Mis vehículos

```
GET {{base_url}}/api/vehiculos/mis-vehiculos
Authorization: Bearer {{token_transportista}}
```

---

### Endpoint 7 — Crear viaje

```
POST {{base_url}}/api/viajes
Authorization: Bearer {{token_cliente}}
Content-Type: application/json

{
    "tipoMercanciaId": 1,
    "descripcionMercancia": "Caja grande con ropa de temporada",
    "direccionRecogida": "Polígono Industrial Can Fontanet, Terrassa, Barcelona",
    "direccionEntrega": "Centro Logístico Mercamadrid, Madrid",
    "pesoKg": 480
}
```

**Script de Tests para guardar el ID del viaje:**
```javascript
if (pm.response.code === 201) {
    pm.environment.set("viaje_id", pm.response.json().id);
}
```

---

### Endpoint 8 — Ver mis viajes

```
GET {{base_url}}/api/viajes
Authorization: Bearer {{token_cliente}}
(o Bearer {{token_transportista}})
```

---

### Endpoint 9 — Ver detalle de un viaje

```
GET {{base_url}}/api/viajes/{{viaje_id}}
Authorization: Bearer {{token_cliente}}
```

---

### Endpoint 10 — Ver viajes disponibles (solo transportista)

```
GET {{base_url}}/api/viajes/disponibles
Authorization: Bearer {{token_transportista}}
```

---

### Endpoint 11 — Aceptar un viaje

```
PUT {{base_url}}/api/viajes/{{viaje_id}}/aceptar
Authorization: Bearer {{token_transportista}}
Content-Type: application/json

{
    "vehiculoId": {{vehiculo_id}}
}
```

**Respuesta esperada**: `estado: "ACEPTADO"`, `fechaInicio` rellenada, `transportistaId` y `vehiculoId` asignados.

---

### Endpoint 12 — Actualizar estados (secuencia completa)

```
PUT {{base_url}}/api/viajes/{{viaje_id}}/estado
Authorization: Bearer {{token_transportista}}
Content-Type: application/json
```

Hacer esta llamada **6 veces** con estos bodies en orden:

```json
{ "nuevoEstado": "SALIDA_RECOGIDA" }
```
```json
{ "nuevoEstado": "LLEGADA_RECOGIDA" }
```
```json
{ "nuevoEstado": "MERCANCIA_RECOGIDA" }
```
```json
{ "nuevoEstado": "SALIDA_ENTREGA" }
```
```json
{ "nuevoEstado": "LLEGADA_ENTREGA" }
```
```json
{ "nuevoEstado": "COMPLETADO" }
```

La última llamada devuelve `estado: "COMPLETADO"` y `fechaFin` rellenada.

**Cancelar** (desde cualquier estado excepto COMPLETADO/CANCELADO):
```json
{ "nuevoEstado": "CANCELADO" }
```

---

### Pruebas de error — escenarios negativos

Estas pruebas demuestran que el sistema rechaza correctamente las peticiones inválidas y son igual de importantes para el TFG.

#### Errores de autenticación

**A. Sin token:**
```
GET {{base_url}}/api/viajes    (sin Authorization)
→ 403 Forbidden
```

**B. Token manipulado:**
```
GET {{base_url}}/api/viajes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.PAYLOAD_MODIFICADO.firma_incorrecta
→ 403 Forbidden
```

#### Errores de autorización por rol

**C. Cliente intenta ver viajes disponibles:**
```
GET {{base_url}}/api/viajes/disponibles
Authorization: Bearer {{token_cliente}}
→ 403 Forbidden
```

**D. Cliente intenta crear un vehículo:**
```
POST {{base_url}}/api/vehiculos
Authorization: Bearer {{token_cliente}}
→ 403 Forbidden
```

**E. Transportista intenta crear un viaje:**
```
POST {{base_url}}/api/viajes
Authorization: Bearer {{token_transportista}}
→ 403 Forbidden
```

#### Errores de validación

**F. Email duplicado:**
```
POST {{base_url}}/api/auth/registro  (mismo email de María por segunda vez)
→ 400 Bad Request: {"mensaje":"Ya existe un usuario con ese email","codigo":400}
```

**G. Contraseña muy corta:**
```
POST {{base_url}}/api/auth/registro  con "password":"abc" (solo 3 chars)
→ 400 Bad Request: {"mensaje":"password: La contraseña debe tener al menos 8 caracteres","codigo":400}
```

**H. Peso negativo:**
```
POST {{base_url}}/api/viajes  con "pesoKg": -10
→ 400 Bad Request: {"mensaje":"pesoKg: El peso debe ser mayor que 0","codigo":400}
```

#### Errores de lógica de negocio

**I. Aceptar un viaje ya ACEPTADO:**
```
PUT {{base_url}}/api/viajes/{{viaje_id}}/aceptar
→ 409 Conflict: {"mensaje":"Solo se pueden aceptar viajes en estado PENDIENTE","codigo":409}
```

**J. Saltar un estado:**
```
PUT {{base_url}}/api/viajes/{{viaje_id}}/estado
{ "nuevoEstado": "COMPLETADO" }   ← cuando el estado actual es ACEPTADO
→ 409 Conflict: {"mensaje":"Transición inválida: ACEPTADO → COMPLETADO","codigo":409}
```

**K. Peso excede capacidad del vehículo:**
```
PUT {{base_url}}/api/viajes/{{viaje_id}}/aceptar
{ "vehiculoId": 1 }   ← vehículo con 1500kg pero el viaje pesa 2000kg
→ 400 Bad Request: {"mensaje":"El peso del viaje excede la capacidad del vehículo","codigo":400}
```

**L. Vehículo que no es tuyo:**
```
PUT {{base_url}}/api/viajes/{{viaje_id}}/aceptar
{ "vehiculoId": 99 }   ← vehículo de otro transportista
→ 400 Bad Request: {"mensaje":"El vehículo no pertenece a este transportista","codigo":400}
```

**M. Cancelar un viaje ya completado:**
```
PUT {{base_url}}/api/viajes/{{viaje_id}}/estado
{ "nuevoEstado": "CANCELADO" }   ← cuando el viaje ya está COMPLETADO
→ 409 Conflict: {"mensaje":"No se puede cancelar un viaje ya finalizado","codigo":409}
```

---

## 9. Flujo de datos completo de extremo a extremo

Este es el guión completo para la demostración de todos los endpoints en la presentación del TFG:

```
═══════════════════════════════════════════════════
BLOQUE 1 — SETUP (sin token necesario)
═══════════════════════════════════════════════════

1. GET  /api/tipo-mercancia
   → 200 OK: lista de 12 tipos de mercancía

2. POST /api/auth/registro (CLIENTE)
   → 201 Created: usuario id=1

3. POST /api/auth/registro (TRANSPORTISTA)
   → 201 Created: usuario id=2

═══════════════════════════════════════════════════
BLOQUE 2 — AUTENTICACIÓN (obtener tokens JWT)
═══════════════════════════════════════════════════

4. POST /api/auth/login (CLIENTE: maria.cliente@pispax.com)
   → 200 OK: token guardado en {{token_cliente}}

5. POST /api/auth/login (TRANSPORTISTA: carlos.transportista@pispax.com)
   → 200 OK: token guardado en {{token_transportista}}

═══════════════════════════════════════════════════
BLOQUE 3 — TRANSPORTISTA prepara su vehículo
═══════════════════════════════════════════════════

6. POST /api/vehiculos (token_transportista)
   → 201 Created: vehículo id=1 guardado en {{vehiculo_id}}

7. GET /api/vehiculos/mis-vehiculos (token_transportista)
   → 200 OK: lista con el vehículo recién creado

═══════════════════════════════════════════════════
BLOQUE 4 — CLIENTE crea una solicitud de viaje
═══════════════════════════════════════════════════

8. POST /api/viajes (token_cliente)
   → 201 Created: viaje id=1 en estado PENDIENTE
     viaje_id guardado en {{viaje_id}}

9. GET /api/viajes (token_cliente)
   → 200 OK: lista con el viaje PENDIENTE

═══════════════════════════════════════════════════
BLOQUE 5 — TRANSPORTISTA acepta y ejecuta el viaje
═══════════════════════════════════════════════════

10. GET /api/viajes/disponibles (token_transportista)
    → 200 OK: aparece el viaje de María en PENDIENTE

11. GET /api/viajes/{{viaje_id}} (token_transportista)
    → 200 OK: detalle completo del viaje

12. PUT /api/viajes/{{viaje_id}}/aceptar (token_transportista)
    Body: { "vehiculoId": {{vehiculo_id}} }
    → 200 OK: estado="ACEPTADO", fechaInicio≠null, transportistaId=2

13. PUT /api/viajes/{{viaje_id}}/estado { "nuevoEstado": "SALIDA_RECOGIDA" }
    → 200 OK: estado="SALIDA_RECOGIDA"

14. PUT ... { "nuevoEstado": "LLEGADA_RECOGIDA" }
    → 200 OK

15. PUT ... { "nuevoEstado": "MERCANCIA_RECOGIDA" }
    → 200 OK

16. PUT ... { "nuevoEstado": "SALIDA_ENTREGA" }
    → 200 OK

17. PUT ... { "nuevoEstado": "LLEGADA_ENTREGA" }
    → 200 OK

18. PUT ... { "nuevoEstado": "COMPLETADO" }
    → 200 OK: estado="COMPLETADO", fechaFin≠null

═══════════════════════════════════════════════════
BLOQUE 6 — Verificación final
═══════════════════════════════════════════════════

19. GET /api/viajes/{{viaje_id}} (token_cliente)
    → 200 OK: estado="COMPLETADO", fechaInicio y fechaFin rellenadas

20. GET /api/viajes (token_cliente)
    → 200 OK: el viaje aparece como COMPLETADO

21. GET /api/viajes (token_transportista)
    → 200 OK: el viaje aparece como COMPLETADO (desde el rol transportista)
```

---

## 10. Checklist de arranque del sistema completo

Para arrancar el sistema completo (backend + base de datos) listo para ser usado por el frontend Android:

### Paso 1 — Arrancar MySQL

```powershell
# PowerShell como Administrador
Start-Service MYSQL80

# Verificar
(Get-Service MYSQL80).Status   # → Running
```

### Paso 2 — Crear la base de datos (solo la primera vez)

```powershell
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS pispax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
# Password: nairda04
```

### Paso 3 — Arrancar el backend

```powershell
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
mvn spring-boot:run
```

### Paso 4 — Verificar el arranque

Esperar estos mensajes en el log:
```
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http)
Started PispaxApplication in X.XX seconds
```

### Paso 5 — Verificar con petición de prueba

```powershell
# Debe devolver 200 con los 12 tipos de mercancía
Invoke-RestMethod -Uri "http://localhost:8080/api/tipo-mercancia" -Method GET

# Debe devolver 403 (correcto — Spring Security activo)
Invoke-RestMethod -Uri "http://localhost:8080/api/viajes" -Method GET
```

### Paso 6 — Arrancar el frontend Android

**Si usas emulador:**
- La `BASE_URL` ya está configurada como `http://10.0.2.2:8080/api/`
- Lanzar emulador desde Android Studio → Run

**Si usas dispositivo físico:**
- Cambiar `Constants.BASE_URL` a `http://192.168.X.X:8080/api/` (tu IP WiFi)
- Conectar el dispositivo por USB con depuración USB activada
- Run desde Android Studio

### Credenciales de la demo precargada

| Rol | Email | Password |
|---|---|---|
| CLIENTE | `demo.cliente@pispax.com` | `Demo1234` |
| TRANSPORTISTA | `demo.transportista@pispax.com` | `Demo1234` |

Vehículo del transportista demo: Renault Master, matrícula `1234ABC`, capacidad 1500 kg, carnet B.

### Parar el sistema

```powershell
# Backend: Ctrl+C en la terminal donde está corriendo
# MySQL (si quieres pararlo):
Stop-Service MYSQL80
```

---

*Documentación unificada generada el 08/06/2026 — TFG PisPax — Adrián Salazar Nicolás*
