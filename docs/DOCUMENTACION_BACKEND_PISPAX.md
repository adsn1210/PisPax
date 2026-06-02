# Documentación Técnica — Backend PisPax
### TFG DAM 2025/2026 — Adrián Salazar

---

## Índice

1. [Visión general del proyecto](#1-vision-general)
2. [Stack tecnológico](#2-stack-tecnologico)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Configuración y conexión a MySQL](#4-configuracion-y-conexion-a-mysql)
5. [Cómo funciona la conexión internamente](#5-como-funciona-la-conexion-internamente)
6. [Modelo de datos — Tablas y Entidades](#6-modelo-de-datos)
7. [Seguridad — JWT y Spring Security](#7-seguridad-jwt)
8. [API REST — Endpoints disponibles](#8-api-rest-endpoints)
9. [Lógica de negocio — Flujo de estados de un viaje](#9-flujo-de-estados)
10. [Manejo de errores](#10-manejo-de-errores)
11. [Datos precargados — data.sql](#11-datos-precargados)
12. [Cómo arrancar el proyecto paso a paso](#12-como-arrancar)
13. [Tests necesarios para el TFG](#13-tests)
14. [Conexión con el Frontend Android](#14-conexion-frontend)
15. [Resultado de la verificación realizada](#15-verificacion-realizada)
16. [Avisos y mejoras pendientes](#16-mejoras-pendientes)

---

## 1. Visión general

**PisPax** es una aplicación móvil para gestión de transporte de mercancías, funcionando como un "Uber para transporte". Conecta dos tipos de usuario:

- **Cliente**: publica solicitudes de transporte (viajes) con origen, destino, tipo de mercancía y peso.
- **Transportista**: acepta los viajes disponibles con su vehículo y actualiza el estado del trayecto en tiempo real.

El backend actúa como servidor central: expone una **API REST** a la que tanto el cliente Android como cualquier herramienta de prueba (Postman, curl) pueden conectarse mediante peticiones HTTP con autenticación JWT.

---

## 2. Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.2.5 |
| Seguridad | Spring Security | 6.x (incluido en Boot 3.2) |
| Autenticación | JWT (jjwt) | 0.12.5 |
| Base de datos | MySQL | 8.0 |
| ORM | Hibernate / Spring Data JPA | 6.4.4 |
| Pool de conexiones | HikariCP | Incluido en Boot |
| Reducción boilerplate | Lombok | Última estable |
| Construcción | Maven | 3.x |
| Servidor embebido | Apache Tomcat | 10.1.20 |

---

## 3. Estructura del proyecto

```
pispax-backend/
├── pom.xml                            ← Dependencias Maven
└── src/
    └── main/
        ├── java/com/pixpax/app/
        │   ├── PispaxApplication.java         ← Punto de entrada (main)
        │   ├── config/
        │   │   └── SecurityConfig.java        ← Reglas de seguridad HTTP
        │   ├── controller/
        │   │   ├── AuthController.java        ← /api/auth/registro y /login
        │   │   ├── ViajeController.java       ← /api/viajes/**
        │   │   ├── VehiculoController.java    ← /api/vehiculos/**
        │   │   └── TipoMercanciaController.java ← /api/tipo-mercancia
        │   ├── dto/
        │   │   ├── UsuarioDTO.java
        │   │   ├── ViajeDTO.java
        │   │   ├── VehiculoDTO.java
        │   │   └── request/               ← Objetos de entrada (JSON del cliente)
        │   │       ├── LoginRequest.java
        │   │       ├── RegistroRequest.java
        │   │       ├── CrearViajeRequest.java
        │   │       ├── CrearVehiculoRequest.java
        │   │       ├── AceptarViajeRequest.java
        │   │       └── ActualizarEstadoRequest.java
        │   ├── entity/                    ← Entidades JPA (mapeo con tablas MySQL)
        │   │   ├── Usuario.java
        │   │   ├── Vehiculo.java
        │   │   ├── Viaje.java
        │   │   ├── TipoMercancia.java
        │   │   ├── VehiculoTipoMercancia.java
        │   │   └── VehiculoTipoMercanciaId.java
        │   ├── enums/                     ← Tipos enumerados
        │   │   ├── Rol.java               ← CLIENTE, TRANSPORTISTA
        │   │   ├── EstadoViaje.java       ← PENDIENTE → ... → COMPLETADO
        │   │   ├── TipoVehiculo.java
        │   │   ├── CarnetRequerido.java
        │   │   └── CompatibilidadMercancia.java
        │   ├── exception/
        │   │   ├── ErrorResponse.java     ← Formato JSON de error
        │   │   └── GlobalExceptionHandler.java ← Captura global de excepciones
        │   ├── repository/                ← Interfaces JPA (acceso a BD)
        │   │   ├── UsuarioRepository.java
        │   │   ├── VehiculoRepository.java
        │   │   ├── ViajeRepository.java
        │   │   └── TipoMercanciaRepository.java
        │   ├── security/
        │   │   ├── JwtUtil.java           ← Genera y valida tokens JWT
        │   │   └── JwtAuthFilter.java     ← Intercepta cada petición HTTP
        │   └── service/
        │       ├── AuthService.java       ← Lógica de registro y login
        │       ├── ViajeService.java      ← Lógica de viajes y transiciones
        │       └── VehiculoService.java   ← Lógica de vehículos
        └── resources/
            ├── application.properties     ← Configuración de la aplicación
            └── data.sql                   ← Datos iniciales (12 tipos de mercancía)
```

**Patrón arquitectónico:** Controller → Service → Repository → MySQL.  
El Controller recibe la petición HTTP, el Service aplica la lógica de negocio y el Repository habla con la base de datos a través de JPA/Hibernate.

---

## 4. Configuración y conexión a MySQL

### Archivo de configuración

**Ruta:** `pispax-backend/src/main/resources/application.properties`

```properties
spring.application.name=pispax-backend

# ── BASE DE DATOS MYSQL ──────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/pispax?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=nairda04
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── JPA / HIBERNATE ──────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Ejecutar data.sql DESPUÉS de que Hibernate cree/actualice las tablas
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# ── SERVIDOR ──────────────────────────────────────────────────────────────────
server.port=8080

# ── JWT ───────────────────────────────────────────────────────────────────────
jwt.secret=pispax_jwt_secret_key_2026_muy_larga_para_seguridad_hmac_sha256
jwt.expiration=86400000

# ── LOGGING ───────────────────────────────────────────────────────────────────
logging.level.com.pixpax=DEBUG
logging.level.org.springframework.security=INFO
```

### Explicación línea por línea de la URL de conexión

```
jdbc:mysql://localhost:3306/pispax?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

| Fragmento | Significado |
|---|---|
| `jdbc:mysql://` | Protocolo JDBC para MySQL |
| `localhost` | El servidor MySQL está en la misma máquina que el backend |
| `3306` | Puerto estándar de MySQL (el servicio Windows se llama MYSQL80) |
| `pispax` | Nombre de la base de datos a la que conecta |
| `useSSL=false` | Desactiva SSL (entorno de desarrollo local) |
| `serverTimezone=UTC` | Evita errores de zona horaria entre Java y MySQL |
| `allowPublicKeyRetrieval=true` | Necesario con MySQL 8 cuando useSSL=false para intercambiar la clave pública |

### Parámetros de JPA relevantes

| Propiedad | Valor | Qué hace |
|---|---|---|
| `ddl-auto=update` | update | Hibernate compara las entidades con las tablas existentes y aplica ALTER TABLE si es necesario. **No borra datos.** |
| `show-sql=true` | true | Imprime cada consulta SQL en el log (útil para depurar) |
| `sql.init.mode=always` | always | Ejecuta `data.sql` en cada arranque |
| `defer-datasource-initialization=true` | true | Garantiza que `data.sql` se ejecuta DESPUÉS de que Hibernate haya creado/actualizado las tablas |

### Puerto del servidor

El backend escucha en `http://localhost:8080`. Todos los endpoints de la API tienen el prefijo `/api/`.

---

## 5. Cómo funciona la conexión internamente

Cuando se arranca Spring Boot, ocurre la siguiente secuencia de pasos en orden:

```
1. Spring Boot lee application.properties
         │
         ▼
2. HikariCP (pool de conexiones) abre conexiones a MySQL
   usando la URL, usuario y contraseña configurados.
   Crea un pool de ~10 conexiones reutilizables.
         │
         ▼
3. Hibernate analiza todas las clases @Entity
   y compara su estructura con las tablas reales de MySQL.
   Con ddl-auto=update, crea columnas o tablas que falten
   sin borrar las existentes.
         │
         ▼
4. Spring ejecuta data.sql (INSERT IGNORE de los 12 tipos
   de mercancía). El INSERT IGNORE evita duplicados si ya
   existen.
         │
         ▼
5. Spring Security configura la cadena de filtros JWT.
         │
         ▼
6. Tomcat (servidor HTTP embebido) se levanta en el puerto 8080.
         │
         ▼
7. El log muestra: "Started PispaxApplication in X.XX seconds"
```

**Lo que se vio en el log durante la verificación:**

```
HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@766b6d02
HikariPool-1 - Start completed.
...
Started PispaxApplication in 2.82 seconds (process running for 3.015)
```

Esto confirma que: la conexión al pool se estableció con éxito, y el servidor quedó operativo en 2.82 segundos.

---

## 6. Modelo de datos

### Tablas en MySQL (base de datos: `pispax`)

Se verificó que las 5 tablas existen correctamente:

```sql
SHOW TABLES;
-- Resultado:
tipo_mercancia
usuario
vehiculo
vehiculo_tipo_mercancia
viaje
```

### Descripción de cada tabla

#### `usuario`
Almacena tanto clientes como transportistas, diferenciados por el campo `rol`.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT (PK, AUTO) | Identificador único |
| nombre | VARCHAR(100) | Nombre del usuario |
| apellidos | VARCHAR(150) | Apellidos |
| email | VARCHAR(200) UNIQUE | Email — usado para login |
| password_hash | VARCHAR(255) | Contraseña encriptada con BCrypt |
| telefono | VARCHAR(20) | Teléfono de contacto |
| rol | ENUM | `CLIENTE` o `TRANSPORTISTA` |
| created_at | DATETIME | Fecha de registro (automática) |

#### `vehiculo`
Cada vehículo pertenece a un transportista.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT (PK, AUTO) | Identificador único |
| matricula | VARCHAR(15) UNIQUE | Matrícula del vehículo |
| marca | VARCHAR(80) | Marca del vehículo |
| modelo | VARCHAR(80) | Modelo |
| tipo_vehiculo | ENUM | Ej: FURGONETA, CAMION... |
| capacidad_kg | DECIMAL(8,2) | Capacidad máxima de carga |
| carnet_requerido | ENUM | Ej: B, C, C1... |
| transportista_id | BIGINT (FK) | Referencia al usuario transportista |

#### `viaje`
Solicitud de transporte creada por un cliente.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT (PK, AUTO) | Identificador único |
| cliente_id | BIGINT (FK) | Usuario que solicita el viaje |
| transportista_id | BIGINT (FK) | Transportista que lo acepta (null hasta que se acepta) |
| vehiculo_id | BIGINT (FK) | Vehículo asignado (null hasta aceptación) |
| tipo_mercancia_id | BIGINT (FK) | Tipo de mercancía del catálogo |
| descripcion_mercancia | VARCHAR(500) | Descripción libre |
| direccion_recogida | VARCHAR(300) | Dirección origen |
| direccion_entrega | VARCHAR(300) | Dirección destino |
| peso_kg | DECIMAL(8,2) | Peso de la carga |
| estado | ENUM | Estado actual del viaje |
| fecha_solicitud | DATETIME | Automática al crear |
| fecha_inicio | DATETIME | Cuando el transportista acepta |
| fecha_fin | DATETIME | Cuando se completa o cancela |

#### `tipo_mercancia`
Catálogo fijo de 12 tipos de mercancía, precargado por `data.sql`.

#### `vehiculo_tipo_mercancia`
Tabla de relación N:M entre vehículos y tipos de mercancía que puede transportar.

---

## 7. Seguridad — JWT

### ¿Qué es JWT?

JWT (JSON Web Token) es un estándar para transmitir información de forma segura entre cliente y servidor. En PisPax, se usa para autenticar cada petición sin necesidad de sesiones en el servidor (stateless).

### Flujo completo de autenticación

```
Cliente Android                     Backend PisPax
      │                                    │
      │  POST /api/auth/login              │
      │  {"email":"...", "password":"..."}  │
      │ ─────────────────────────────────► │
      │                                    │ 1. Busca usuario por email
      │                                    │ 2. Verifica password con BCrypt
      │                                    │ 3. Genera JWT con: email, userId, rol
      │  200 OK                            │
      │  {"token": "eyJ...", "usuario":{}} │
      │ ◄───────────────────────────────── │
      │                                    │
      │  GET /api/viajes                   │
      │  Authorization: Bearer eyJ...      │
      │ ─────────────────────────────────► │
      │                                    │ 4. JwtAuthFilter intercepta
      │                                    │ 5. Extrae y valida el token
      │                                    │ 6. Inyecta userId y rol en el contexto
      │  200 OK [lista de viajes]          │
      │ ◄───────────────────────────────── │
```

### Contenido del token JWT

El token tiene tres partes separadas por `.` (header.payload.signature). El payload contiene:

```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

- `sub`: email del usuario (subject estándar JWT)
- `userId`: ID del usuario en la BD — se usa en los controllers para saber quién hace la petición
- `rol`: CLIENTE o TRANSPORTISTA — controla qué endpoints puede usar
- `iat`: issued at (cuándo se emitió)
- `exp`: expiration (cuándo caduca — 86400000ms = 24 horas)

### Configuración de seguridad por endpoint

Definida en `SecurityConfig.java`:

```
/api/auth/**           → Libre (sin token) — registro y login
/api/tipo-mercancia    → Libre (sin token) — catálogo público
Cualquier otra ruta    → Requiere token JWT válido
```

Adicionalmente, algunos endpoints exigen un rol concreto mediante `@PreAuthorize`:

```
@PreAuthorize("hasRole('TRANSPORTISTA')")  → Solo transportistas
@PreAuthorize("hasRole('CLIENTE')")        → Solo clientes
```

### Clave secreta JWT

Configurada en `application.properties`:
```
jwt.secret=pispax_jwt_secret_key_2026_muy_larga_para_seguridad_hmac_sha256
jwt.expiration=86400000
```

La clave se usa con algoritmo HMAC-SHA256. Debe tener al menos 32 caracteres para este algoritmo.

---

## 8. API REST — Endpoints disponibles

### Autenticación (`/api/auth`)

#### Registro de usuario
```
POST http://localhost:8080/api/auth/registro
Content-Type: application/json

{
  "nombre": "Juan",
  "apellidos": "García López",
  "email": "juan@email.com",
  "password": "miPassword123",
  "telefono": "600123456",
  "rol": "CLIENTE"
}

→ 201 Created: UsuarioDTO (sin password)
→ 400 Bad Request: si el email ya existe o faltan campos
```

#### Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan@email.com",
  "password": "miPassword123"
}

→ 200 OK:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": { "id": 1, "nombre": "Juan", "rol": "CLIENTE", ... }
}
→ 400 Bad Request: credenciales incorrectas
```

### Viajes (`/api/viajes`) — Requieren token JWT

#### Listar mis viajes (cliente ve los suyos, transportista los suyos)
```
GET http://localhost:8080/api/viajes
Authorization: Bearer {token}

→ 200 OK: [ ViajeDTO, ... ]
```

#### Ver viajes disponibles para aceptar (solo TRANSPORTISTA)
```
GET http://localhost:8080/api/viajes/disponibles
Authorization: Bearer {token_transportista}

→ 200 OK: [ ViajeDTO, ... ]
→ 403 Forbidden: si el token es de un CLIENTE
```

#### Detalle de un viaje
```
GET http://localhost:8080/api/viajes/{id}
Authorization: Bearer {token}

→ 200 OK: ViajeDTO
→ 404 Not Found: si no existe
```

#### Crear viaje (solo CLIENTE)
```
POST http://localhost:8080/api/viajes
Authorization: Bearer {token_cliente}
Content-Type: application/json

{
  "tipoMercanciaId": 1,
  "descripcionMercancia": "Caja con ropa",
  "direccionRecogida": "Calle Mayor 1, Madrid",
  "direccionEntrega": "Avenida de la Paz 5, Barcelona",
  "pesoKg": 50.0
}

→ 201 Created: ViajeDTO (estado: PENDIENTE)
→ 403 Forbidden: si el token es de un TRANSPORTISTA
```

#### Aceptar un viaje (solo TRANSPORTISTA)
```
PUT http://localhost:8080/api/viajes/{id}/aceptar
Authorization: Bearer {token_transportista}
Content-Type: application/json

{
  "vehiculoId": 1
}

→ 200 OK: ViajeDTO (estado: ACEPTADO)
→ 409 Conflict: si el viaje no está PENDIENTE
→ 400 Bad Request: si el peso excede la capacidad del vehículo
```

#### Actualizar estado del viaje (solo TRANSPORTISTA propietario)
```
PUT http://localhost:8080/api/viajes/{id}/estado
Authorization: Bearer {token_transportista}
Content-Type: application/json

{
  "nuevoEstado": "SALIDA_RECOGIDA"
}

→ 200 OK: ViajeDTO
→ 409 Conflict: si la transición de estado no es válida
→ 403 Forbidden: si el transportista no es el asignado al viaje
```

### Vehículos (`/api/vehiculos`) — Solo TRANSPORTISTA

#### Mis vehículos
```
GET http://localhost:8080/api/vehiculos/mis-vehiculos
Authorization: Bearer {token_transportista}

→ 200 OK: [ VehiculoDTO, ... ]
```

#### Registrar vehículo
```
POST http://localhost:8080/api/vehiculos
Authorization: Bearer {token_transportista}
Content-Type: application/json

{
  "matricula": "1234ABC",
  "marca": "Mercedes",
  "modelo": "Sprinter",
  "tipoVehiculo": "FURGONETA",
  "capacidadKg": 1500.0,
  "carnetRequerido": "B"
}

→ 201 Created: VehiculoDTO
```

### Tipos de mercancía (`/api/tipo-mercancia`) — Público

```
GET http://localhost:8080/api/tipo-mercancia

→ 200 OK: lista de los 12 tipos de mercancía del catálogo
```

---

## 9. Flujo de estados de un viaje

Un viaje pasa por los siguientes estados en orden estricto:

```
PENDIENTE
    │
    │ (transportista llama a PUT /aceptar)
    ▼
ACEPTADO
    │
    │ (transportista llama a PUT /estado)
    ▼
SALIDA_RECOGIDA     ← El transportista sale hacia el punto de recogida
    │
    ▼
LLEGADA_RECOGIDA    ← El transportista llega al punto de recogida
    │
    ▼
MERCANCIA_RECOGIDA  ← La mercancía está cargada en el vehículo
    │
    ▼
SALIDA_ENTREGA      ← El transportista sale hacia el destino
    │
    ▼
LLEGADA_ENTREGA     ← El transportista llega al destino
    │
    ▼
COMPLETADO          ← Entrega confirmada
```

En cualquier punto antes de COMPLETADO, se puede pasar a `CANCELADO`.

La lógica de validación está en `ViajeService.validarTransicion()`:

```java
private void validarTransicion(EstadoViaje actual, EstadoViaje nuevo) {
    if (nuevo == EstadoViaje.CANCELADO) {
        if (actual == EstadoViaje.COMPLETADO || actual == EstadoViaje.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar un viaje ya finalizado");
        }
        return;
    }
    int indexActual = FLUJO_NORMAL.indexOf(actual);
    int indexNuevo  = FLUJO_NORMAL.indexOf(nuevo);
    if (indexNuevo != indexActual + 1) {
        throw new IllegalStateException("Transición inválida: " + actual + " → " + nuevo);
    }
}
```

Esto garantiza que nadie pueda saltar estados ni retroceder.

---

## 10. Manejo de errores

Todas las excepciones se centralizan en `GlobalExceptionHandler.java`. Esto significa que cuando algo falla, el cliente siempre recibe una respuesta JSON uniforme:

```json
{
  "mensaje": "Descripción del error",
  "codigo": 400
}
```

| Excepción | Código HTTP | Cuándo ocurre |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Campo obligatorio vacío o mal formateado |
| `IllegalArgumentException` | 400 | Email duplicado, credenciales incorrectas, vehículo ajeno |
| `IllegalStateException` | 409 Conflict | Transición de estado inválida, viaje no en PENDIENTE |
| `ResponseStatusException` | 404 / 403 | Recurso no encontrado, acceso denegado |
| `RuntimeException` (resto) | 500 | Error inesperado del servidor |

---

## 11. Datos precargados — data.sql

**Ruta:** `pispax-backend/src/main/resources/data.sql`

Este archivo se ejecuta automáticamente en cada arranque. Precarga el catálogo de 12 tipos de mercancía:

```sql
INSERT IGNORE INTO tipo_mercancia (id, nombre, nombre_db, descripcion) VALUES
(1,  'Paquetería general',         'paqueteria_general',    'Paquetes, cajas, envíos estándar'),
(2,  'Electrónica frágil',         'electronica_fragil',    'Dispositivos frágiles, equipos electrónicos'),
(3,  'Alimentos frescos',          'alimentos_frescos',     'Productos perecederos (0–8°C), requiere frío'),
(4,  'Alimentos congelados',       'alimentos_congelados',  'Productos congelados (< -18°C)'),
(5,  'Materiales de construcción', 'materiales_construccion','Cemento, acero, materiales pesados'),
(6,  'Mobiliario',                 'mobiliario',            'Muebles, sofás, piezas grandes'),
(7,  'Mercancía peligrosa (ADR)',  'mercancia_peligrosa',   'Sustancias químicas, inflamables, tóxicas'),
(8,  'Productos farmacéuticos',    'productos_farmaceuticos','Medicinas, biologics — GDP recomendado'),
(9,  'Textil y moda',              'textil_moda',           'Ropa, telas, calzado'),
(10, 'Maquinaria industrial',      'maquinaria_industrial', 'Máquinas, equipos pesados'),
(11, 'Documentación y archivo',    'documentacion_archivo', 'Documentos, libros, archivos'),
(12, 'Vehículos y automoción',     'vehiculos_automocion',  'Coches, motos, piezas vehiculares');
```

El `INSERT IGNORE` es clave: si los datos ya existen, los ignora sin lanzar error. Esto permite arrancar el servidor múltiples veces sin problemas.

---

## 12. Cómo arrancar el proyecto — Paso a paso

### Requisitos previos

Antes de ejecutar el backend, necesitas tener instalado y funcionando:

1. **Java 17** (JDK, no solo JRE)
2. **Maven 3.x** (o usar el wrapper `mvnw` incluido)
3. **MySQL 8.0** corriendo en el puerto 3306
4. La base de datos `pispax` creada

### Paso 1 — Verificar que MySQL está activo (Windows)

Abre PowerShell y ejecuta:

```powershell
Get-Service -Name "MySQL*"
```

Debes ver `Status: Running`. Si está parado:

```powershell
Start-Service MYSQL80
```

### Paso 2 — Verificar que la base de datos existe

Si no existe, créala desde MySQL Workbench o desde línea de comandos:

```powershell
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS pispax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Las tablas **no hace falta crearlas manualmente** — Hibernate las crea automáticamente con `ddl-auto=update`.

### Paso 3 — Arrancar el backend

Desde una terminal situada en el directorio del proyecto:

```powershell
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
mvn spring-boot:run
```

O si se usa el wrapper de Maven (no requiere Maven instalado globalmente):

```powershell
.\mvnw spring-boot:run
```

### Paso 4 — Confirmar que arrancó correctamente

En el log de la consola debes ver estas líneas (en este orden):

```
HikariPool-1 - Start completed.        ← Conexión MySQL OK
Tomcat started on port 8080            ← Servidor HTTP activo
Started PispaxApplication in X.XX seconds  ← Todo correcto
```

### Paso 5 — Verificar con una petición de prueba

Desde PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tipo-mercancia" -Method GET
```

Este endpoint es público (sin token). Debes recibir la lista de 12 tipos de mercancía.

O con curl (si está disponible):

```bash
curl http://localhost:8080/api/tipo-mercancia
```

### Paso 6 — Parar el servidor

En la terminal donde está corriendo el servidor: `Ctrl + C`

---

## 13. Tests necesarios para el TFG

Para el apartado de pruebas (sección 4.5 del TFG), se recomienda cubrir los siguientes niveles:

### 13.1 Tests unitarios de Service

Prueban la lógica de negocio de forma aislada, usando Mockito para simular los repositorios.

**AuthService:**
- Registro exitoso de cliente y transportista
- Registro fallido si el email ya existe
- Login exitoso devuelve token JWT
- Login fallido con contraseña incorrecta
- Login fallido si el usuario no existe

**ViajeService:**
- Crear viaje exitoso (rol CLIENTE)
- Error al crear viaje con tipo de mercancía inexistente
- Aceptar viaje exitoso (rol TRANSPORTISTA)
- Error al aceptar viaje si el peso supera la capacidad del vehículo
- Error al aceptar viaje si el vehículo no pertenece al transportista
- Todas las transiciones de estado válidas (PENDIENTE → ACEPTADO → ... → COMPLETADO)
- Error en transición inválida (ej: PENDIENTE → COMPLETADO)
- Cancelación válida e inválida (no se puede cancelar si ya está COMPLETADO)

### 13.2 Tests de integración con base de datos

Se puede usar H2 (base de datos en memoria) para tests de integración sin necesidad de MySQL.

Añadir en `pom.xml`:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

Crear `src/test/resources/application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

Estos tests comprueban que los repositorios JPA funcionan correctamente con SQL real.

### 13.3 Tests de endpoints (integración completa)

Con `@SpringBootTest` + `MockMvc`, se prueban los endpoints HTTP completos:

```java
@Test
void registroUsuario_retorna201() {
    // POST /api/auth/registro con datos válidos → 201
}

@Test
void login_retornaToken() {
    // POST /api/auth/login con credenciales correctas → 200 con token
}

@Test
void crearViaje_sinToken_retorna403() {
    // POST /api/viajes sin Authorization header → 403
}

@Test
void crearViaje_conTokenCliente_retorna201() {
    // POST /api/viajes con token CLIENTE → 201
}

@Test
void crearViaje_conTokenTransportista_retorna403() {
    // POST /api/viajes con token TRANSPORTISTA → 403
}
```

### 13.4 Tests manuales con Postman

Para la presentación del TFG es muy efectivo mostrar una **colección de Postman** con los flujos principales:

**Flujo 1 — Registro y login de cliente:**
1. POST /api/auth/registro (rol: CLIENTE)
2. POST /api/auth/login → guardar token

**Flujo 2 — Ciclo completo de viaje:**
1. POST /api/auth/registro (rol: TRANSPORTISTA)
2. POST /api/auth/login (transportista) → guardar token
3. POST /api/vehiculos (con token transportista) → registrar vehículo
4. POST /api/viajes (con token cliente) → crear viaje → estado PENDIENTE
5. PUT /api/viajes/{id}/aceptar (con token transportista) → estado ACEPTADO
6. PUT /api/viajes/{id}/estado {"nuevoEstado": "SALIDA_RECOGIDA"}
7. (repetir para cada estado hasta COMPLETADO)

**Flujo 3 — Validaciones de error:**
1. Intentar login con contraseña incorrecta → 400
2. Intentar crear viaje con token de transportista → 403
3. Intentar saltar un estado → 409

---

## 14. Conexión con el Frontend Android

### Qué necesita el Frontend para conectarse al Backend

El cliente Android (Java + Retrofit 2.9) necesita conocer la URL base del backend. Durante el desarrollo con el móvil en la misma red WiFi que el PC:

```java
// En Retrofit, la BASE_URL es la IP local de tu PC, no "localhost"
// "localhost" en Android se refiere al propio teléfono
String BASE_URL = "http://192.168.X.X:8080/";
```

Para saber tu IP local:
```powershell
ipconfig
# Buscar "Dirección IPv4" en el adaptador WiFi, ej: 192.168.1.100
```

### Configuración de Retrofit en Android

```java
// RetrofitClient.java
public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.1.100:8080/";
    private static Retrofit retrofit;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = SharedPrefManager.getToken(); // token guardado tras login
                    Request request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .addHeader("Content-Type", "application/json")
                        .build();
                    return chain.proceed(request);
                })
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit;
    }
}
```

### Interfaz de API en Android (ejemplo)

```java
// ApiService.java
public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/registro")
    Call<UsuarioDTO> registro(@Body RegistroRequest request);

    @GET("api/viajes")
    Call<List<ViajeDTO>> getMisViajes();

    @POST("api/viajes")
    Call<ViajeDTO> crearViaje(@Body CrearViajeRequest request);

    @PUT("api/viajes/{id}/aceptar")
    Call<ViajeDTO> aceptarViaje(@Path("id") Long id, @Body AceptarViajeRequest request);

    @PUT("api/viajes/{id}/estado")
    Call<ViajeDTO> actualizarEstado(@Path("id") Long id, @Body ActualizarEstadoRequest request);

    @GET("api/vehiculos/mis-vehiculos")
    Call<List<VehiculoDTO>> getMisVehiculos();
}
```

### Arrancar todo el sistema (backend + frontend simultáneamente)

Cuando el frontend esté creado, el proceso de arranque completo es:

**Terminal 1 — Backend:**
```powershell
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
mvn spring-boot:run
```
Esperar a: `Started PispaxApplication in X.XX seconds`

**Android Studio:**
- Conectar el móvil o lanzar un emulador
- En el emulador: usar `http://10.0.2.2:8080/` en vez de la IP local
- En móvil físico: usar la IP WiFi de tu PC
- Pulsar Run (triángulo verde)

**Verificar la conexión:**
- El login en Android debe devolver un token
- Si hay un error de red → revisar que el PC y el móvil están en la misma WiFi
- Si hay 403 → el token no se está enviando en la cabecera
- Si hay 404 → la URL base no es correcta

---

## 15. Resultado de la verificación realizada

El día 28/05/2026 se realizó la siguiente comprobación completa del sistema:

### Verificaciones de infraestructura

| Verificación | Comando | Resultado |
|---|---|---|
| Servicio MySQL activo | `Get-Service -Name "MySQL*"` | MYSQL80: **Running** |
| Base de datos existe | `SHOW DATABASES LIKE 'pispax'` | **pispax** encontrada |
| Tablas creadas | `SHOW TABLES` | **5 tablas** presentes |

### Log de arranque de Spring Boot

```
HikariPool-1 - Adding connection com.mysql.cj.jdbc.ConnectionImpl@766b6d02
HikariPool-1 - Start completed.                          ← CONEXIÓN MYSQL: OK
...
Tomcat started on port 8080 (http)                       ← SERVIDOR HTTP: OK
Started PispaxApplication in 2.82 seconds                ← ARRANQUE: OK
```

### Verificación del endpoint

```
GET http://localhost:8080/api/tipo-mercancia → 200 OK (sin token, endpoint público)
GET http://localhost:8080/api/viajes         → 403 Forbidden (sin token, comportamiento correcto)
```

El 403 en `/api/viajes` confirma que Spring Security está funcionando correctamente: rechaza peticiones sin JWT, exactamente como se diseñó.

**Conclusión: la conexión Backend ↔ MySQL es completamente funcional.**

---

## 16. Avisos y mejoras pendientes

Durante la verificación se detectaron los siguientes puntos menores:

### Aviso 1 — `MySQLDialect` redundante
**Línea afectada:** `application.properties`
```properties
# Esta línea se puede eliminar — Hibernate la detecta automáticamente
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```
No afecta al funcionamiento, pero el log muestra un WARN: `HHH90000025: MySQLDialect does not need to be specified explicitly`.

### Aviso 2 — `@EnableMethodSecurity` no está en `SecurityConfig`
Actualmente `@PreAuthorize("hasRole('TRANSPORTISTA')")` en los controllers **no tiene efecto** porque falta la anotación que lo activa. Añadir en `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // ← AÑADIR ESTA LÍNEA
public class SecurityConfig {
```

Sin esto, cualquier usuario autenticado (sea CLIENTE o TRANSPORTISTA) puede llamar a cualquier endpoint protegido por rol, lo cual es un fallo de seguridad.

### Aviso 3 — `spring.jpa.open-in-view` no configurado
Añadir en `application.properties` para eliminar el WARN del log:
```properties
spring.jpa.open-in-view=false
```

### Aviso 4 — `VehiculoTipoMercancia` no se popula al crear vehículo
`VehiculoService.crearVehiculo()` no rellena la tabla `vehiculo_tipo_mercancia`. Esta funcionalidad está pendiente de implementar para que el sistema sepa qué tipos de mercancía puede transportar cada vehículo.

---

*Documento generado el 28/05/2026 como parte del TFG PisPax — Adrián Salazar*
