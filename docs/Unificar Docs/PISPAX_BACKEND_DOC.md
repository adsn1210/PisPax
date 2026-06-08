# PisPax Backend — Documentación Técnica Completa

## Índice
1. [Resumen del proyecto](#1-resumen-del-proyecto)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura y estructura de paquetes](#3-arquitectura-y-estructura-de-paquetes)
4. [Modelo de datos](#4-modelo-de-datos)
5. [Seguridad y autenticación JWT](#5-seguridad-y-autenticación-jwt)
6. [API Reference — Endpoints](#6-api-reference--endpoints)
7. [Máquina de estados del viaje](#7-máquina-de-estados-del-viaje)
8. [Flujo de presentación (Demo)](#8-flujo-de-presentación-demo)
9. [Configuración y arranque](#9-configuración-y-arranque)
10. [Datos demo precargados](#10-datos-demo-precargados)
11. [Guía de integración Android](#11-guía-de-integración-android)
12. [Errores y códigos HTTP](#12-errores-y-códigos-http)
13. [Limitaciones conocidas (MVP)](#13-limitaciones-conocidas-mvp)
14. [Resultados de la test suite](#14-resultados-de-la-test-suite)

---

## 1. Resumen del proyecto

**PisPax** es una plataforma de gestión de transporte de mercancías que conecta:
- **Clientes** — empresas o particulares que necesitan transportar mercancía.
- **Transportistas** — profesionales con vehículos que aceptan y ejecutan los pedidos.

El backend expone una API REST con autenticación JWT. El frontend Android se conecta a esta API.

---

## 2. Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Seguridad | Spring Security 6 + JWT (JJWT) |
| Persistencia | Spring Data JPA + Hibernate 6 |
| Base de datos | MySQL 8 |
| Validación | Jakarta Bean Validation |
| Build | Maven 3.9 |
| Puerto | 8080 |

---

## 3. Arquitectura y estructura de paquetes

```
com.pixpax.app
├── config/
│   ├── SecurityConfig.java       — Cadena de filtros, CORS, reglas de acceso
│   └── DataInitializer.java      — Datos demo al arranque (CommandLineRunner)
├── controller/
│   ├── AuthController.java       — /api/auth/registro, /api/auth/login
│   ├── VehiculoController.java   — /api/vehiculos/**  [TRANSPORTISTA]
│   ├── ViajeController.java      — /api/viajes/**     [mixto]
│   └── TipoMercanciaController.java — /api/tipo-mercancia  [público]
├── service/
│   ├── AuthService.java          — Registro, login, generación JWT
│   ├── VehiculoService.java      — CRUD vehículos
│   ├── ViajeService.java         — Lógica de negocio del viaje
│   └── TipoMercanciaService.java — Catálogo de tipos
├── repository/                   — Interfaces JPA (Spring Data)
├── entity/                       — Entidades JPA (@Entity)
├── dto/                          — Clases de respuesta (sin datos internos)
│   └── request/                  — Clases de petición (@Valid)
├── security/
│   ├── JwtUtil.java              — Generación y parsing de tokens
│   ├── JwtAuthFilter.java        — Filtro OncePerRequestFilter
│   └── AuthUtils.java            — Helper para extraer userId del token
├── exception/
│   ├── GlobalExceptionHandler.java — @RestControllerAdvice
│   └── ErrorResponse.java
└── enums/
    ├── Rol.java                  — CLIENTE, TRANSPORTISTA
    ├── EstadoViaje.java          — 9 estados
    ├── TipoVehiculo.java         — 7 tipos
    ├── CarnetRequerido.java      — B, C1, C, C_E
    └── CompatibilidadMercancia.java — SI, CON_REQUISITOS
```

**Flujo de una petición:**
```
Android → JwtAuthFilter → SecurityConfig (roles) → Controller → Service → Repository → MySQL
```

---

## 4. Modelo de datos

### Usuario
| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK autoincrement |
| nombre | String(100) | NOT NULL |
| apellidos | String(150) | NOT NULL |
| email | String(200) | UNIQUE, NOT NULL |
| passwordHash | String(255) | BCrypt, nunca se expone |
| telefono | String(20) | nullable |
| rol | Rol | CLIENTE / TRANSPORTISTA |
| createdAt | LocalDateTime | autogenerado |

### Vehiculo
| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| matricula | String(15) | UNIQUE, NOT NULL, guardada en MAYÚSCULAS |
| marca | String(80) | NOT NULL |
| modelo | String(80) | NOT NULL |
| tipoVehiculo | TipoVehiculo | enum |
| subtipo | String(50) | nullable |
| taraKg | BigDecimal | nullable |
| capacidadKg | BigDecimal | NOT NULL |
| mmaKg | BigDecimal | nullable |
| carnetRequerido | CarnetRequerido | NOT NULL |
| transportista | Usuario | FK, NOT NULL |

### Viaje
| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| cliente | Usuario | FK, NOT NULL |
| transportista | Usuario | FK, nullable (se asigna al aceptar) |
| vehiculo | Vehiculo | FK, nullable (se asigna al aceptar) |
| tipoMercancia | TipoMercancia | FK |
| descripcionMercancia | String(500) | nullable |
| direccionRecogida | String(300) | NOT NULL |
| direccionEntrega | String(300) | NOT NULL |
| pesoKg | BigDecimal | NOT NULL, > 0 |
| estado | EstadoViaje | default PENDIENTE |
| fechaSolicitud | LocalDateTime | autogenerado al crear |
| fechaInicio | LocalDateTime | se rellena al aceptar |
| fechaFin | LocalDateTime | se rellena al completar/cancelar |

### TipoMercancia
| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| nombre | String(100) | visible al cliente |
| nombreDb | String(100) | identificador interno, NO expuesto en la API |
| descripcion | String(500) | nullable |

---

## 5. Seguridad y autenticación JWT

### Flujo de autenticación
```
1. POST /api/auth/login  →  respuesta: { token, usuario }
2. Cada petición protegida:  Authorization: Bearer <token>
3. JwtAuthFilter valida el token y establece el contexto de seguridad
```

### Estructura del token JWT
```json
Header: { "alg": "HS384" }
Payload: {
  "sub": "email@ejemplo.com",
  "userId": 123,
  "rol": "CLIENTE",
  "iat": 1700000000,
  "exp": 1700086400
}
```

### Roles y acceso
| Endpoint | Sin token | CLIENTE | TRANSPORTISTA |
|---|---|---|---|
| `GET /api/tipo-mercancia` | ✅ 200 | ✅ 200 | ✅ 200 |
| `POST /api/auth/registro` | ✅ 201 | — | — |
| `POST /api/auth/login` | ✅ 200 | — | — |
| `GET /api/viajes` | ❌ 401 | ✅ (sus viajes) | ✅ (sus viajes) |
| `POST /api/viajes` | ❌ 401 | ✅ 201 | ❌ 403 |
| `GET /api/viajes/disponibles` | ❌ 401 | ❌ 403 | ✅ 200 |
| `GET /api/viajes/{id}` | ❌ 401 | ✅ (si es suyo) | ✅ (si es suyo o PENDIENTE) |
| `PUT /api/viajes/{id}/aceptar` | ❌ 401 | ❌ 403 | ✅ 200 |
| `PUT /api/viajes/{id}/estado` | ❌ 401 | ❌ 403 | ✅ 200 |
| `GET /api/vehiculos/mis-vehiculos` | ❌ 401 | ❌ 403 | ✅ 200 |
| `POST /api/vehiculos` | ❌ 401 | ❌ 403 | ✅ 201 |

---

## 6. API Reference — Endpoints

### AUTH

#### `POST /api/auth/registro`
Registra un nuevo usuario.

**Body:**
```json
{
  "nombre": "María",
  "apellidos": "López Fernández",
  "email": "maria@ejemplo.com",
  "password": "MiPass123",
  "telefono": "612345678",
  "rol": "CLIENTE"
}
```
- `rol`: `"CLIENTE"` o `"TRANSPORTISTA"`
- `password`: mínimo 8 caracteres
- `telefono`: opcional, formato `^[0-9+\s()-]{7,20}$`

**Respuesta 201:**
```json
{
  "id": 1,
  "nombre": "María",
  "apellidos": "López Fernández",
  "email": "maria@ejemplo.com",
  "telefono": "612345678",
  "rol": "CLIENTE"
}
```

---

#### `POST /api/auth/login`
Autentica un usuario y devuelve el token JWT.

**Body:**
```json
{
  "email": "maria@ejemplo.com",
  "password": "MiPass123"
}
```

**Respuesta 200:**
```json
{
  "token": "eyJhbGciOiJIUzM4NJ9...",
  "usuario": {
    "id": 1,
    "nombre": "María",
    "apellidos": "López Fernández",
    "email": "maria@ejemplo.com",
    "telefono": "612345678",
    "rol": "CLIENTE"
  }
}
```

---

### TIPO-MERCANCIA

#### `GET /api/tipo-mercancia` — Público
Devuelve el catálogo completo de tipos de mercancía.

**Respuesta 200:**
```json
[
  { "id": 1, "nombre": "Paquetería general", "descripcion": "Paquetes, cajas, envíos estándar" },
  { "id": 2, "nombre": "Electrónica frágil", "descripcion": "..." },
  ...
]
```

**12 tipos disponibles:**
1. Paquetería general
2. Electrónica frágil
3. Alimentos frescos
4. Alimentos congelados
5. Materiales de construcción
6. Mobiliario
7. Mercancía peligrosa (ADR)
8. Productos farmacéuticos
9. Textil y moda
10. Maquinaria industrial
11. Documentación y archivo
12. Vehículos y automoción

---

### VEHÍCULOS — requiere rol TRANSPORTISTA

#### `GET /api/vehiculos/mis-vehiculos`
Lista los vehículos del transportista autenticado.

**Headers:** `Authorization: Bearer <token>`

**Respuesta 200:**
```json
[
  {
    "id": 11,
    "matricula": "1234ABC",
    "marca": "Renault",
    "modelo": "Master",
    "tipoVehiculo": "FURGON_GRANDE",
    "subtipo": "Caja cerrada isotérmica",
    "taraKg": 1850.00,
    "capacidadKg": 1500.00,
    "mmaKg": 3500.00,
    "carnetRequerido": "B",
    "transportistaId": 12
  }
]
```

---

#### `POST /api/vehiculos`
Crea un nuevo vehículo para el transportista autenticado.

**Headers:** `Authorization: Bearer <token>`

**Body:**
```json
{
  "matricula": "5678XYZ",
  "marca": "Mercedes",
  "modelo": "Sprinter",
  "tipoVehiculo": "FURGON_GRANDE",
  "subtipo": "Frigorífico",
  "taraKg": 2100,
  "capacidadKg": 1200,
  "mmaKg": 3500,
  "carnetRequerido": "B"
}
```
- `tipoVehiculo`: `FURGONETA`, `FURGON_GRANDE`, `FRIGORIFICO`, `CAMION_LIGERO`, `CAMION_PESADO`, `CAMION_ARTICULADO`, `PLATAFORMA`
- `carnetRequerido`: `B`, `C1`, `C`, `C_E`
- `subtipo`, `taraKg`, `mmaKg`: opcionales

**Respuesta 201:** VehiculoDTO (mismo formato que GET)

---

### VIAJES

#### `GET /api/viajes`
Lista los viajes del usuario autenticado (adaptado por rol).
- CLIENTE: ve los viajes que él ha creado.
- TRANSPORTISTA: ve los viajes que él ha aceptado.

**Headers:** `Authorization: Bearer <token>`

**Respuesta 200:** Array de ViajeDTO.

---

#### `GET /api/viajes/disponibles` — requiere TRANSPORTISTA
Lista todos los viajes en estado PENDIENTE disponibles para aceptar.

**Respuesta 200:** Array de ViajeDTO con `estado: "PENDIENTE"`.

---

#### `GET /api/viajes/{id}`
Detalle de un viaje específico.
- CLIENTE: solo puede ver viajes donde él es el cliente.
- TRANSPORTISTA: puede ver sus viajes asignados y cualquier viaje PENDIENTE.

**Respuesta 200 — ViajeDTO:**
```json
{
  "id": 15,
  "clienteId": 11,
  "clienteNombre": "María López Fernández",
  "transportistaId": 12,
  "transportistaNombre": "Carlos García Martínez",
  "vehiculoId": 11,
  "vehiculoMatricula": "1234ABC",
  "tipoMercanciaId": 1,
  "tipoMercanciaNombre": "Paquetería general",
  "descripcionMercancia": "12 palés ropa deportiva",
  "direccionRecogida": "Polígono Can Fontanet, Terrassa, Barcelona",
  "direccionEntrega": "Centro Logístico Mercamadrid, Madrid",
  "pesoKg": 480.00,
  "estado": "ACEPTADO",
  "fechaSolicitud": "2026-06-03T10:00:00",
  "fechaInicio": "2026-06-03T10:05:00",
  "fechaFin": null
}
```

---

#### `POST /api/viajes` — requiere CLIENTE

**Body:**
```json
{
  "tipoMercanciaId": 1,
  "descripcionMercancia": "Descripción opcional de la carga",
  "direccionRecogida": "Dirección completa de recogida",
  "direccionEntrega": "Dirección completa de entrega",
  "pesoKg": 480
}
```
- `tipoMercanciaId`: ID del tipo del catálogo (1-12)
- `pesoKg`: obligatorio, mayor que 0
- El viaje se crea siempre en estado `PENDIENTE`

**Respuesta 201:** ViajeDTO

---

#### `PUT /api/viajes/{id}/aceptar` — requiere TRANSPORTISTA
El transportista acepta un viaje PENDIENTE asignándole uno de sus vehículos.

**Body:**
```json
{
  "vehiculoId": 11
}
```

**Validaciones:**
- El viaje debe estar en estado `PENDIENTE`
- El vehículo debe pertenecer al transportista autenticado
- El `pesoKg` del viaje no puede superar la `capacidadKg` del vehículo

**Respuesta 200:** ViajeDTO con `estado: "ACEPTADO"` y `fechaInicio` rellenada.

---

#### `PUT /api/viajes/{id}/estado` — requiere TRANSPORTISTA
Avanza el estado del viaje. Solo el transportista asignado puede llamarlo.

**Body:**
```json
{
  "nuevoEstado": "SALIDA_RECOGIDA"
}
```

**Respuesta 200:** ViajeDTO actualizado.

---

## 7. Máquina de estados del viaje

```
PENDIENTE
    │
    ▼ (aceptar — asigna transportista + vehículo)
ACEPTADO
    │
    ▼ (actualizar estado)
SALIDA_RECOGIDA      ← Transportista sale hacia el punto de recogida
    │
    ▼
LLEGADA_RECOGIDA     ← Transportista llega al punto de recogida
    │
    ▼
MERCANCIA_RECOGIDA   ← Mercancía cargada en el vehículo
    │
    ▼
SALIDA_ENTREGA       ← Transportista sale hacia el destino
    │
    ▼
LLEGADA_ENTREGA      ← Transportista llega al destino
    │
    ▼
COMPLETADO           ← Entrega confirmada (se registra fechaFin)

  Desde cualquier estado (excepto COMPLETADO y CANCELADO):
    └──▶ CANCELADO   (se registra fechaFin)
```

**Reglas:**
- Las transiciones deben ser secuenciales (no se puede saltar estados).
- El estado `CANCELADO` permite salir del flujo desde cualquier punto excepto estados finales.
- Solo el transportista asignado puede cambiar el estado.
- `fechaInicio` se registra al pasar a `ACEPTADO`.
- `fechaFin` se registra al pasar a `COMPLETADO` o `CANCELADO`.

---

## 8. Flujo de presentación (Demo)

### Credenciales de demo
| Rol | Email | Password |
|---|---|---|
| CLIENTE | `demo.cliente@pispax.com` | `Demo1234` |
| TRANSPORTISTA | `demo.transportista@pispax.com` | `Demo1234` |

**Vehículo demo:** Renault Master · Matrícula `1234ABC` · Capacidad 1500 kg · Carnet B

---

### Secuencia completa para la demo en Postman

#### Paso 1 — Login como CLIENTE
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "demo.cliente@pispax.com",
  "password": "Demo1234"
}
```
→ Guarda el `token` como variable `TOKEN_CLIENTE`

---

#### Paso 2 — Consultar catálogo (sin token)
```
GET http://localhost:8080/api/tipo-mercancia
```
→ Muestra los 12 tipos de mercancía disponibles

---

#### Paso 3 — Cliente crea petición de viaje
```
POST http://localhost:8080/api/viajes
Authorization: Bearer {{TOKEN_CLIENTE}}
Content-Type: application/json

{
  "tipoMercanciaId": 1,
  "descripcionMercancia": "12 palés de ropa deportiva temporada verano",
  "direccionRecogida": "Polígono Industrial Can Fontanet, Terrassa, Barcelona",
  "direccionEntrega": "Centro Logístico Mercamadrid, Madrid",
  "pesoKg": 480
}
```
→ Respuesta con `estado: "PENDIENTE"`. Guarda el `id` como `VIAJE_ID`.

---

#### Paso 4 — Login como TRANSPORTISTA
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "demo.transportista@pispax.com",
  "password": "Demo1234"
}
```
→ Guarda el `token` como `TOKEN_TRANSPORTISTA`

---

#### Paso 5 — Transportista busca viajes disponibles
```
GET http://localhost:8080/api/viajes/disponibles
Authorization: Bearer {{TOKEN_TRANSPORTISTA}}
```
→ Aparece el viaje recién creado con todos los detalles.

---

#### Paso 6 — Transportista consulta detalle del viaje
```
GET http://localhost:8080/api/viajes/{{VIAJE_ID}}
Authorization: Bearer {{TOKEN_TRANSPORTISTA}}
```

---

#### Paso 7 — Transportista acepta el viaje
```
PUT http://localhost:8080/api/viajes/{{VIAJE_ID}}/aceptar
Authorization: Bearer {{TOKEN_TRANSPORTISTA}}
Content-Type: application/json

{
  "vehiculoId": 11
}
```
→ Estado cambia a `ACEPTADO`, se registra `fechaInicio`.

---

#### Pasos 8–13 — Avance de estados
```
PUT http://localhost:8080/api/viajes/{{VIAJE_ID}}/estado
Authorization: Bearer {{TOKEN_TRANSPORTISTA}}
Content-Type: application/json

{ "nuevoEstado": "SALIDA_RECOGIDA" }
```
Repetir con:
- `"LLEGADA_RECOGIDA"`
- `"MERCANCIA_RECOGIDA"`
- `"SALIDA_ENTREGA"`
- `"LLEGADA_ENTREGA"`
- `"COMPLETADO"`

---

#### Paso 14 — Cliente consulta su viaje completado
```
GET http://localhost:8080/api/viajes/{{VIAJE_ID}}
Authorization: Bearer {{TOKEN_CLIENTE}}
```
→ `estado: "COMPLETADO"`, `fechaInicio` y `fechaFin` rellenadas.

---

#### Extra — Probar seguridad
```bash
# Sin token → 401
GET http://localhost:8080/api/viajes

# Token inválido → 401
GET http://localhost:8080/api/viajes
Authorization: Bearer token.invalido.aqui

# Cliente intenta crear vehículo → 403
POST http://localhost:8080/api/vehiculos
Authorization: Bearer {{TOKEN_CLIENTE}}

# Transición de estado inválida → 409
PUT http://localhost:8080/api/viajes/{{VIAJE_ID}}/estado
{ "nuevoEstado": "PENDIENTE" }
```

---

## 9. Configuración y arranque

### Prerrequisitos
- Java 17
- MySQL 8 corriendo en `localhost:3306`
- Base de datos `pispax` creada (`CREATE DATABASE pispax CHARACTER SET utf8mb4;`)
- Maven 3.9

### Ficheros de configuración

**`application.properties`** (en git, sin secretos):
```properties
spring.profiles.active=local
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/pispax?useSSL=false&...&characterEncoding=UTF-8}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
jwt.secret=${JWT_SECRET:}
jwt.expiration=86400000
```

**`application-local.properties`** (NO en git — contiene secretos):
```properties
spring.datasource.password=TU_PASSWORD_MYSQL
jwt.secret=clave_secreta_larga_al_menos_32_caracteres
spring.jpa.show-sql=true
logging.level.com.pixpax=DEBUG
```

### Arranque
```bash
# Desde IntelliJ: Run > PispaxApplication
# Desde terminal:
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8080`.

Al primer arranque, `data.sql` inserta los 12 tipos de mercancía y `DataInitializer` crea los usuarios demo.

---

## 10. Datos demo precargados

### Usuarios
| Nombre | Email | Password | Rol |
|---|---|---|---|
| María López Fernández | `demo.cliente@pispax.com` | `Demo1234` | CLIENTE |
| Carlos García Martínez | `demo.transportista@pispax.com` | `Demo1234` | TRANSPORTISTA |

### Vehículo del transportista demo
| Campo | Valor |
|---|---|
| Matrícula | `1234ABC` |
| Marca / Modelo | Renault Master |
| Tipo | FURGON_GRANDE — Caja cerrada isotérmica |
| Capacidad | 1.500 kg |
| MMA | 3.500 kg |
| Carnet | B |

### Viaje ficticio de presentación
| Campo | Valor |
|---|---|
| Estado inicial | **PENDIENTE** |
| Cliente | María López Fernández |
| Recogida | Polígono Industrial Can Fontanet, C/ de la Indústria 8, 08225 Terrassa, Barcelona |
| Entrega | Centro Logístico Mercamadrid, Parcela M-40, 28053 Madrid |
| Mercancía | Paquetería general |
| Descripción | 12 palés de ropa deportiva temporada verano — frágil, no apilar más de 2 alturas |
| Peso | 480 kg |

---

## 11. Guía de integración Android

### Base URL
```
http://10.0.2.2:8080    ← emulador Android (apunta a localhost del PC)
http://<IP_LOCAL>:8080  ← dispositivo físico en la misma red WiFi
```

### Cabeceras obligatorias para endpoints protegidos
```
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

### Flujo de autenticación en Android
1. Login con `POST /api/auth/login` → guardar `token` y `usuario.rol` en `SharedPreferences` o `DataStore`.
2. Incluir `Authorization: Bearer <token>` en cada petición con Retrofit interceptor.
3. Si la respuesta es 401 → limpiar sesión y redirigir al login.

### Ejemplo Retrofit (Kotlin)
```kotlin
// Interceptor JWT
val authInterceptor = Interceptor { chain ->
    val token = SessionManager.getToken()
    val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    chain.proceed(request)
}

// Manejo de errores por código HTTP
when (response.code()) {
    401 -> // Token expirado o inválido → logout
    403 -> // Sin permiso → mostrar mensaje
    400 -> // Error de validación → leer body.mensaje
    409 -> // Conflicto (duplicado) → leer body.mensaje
    500 -> // Error servidor → mensaje genérico
}
```

### Formato de error estándar
Todos los errores devuelven:
```json
{
  "mensaje": "Descripción del error",
  "codigo": 400
}
```

---

## 12. Errores y códigos HTTP

| Código | Significado | Cuándo ocurre |
|---|---|---|
| 200 | OK | Petición exitosa |
| 201 | Created | Recurso creado (registro, viaje, vehículo) |
| 400 | Bad Request | Validación fallida, email duplicado, password corta |
| 401 | Unauthorized | Sin token, token inválido, token expirado, credenciales incorrectas |
| 403 | Forbidden | Token válido pero rol sin permisos, acceso a recurso ajeno |
| 404 | Not Found | Recurso no existe |
| 409 | Conflict | Transición de estado inválida, matrícula duplicada (constraint BD) |
| 500 | Internal Server Error | Error inesperado (se loguea en servidor) |

---

## 13. Limitaciones conocidas (MVP)

Funcionalidades identificadas que **no están implementadas** en esta versión:

| Limitación | Descripción | Impacto |
|---|---|---|
| Compatibilidad vehículo-mercancía | La tabla `vehiculo_tipo_mercancia` existe pero no hay API para gestionar las entradas. El filtro de viajes disponibles no usa compatibilidad. | Bajo para demo |
| Paginación | Los endpoints devuelven listas completas. Con muchos registros puede ser lento. | Bajo para demo |
| Validación ADR/frío | No se verifica que un vehículo refrigerado transporte alimentos congelados, etc. | Medio |
| Notificaciones push | No hay sistema de notificaciones cuando un viaje cambia de estado. | Pendiente Android |
| Valoraciones | No hay sistema de puntuación cliente↔transportista. | Post-MVP |
| Historial de estados | No se guardan los timestamps de cada transición, solo fechaInicio y fechaFin. | Menor |
| HTTPS/TLS | Solo HTTP en desarrollo. En producción se requiere TLS. | Crítico para prod |
| Versionado de API | No hay `/v1/`. Cambios romperían clientes existentes. | Post-MVP |

---

## 14. Resultados de la test suite

Tests ejecutados contra el backend en ejecución:

| Sección | Tests | Resultado |
|---|---|---|
| JWT / Seguridad | 4 | ✅ 4/4 |
| Registro | 9 | ✅ 9/9 |
| Roles | 3 | ✅ 3/3 |
| Catálogo tipo-mercancía | 4 | ✅ 4/4 |
| Vehículos | 5 | ✅ 5/5 |
| Viajes — Creación y validación | 3 | ✅ 3/3 |
| Viajes — Visibilidad y aislamiento | 4 | ✅ 4/4 |
| Máquina de estados (6 transiciones) | 11 | ✅ 11/11 |
| Mis viajes | 2 | ✅ 2/2 |
| Demo data | 4 | ✅ 4/4 |
| **TOTAL** | **49** | **✅ 49/49** |

---

*Generado el 2026-06-03 · PisPax TFG — Adrián Salazar*
