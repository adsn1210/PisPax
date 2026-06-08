# Documentación Técnica Unificada — Backend PisPax
### TFG DAM 2025/2026 — Adrián Salazar Nicolás
### Versión unificada — Junio 2026

---

## Índice

1. [Visión general del proyecto](#1-visión-general-del-proyecto)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Estructura del proyecto y paquetes](#3-estructura-del-proyecto-y-paquetes)
4. [Patrón arquitectónico por capas](#4-patrón-arquitectónico-por-capas)
5. [Configuración y conexión a MySQL](#5-configuración-y-conexión-a-mysql)
6. [Ciclo de arranque de Spring Boot](#6-ciclo-de-arranque-de-spring-boot)
7. [Modelo de datos — Entidades JPA](#7-modelo-de-datos--entidades-jpa)
8. [ENUMs del dominio](#8-enums-del-dominio)
9. [Seguridad — Spring Security y JWT](#9-seguridad--spring-security-y-jwt)
10. [API REST — Referencia completa de endpoints](#10-api-rest--referencia-completa-de-endpoints)
11. [Lógica de negocio — Máquina de estados del viaje](#11-lógica-de-negocio--máquina-de-estados-del-viaje)
12. [Manejo de errores](#12-manejo-de-errores)
13. [Datos precargados — data.sql](#13-datos-precargados--datasql)
14. [Sistema de simulación asíncrona](#14-sistema-de-simulación-asíncrona)
15. [Tests del backend](#15-tests-del-backend)
16. [Guía de arranque paso a paso](#16-guía-de-arranque-paso-a-paso)
17. [Limitaciones conocidas del MVP](#17-limitaciones-conocidas-del-mvp)
18. [Resultados de la test suite](#18-resultados-de-la-test-suite)

---

## 1. Visión general del proyecto

**PisPax** es una plataforma de gestión de transporte de mercancías que funciona como un "Uber para transporte de cargas". Conecta dos tipos de usuario con necesidades complementarias:

- **Cliente**: empresa o particular que necesita transportar mercancía. Crea solicitudes de viaje especificando origen, destino, tipo de mercancía y peso.
- **Transportista**: profesional con vehículo propio o flota que acepta los viajes disponibles y gestiona el trayecto actualizando el estado en tiempo real.

El backend de PisPax es el núcleo del sistema. Actúa como servidor central que expone una **API REST stateless** a la que tanto el cliente Android como herramientas de prueba (Postman, curl) pueden conectarse mediante peticiones HTTP estándar con autenticación JWT. No mantiene estado de sesión en servidor: toda la información de autenticación viaja en el token JWT adjunto a cada petición.

### Problema que resuelve

Muchas empresas y autónomos del sector transporte poseen vehículos que permanecen inactivos durante varias horas al día, generando costes sin producir ingresos. PisPax digitaliza y optimiza la asignación de esa capacidad ociosa, ofreciendo un sistema similar a OnTruck, Uber o Blablacar pero adaptado al sector de mercancías por carretera en el contexto español y europeo.

### Responsabilidades del backend

- Gestionar el registro y la autenticación de usuarios (JWT + BCrypt)
- Aplicar el control de acceso por rol (CLIENTE/TRANSPORTISTA) en cada endpoint
- Recibir y validar las solicitudes de viaje de los clientes
- Exponer los viajes disponibles a los transportistas
- Gestionar el ciclo completo del viaje a través de su máquina de estados
- Validar que el vehículo asignado tiene capacidad suficiente para el peso del viaje
- Persistir todos los datos en MySQL con integridad referencial completa
- Ejecutar la simulación automática de viajes para la demo del TFG

---

## 2. Stack tecnológico

La selección del stack se realizó priorizando la robustez, la integración nativa entre componentes y la coherencia con los conocimientos adquiridos en el ciclo DAM.

| Componente | Tecnología | Versión | Justificación |
|---|---|---|---|
| **Lenguaje** | Java | 17 (LTS) | Tipado fuerte, ecosistema maduro, coherente con el frontend Android |
| **Framework** | Spring Boot | 3.2.5 | Configuración por convención, servidor embebido, integración total del stack |
| **Seguridad** | Spring Security | 6.x (incluido en Boot 3.2) | Filtros HTTP, gestión de roles, integración con JWT |
| **Autenticación** | JWT (jjwt) | 0.12.5 | Stateless, móvil-friendly, información embebida en el token |
| **Base de datos** | MySQL | 8.0 | Relacional, soporte ACID, estándar en producción |
| **ORM** | Hibernate / Spring Data JPA | 6.4.4 | Abstracción de BD, queries tipadas, generación automática de esquema |
| **Pool de conexiones** | HikariCP | Incluido en Boot | Pool de alto rendimiento, configuración automática |
| **Reducción boilerplate** | Lombok | Última estable | Genera getters/setters/constructores automáticamente en compilación |
| **Construcción** | Maven | 3.9.x | Gestión de dependencias estándar, ciclo de vida de build |
| **Servidor embebido** | Apache Tomcat | 10.1.20 | Incluido en Spring Boot, no requiere instalación externa |
| **Validación** | Jakarta Bean Validation | Incluida en Boot | Anotaciones `@NotNull`, `@Size`, `@Min` en los DTOs de request |

### Dependencias Maven principales (pom.xml)

```xml
<!-- Spring Boot starter web (Tomcat + Spring MVC) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Spring Data JPA + Hibernate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Validación Bean Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MySQL JDBC Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- JWT (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 3. Estructura del proyecto y paquetes

El proyecto sigue la convención estándar de Maven para proyectos Spring Boot:

```
pispax-backend/
├── pom.xml                                    ← Dependencias y configuración Maven
└── src/
    └── main/
        ├── java/com/pixpax/app/
        │   │
        │   ├── PispaxApplication.java         ← Punto de entrada de la aplicación (main)
        │   │
        │   ├── config/
        │   │   ├── SecurityConfig.java        ← Reglas de seguridad HTTP, CORS, filtros
        │   │   └── DataInitializer.java       ← Usuarios demo al arranque (CommandLineRunner)
        │   │
        │   ├── controller/
        │   │   ├── AuthController.java        ← POST /api/auth/registro y /api/auth/login
        │   │   ├── ViajeController.java       ← GET/POST/PUT /api/viajes/**
        │   │   ├── VehiculoController.java    ← GET/POST /api/vehiculos/**
        │   │   └── TipoMercanciaController.java  ← GET /api/tipo-mercancia
        │   │
        │   ├── dto/
        │   │   ├── UsuarioDTO.java            ← Respuesta con datos del usuario (sin password)
        │   │   ├── ViajeDTO.java              ← Respuesta completa de un viaje
        │   │   ├── VehiculoDTO.java           ← Respuesta completa de un vehículo
        │   │   └── request/                  ← Objetos de entrada (cuerpo JSON de las peticiones)
        │   │       ├── LoginRequest.java
        │   │       ├── RegistroRequest.java
        │   │       ├── CrearViajeRequest.java
        │   │       ├── CrearVehiculoRequest.java
        │   │       ├── AceptarViajeRequest.java
        │   │       └── ActualizarEstadoRequest.java
        │   │
        │   ├── entity/                        ← Entidades JPA (mapeo directo con tablas MySQL)
        │   │   ├── Usuario.java
        │   │   ├── Vehiculo.java
        │   │   ├── Viaje.java
        │   │   ├── TipoMercancia.java
        │   │   ├── VehiculoTipoMercancia.java
        │   │   └── VehiculoTipoMercanciaId.java  ← Clave compuesta (vehiculo_id + tipo_mercancia_id)
        │   │
        │   ├── enums/                         ← Tipos enumerados del dominio
        │   │   ├── Rol.java                   ← CLIENTE, TRANSPORTISTA
        │   │   ├── EstadoViaje.java           ← PENDIENTE → ... → COMPLETADO / CANCELADO
        │   │   ├── TipoVehiculo.java          ← FURGONETA, FURGON_GRANDE, etc.
        │   │   ├── CarnetRequerido.java       ← B, C1, C, C_E
        │   │   └── CompatibilidadMercancia.java  ← SI, CON_REQUISITOS
        │   │
        │   ├── exception/
        │   │   ├── ErrorResponse.java         ← Formato JSON uniforme para todos los errores
        │   │   └── GlobalExceptionHandler.java   ← @RestControllerAdvice: captura global
        │   │
        │   ├── repository/                    ← Interfaces Spring Data JPA (acceso a BD)
        │   │   ├── UsuarioRepository.java
        │   │   ├── VehiculoRepository.java
        │   │   ├── ViajeRepository.java
        │   │   └── TipoMercanciaRepository.java
        │   │
        │   ├── security/
        │   │   ├── JwtUtil.java               ← Genera y valida tokens JWT
        │   │   ├── JwtAuthFilter.java         ← Filtro que intercepta cada petición HTTP
        │   │   └── AuthUtils.java             ← Helper: extrae userId del Authentication
        │   │
        │   └── service/
        │       ├── AuthService.java           ← Lógica de registro y login
        │       ├── ViajeService.java          ← Lógica de viajes y transiciones de estado
        │       ├── VehiculoService.java       ← Lógica CRUD de vehículos
        │       ├── TipoMercanciaService.java  ← Catálogo de tipos de mercancía
        │       └── SimulacionService.java     ← Simulación @Async para la demo del TFG
        │
        └── resources/
            ├── application.properties         ← Configuración de la aplicación
            └── data.sql                       ← Datos iniciales: 12 tipos de mercancía
```

### Convenciones de nomenclatura

El proyecto usa una convención mixta deliberada: **español para el dominio**, **inglés para los patrones técnicos**. Así `Viaje`, `Usuario`, `Vehiculo` son entidades del dominio en español, mientras que sus capas se nombran en inglés: `ViajeController`, `ViajeService`, `ViajeRepository`. Esto facilita la lectura del código para alguien que conoce el dominio del transporte español.

---

## 4. Patrón arquitectónico por capas

El backend sigue el patrón **MVC por capas** estándar de Spring Boot, con responsabilidades bien delimitadas:

```
Petición HTTP (Android/Postman)
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  CAPA DE SEGURIDAD (Spring Security + JwtAuthFilter)            │
│  Valida el JWT antes de que llegue al controller                │
│  Si el token es inválido: responde 401/403 sin llegar al código │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  CAPA DE CONTROLLER                                             │
│  Recibe la petición HTTP, extrae parámetros y body             │
│  Valida la entrada con @Valid                                   │
│  Delega la lógica al Service                                    │
│  Devuelve ResponseEntity con el DTO adecuado                   │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  CAPA DE SERVICE                                                │
│  Aplica toda la lógica de negocio                               │
│  Valida reglas: peso vs capacidad, transiciones de estado, etc. │
│  Llama al Repository para leer/escribir datos                   │
│  Nunca accede directamente a la BD: siempre a través de Repo   │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  CAPA DE REPOSITORY (Spring Data JPA)                           │
│  Interfaces que extienden JpaRepository<Entidad, Long>         │
│  Spring genera automáticamente la implementación SQL           │
│  Hibernate traduce las operaciones a SQL y ejecuta en MySQL    │
└─────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  MySQL 8.0 — Base de datos relacional                           │
│  5 tablas con integridad referencial (FOREIGN KEYS)            │
│  ENUMs nativos para estados                                     │
└─────────────────────────────────────────────────────────────────┘
```

### Principios de diseño aplicados

**Separación de responsabilidades**: Cada capa tiene una única responsabilidad. El Controller no contiene lógica de negocio. El Service no construye respuestas HTTP. El Repository no valida reglas de negocio.

**DTOs separados de las entidades**: Las entidades JPA (`@Entity`) nunca se exponen directamente en las respuestas HTTP. Se convierten siempre a DTOs que contienen solo los campos necesarios para el cliente. Esto protege campos internos (como `passwordHash`) y desacopla la API del modelo de datos interno.

**Validación siempre en Service**: Las validaciones de lógica de negocio (como "el peso del viaje no puede superar la capacidad del vehículo") se implementan en la capa Service, no en el Controller. Esto permite reutilizar la validación si en el futuro se añade otra forma de invocar la misma lógica.

---

## 5. Configuración y conexión a MySQL

### Archivo de configuración principal

**Ruta:** `src/main/resources/application.properties`

```properties
spring.application.name=pispax-backend

# ── BASE DE DATOS MYSQL ──────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/pispax?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=nairda04
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── JPA / HIBERNATE ──────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.open-in-view=false

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

# ── SIMULACIÓN (delays en ms) ─────────────────────────────────────────────────
simulacion.delay.aceptado=4000
simulacion.delay.salida-recogida=5000
simulacion.delay.llegada-recogida=4000
simulacion.delay.mercancia-recogida=5000
simulacion.delay.salida-entrega=6000
simulacion.delay.llegada-entrega=4000
simulacion.delay.completado=3000
```

### Desglose de la URL de conexión

```
jdbc:mysql://localhost:3306/pispax?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

| Fragmento | Significado |
|---|---|
| `jdbc:mysql://` | Protocolo JDBC para MySQL (el driver de MySQL habla este protocolo) |
| `localhost` | El servidor MySQL corre en la misma máquina que el backend (desarrollo local) |
| `3306` | Puerto estándar de MySQL. El servicio de Windows se llama MYSQL80 |
| `pispax` | Nombre de la base de datos a la que se conecta |
| `useSSL=false` | Desactiva TLS/SSL en la conexión (apropiado en desarrollo local) |
| `serverTimezone=UTC` | Evita errores de zona horaria entre Java y MySQL en Windows |
| `allowPublicKeyRetrieval=true` | Necesario con MySQL 8 cuando `useSSL=false` para intercambiar clave pública RSA |
| `characterEncoding=UTF-8` | Garantiza correcta codificación de caracteres especiales (tildes, ñ) |

### Parámetros de JPA/Hibernate

| Propiedad | Valor | Efecto |
|---|---|---|
| `ddl-auto=update` | update | Hibernate compara las entidades con las tablas existentes y aplica `ALTER TABLE` si hay diferencias. **No borra datos existentes.** |
| `show-sql=true` | true | Imprime cada consulta SQL generada por Hibernate en el log de la consola (esencial para depurar) |
| `format_sql=true` | true | Formatea el SQL impreso con indentación para facilitar su lectura |
| `dialect=MySQLDialect` | MySQLDialect | Indica a Hibernate que genere SQL específico para MySQL 8 (aunque Hibernate lo detecta automáticamente, se puede omitir esta línea) |
| `open-in-view=false` | false | Desactiva el patrón Open Session in View que puede causar lazy loading inesperado |
| `sql.init.mode=always` | always | Ejecuta `data.sql` en cada arranque |
| `defer-datasource-initialization=true` | true | Garantiza que `data.sql` se ejecuta DESPUÉS de que Hibernate haya creado/actualizado las tablas |

### Pool de conexiones HikariCP

HikariCP se configura automáticamente por Spring Boot. Crea un pool de ~10 conexiones reutilizables a MySQL al arrancar la aplicación. Las principales ventajas de un pool de conexiones son:

- **Reutilización**: se evita el coste de abrir y cerrar una conexión a MySQL en cada petición HTTP (una conexión MySQL tarda ~50-100ms en establecerse)
- **Límite de conexiones**: evita saturar el servidor MySQL con demasiadas conexiones simultáneas
- **Monitorización**: registra en el log cuando añade/elimina conexiones del pool

---

## 6. Ciclo de arranque de Spring Boot

Cuando se ejecuta `mvn spring-boot:run`, ocurre la siguiente secuencia en orden estricto:

```
1. JVM carga y ejecuta PispaxApplication.main()
         │
         ▼
2. Spring Boot lee y procesa application.properties
   → Inyecta las propiedades en todos los @Value y @ConfigurationProperties
         │
         ▼
3. HikariCP abre el pool de conexiones a MySQL
   → Usa la URL, usuario y contraseña de application.properties
   → Verifica conectividad abriendo la primera conexión
   → Log: "HikariPool-1 - Start completed."
         │
         ▼
4. Hibernate analiza todas las clases @Entity
   → Compara la estructura de las entidades con las tablas reales de MySQL
   → Con ddl-auto=update: aplica ALTER TABLE para columnas nuevas o cambiadas
   → NO borra tablas ni columnas existentes
         │
         ▼
5. Spring ejecuta data.sql
   → INSERT IGNORE INTO tipo_mercancia (12 registros)
   → El INSERT IGNORE evita duplicados si los datos ya existen
         │
         ▼
6. DataInitializer (CommandLineRunner) se ejecuta
   → Crea usuarios demo si no existen: demo.cliente@pispax.com y demo.transportista@pispax.com
   → Crea el vehículo demo del transportista si no existe
         │
         ▼
7. Spring Security configura la cadena de filtros
   → JwtAuthFilter se registra como filtro OncePerRequestFilter
   → Se configuran las reglas de acceso por ruta y rol
         │
         ▼
8. Apache Tomcat (servidor HTTP embebido) se levanta en el puerto 8080
   → Log: "Tomcat started on port 8080 (http)"
         │
         ▼
9. El log muestra: "Started PispaxApplication in X.XX seconds"
   → El servidor está listo para recibir peticiones
```

### Log de arranque esperado

```
HikariPool-1 - Adding connection com.mysql.cj.jdbc.ConnectionImpl@xxxxxxxx
HikariPool-1 - Start completed.
...
Hibernate: alter table viaje add column ...   ← solo si hay cambios de esquema
...
Tomcat started on port 8080 (http)
Started PispaxApplication in 2.82 seconds (process running for 3.015)
```

---

## 7. Modelo de datos — Entidades JPA

Las entidades JPA son clases Java anotadas con `@Entity` que Hibernate mapea directamente con tablas en MySQL. Cada campo de la clase corresponde a una columna de la tabla.

### 7.1 Entidad `Usuario`

Tabla: `usuario`

```java
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellidos", nullable = false, length = 150)
    private String apellidos;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;  // BCrypt hash — NUNCA texto plano

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

| Columna | Tipo MySQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único del usuario |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre del usuario |
| `apellidos` | VARCHAR(150) | NOT NULL | Apellidos del usuario |
| `email` | VARCHAR(200) | NOT NULL, UNIQUE | Email — se usa como login; irrepetible |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash BCrypt de la contraseña (60 chars) |
| `telefono` | VARCHAR(20) | NULL | Teléfono de contacto (opcional) |
| `rol` | ENUM | NOT NULL | `CLIENTE` o `TRANSPORTISTA` |
| `created_at` | DATETIME | DEFAULT NOW() | Fecha de creación automática |

### 7.2 Entidad `Vehiculo`

Tabla: `vehiculo`

```java
@Entity
@Table(name = "vehiculo")
public class Vehiculo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricula", nullable = false, unique = true, length = 15)
    private String matricula;      // Se guarda siempre en MAYÚSCULAS

    @Column(name = "marca", nullable = false, length = 80)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 80)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    @Column(name = "subtipo", length = 50)
    private String subtipo;        // Descripción adicional opcional (ej: "Caja cerrada")

    @Column(name = "tara_kg", precision = 8, scale = 2)
    private BigDecimal taraKg;     // Peso en vacío del vehículo

    @Column(name = "capacidad_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacidadKg; // Capacidad máxima de carga (clave para la validación)

    @Column(name = "mma_kg", precision = 8, scale = 2)
    private BigDecimal mmaKg;      // Masa Máxima Autorizada (tara + capacidad)

    @Enumerated(EnumType.STRING)
    @Column(name = "carnet_requerido", nullable = false)
    private CarnetRequerido carnetRequerido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Usuario transportista; // FK → usuario.id
}
```

| Columna | Tipo MySQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único del vehículo |
| `matricula` | VARCHAR(15) | NOT NULL, UNIQUE | Matrícula — única en todo el sistema |
| `marca` | VARCHAR(80) | NOT NULL | Marca del fabricante |
| `modelo` | VARCHAR(80) | NOT NULL | Modelo específico |
| `tipo_vehiculo` | ENUM | NOT NULL | Categoría del vehículo |
| `subtipo` | VARCHAR(50) | NULL | Subclasificación libre (ej: "Frigorífico") |
| `tara_kg` | DECIMAL(8,2) | NULL | Peso en vacío (opcional) |
| `capacidad_kg` | DECIMAL(8,2) | NOT NULL | Capacidad máxima de carga **— clave para validación** |
| `mma_kg` | DECIMAL(8,2) | NULL | Masa Máxima Autorizada según normativa |
| `carnet_requerido` | ENUM | NOT NULL | Tipo de carnet requerido para conducir |
| `transportista_id` | BIGINT | FK → usuario.id, NOT NULL | Transportista propietario del vehículo |

### 7.3 Entidad `Viaje`

Tabla: `viaje` — La entidad central del sistema operativo.

```java
@Entity
@Table(name = "viaje")
public class Viaje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Usuario transportista;  // NULL hasta que se acepta el viaje

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;      // NULL hasta que se acepta el viaje

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_mercancia_id")
    private TipoMercancia tipoMercancia;

    @Column(name = "descripcion_mercancia", length = 500)
    private String descripcionMercancia;

    @Column(name = "direccion_recogida", nullable = false, length = 300)
    private String direccionRecogida;

    @Column(name = "direccion_entrega", nullable = false, length = 300)
    private String direccionEntrega;

    @Column(name = "peso_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoViaje estado = EstadoViaje.PENDIENTE; // Valor por defecto al crear

    @Column(name = "fecha_solicitud")
    @CreationTimestamp
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;   // Se rellena al pasar a ACEPTADO

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;     // Se rellena al COMPLETAR o CANCELAR
}
```

| Columna | Tipo MySQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único del viaje |
| `cliente_id` | BIGINT | FK → usuario.id, NOT NULL | Cliente que solicita el viaje |
| `transportista_id` | BIGINT | FK → usuario.id, **NULL** | Transportista asignado — vacío hasta aceptación |
| `vehiculo_id` | BIGINT | FK → vehiculo.id, **NULL** | Vehículo asignado — vacío hasta aceptación |
| `tipo_mercancia_id` | BIGINT | FK → tipo_mercancia.id | Tipo de mercancía del catálogo |
| `descripcion_mercancia` | VARCHAR(500) | NULL | Descripción libre de la carga |
| `direccion_recogida` | VARCHAR(300) | NOT NULL | Dirección de origen |
| `direccion_entrega` | VARCHAR(300) | NOT NULL | Dirección de destino |
| `peso_kg` | DECIMAL(8,2) | NOT NULL, > 0 | Peso de la mercancía en kilogramos |
| `estado` | ENUM | NOT NULL, DEFAULT PENDIENTE | Estado actual del ciclo de vida del viaje |
| `fecha_solicitud` | DATETIME | DEFAULT NOW() | Timestamp automático al crear el viaje |
| `fecha_inicio` | DATETIME | NULL | Se registra al pasar a ACEPTADO |
| `fecha_fin` | DATETIME | NULL | Se registra al pasar a COMPLETADO o CANCELADO |

### 7.4 Entidad `TipoMercancia`

Tabla: `tipo_mercancia` — Catálogo precargado de 12 tipos.

| Columna | Tipo MySQL | Restricciones | Descripción |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL, UNIQUE | Nombre visible para el usuario ("Paquetería general") |
| `nombre_db` | VARCHAR(100) | NOT NULL, UNIQUE | Identificador interno snake_case ("paqueteria_general") |
| `descripcion` | VARCHAR(500) | NULL | Descripción del tipo de mercancía |

El campo `nombre_db` desacopla el nombre de presentación del identificador técnico, lo que permite cambiar el nombre visible sin afectar a la lógica de la API.

### 7.5 Entidad `VehiculoTipoMercancia`

Tabla: `vehiculo_tipo_mercancia` — Relación N:M entre vehículos y tipos de mercancía.

| Columna | Tipo MySQL | Restricciones | Descripción |
|---|---|---|---|
| `vehiculo_id` | BIGINT | PK (compuesta), FK → vehiculo.id | ID del vehículo |
| `tipo_mercancia_id` | BIGINT | PK (compuesta), FK → tipo_mercancia.id | ID del tipo de mercancía |
| `compatible` | ENUM | NOT NULL, DEFAULT 'SI' | `SI` = compatible sin condiciones, `CON_REQUISITOS` = requiere validación adicional |

La clave primaria compuesta `(vehiculo_id, tipo_mercancia_id)` garantiza que no puede haber dos registros para la misma combinación vehículo-mercancía.

---

## 8. ENUMs del dominio

Los ENUMs se definen en Java y Hibernate los persiste en MySQL como columnas de tipo `ENUM`.

### `Rol.java`
```java
public enum Rol {
    CLIENTE,
    TRANSPORTISTA
}
```
Determina el tipo de usuario y controla el acceso a los endpoints.

### `EstadoViaje.java`
```java
public enum EstadoViaje {
    PENDIENTE,           // Viaje creado por cliente, esperando aceptación
    ACEPTADO,            // Transportista ha aceptado, vehículo asignado
    SALIDA_RECOGIDA,     // Transportista sale hacia el punto de recogida
    LLEGADA_RECOGIDA,    // Transportista llega al punto de recogida
    MERCANCIA_RECOGIDA,  // Mercancía cargada en el vehículo
    SALIDA_ENTREGA,      // Transportista sale hacia el destino de entrega
    LLEGADA_ENTREGA,     // Transportista llega al destino
    COMPLETADO,          // Entrega confirmada, viaje finalizado
    CANCELADO            // Viaje cancelado (transición lateral posible desde cualquier estado)
}
```

### `TipoVehiculo.java`
```java
public enum TipoVehiculo {
    FURGONETA,          // Hasta 3.500 kg MMA, carnet B
    FURGON_GRANDE,      // Hasta 3.500 kg MMA, carnet B
    FRIGORIFICO,        // Temperatura controlada, hasta 3.500 kg, carnet B
    CAMION_LIGERO,      // 3.500–7.500 kg MMA, carnet C1
    CAMION_PESADO,      // > 7.500 kg MMA, carnet C + CAP
    CAMION_ARTICULADO,  // Hasta 42.000 kg MMA, carnet C+E + CAP
    PLATAFORMA          // Cargas voluminosas, maquinaria, carnet C1/C
}
```

### `CarnetRequerido.java`
```java
public enum CarnetRequerido {
    B,    // Estándar hasta 3.500 kg MMA
    C1,   // 3.500–7.500 kg MMA, formación adicional
    C,    // > 7.500 kg, requiere CAP
    C_E   // Articulado > 750 kg remolque, requiere C previo y CAP
}
```
Nota: en la base de datos se guarda como `C_E` pero en la UI del frontend se muestra como `C+E`.

### `CompatibilidadMercancia.java`
```java
public enum CompatibilidadMercancia {
    SI,              // Compatible sin condiciones
    CON_REQUISITOS   // Compatible pero requiere validación adicional (ADR, frío, GDP...)
}
```

---

## 9. Seguridad — Spring Security y JWT

### ¿Por qué JWT en lugar de sesiones?

Las aplicaciones web tradicionales usan sesiones en servidor: al hacer login, el servidor crea una sesión en memoria/BD y devuelve una cookie con el ID de sesión. En cada petición siguiente debe buscar esa sesión. Esto tiene un problema: el servidor guarda estado, lo que dificulta el escalado horizontal (si hay múltiples instancias del backend, deben compartir las sesiones).

JWT (JSON Web Token) elimina este problema. Toda la información necesaria (userId, rol) va **dentro del propio token**, firmado criptográficamente. El servidor no guarda nada. Cada petición es autocontenida (stateless). Ventajas específicas para PisPax:

- El backend puede escalar horizontalmente sin sincronizar sesiones
- El token se guarda en `SharedPreferences` del Android y se envía en cada petición
- El token ya lleva `userId` y `rol`, eliminando una consulta a BD en cada petición autenticada

### Anatomía de un token JWT

Un token JWT tiene tres partes separadas por puntos (`.`):

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSIsInVzZXJJZCI6MSwicm9sIjoiQ0xJRU5URSIsImlhdCI6MTc0ODQ1Njc4OSwiZXhwIjoxNzQ4NTQzMTg5fQ.XyZ_firma
│─────────────────│.│────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────│.│──────────────│
      HEADER                                                     PAYLOAD                                                                                              SIGNATURE
```

**HEADER** (decodificado):
```json
{ "alg": "HS256" }
```

**PAYLOAD** en PisPax (decodificado):
```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

| Campo | Descripción |
|---|---|
| `sub` | Subject estándar JWT — email del usuario |
| `userId` | Custom claim — ID del usuario en MySQL (lo leen los controllers) |
| `rol` | Custom claim — `CLIENTE` o `TRANSPORTISTA` (controla el acceso a endpoints) |
| `iat` | Issued at — timestamp Unix de cuándo se generó el token |
| `exp` | Expiration — timestamp Unix de cuándo caduca (`iat + 86400000ms = iat + 24h`) |

**SIGNATURE**: `HMAC-SHA256(base64(header) + "." + base64(payload), clave_secreta)`. Solo quien conoce la clave secreta del servidor puede generar una firma válida. Si alguien manipula el payload (ej: cambia `"rol":"CLIENTE"` a `"TRANSPORTISTA"`), la firma no coincide y el servidor rechaza el token.

> **Importante**: el payload está en Base64, no cifrado. Es visible para cualquiera que tenga el token. Por eso NUNCA debe incluirse información sensible (contraseñas, datos personales completos). Lo que garantiza JWT es la **integridad** (que nadie ha modificado el contenido), no la confidencialidad.

### Flujo completo de autenticación

```
Cliente Android / Postman                      Backend PisPax
        │                                              │
        │  POST /api/auth/login                        │
        │  {"email":"...", "password":"..."}           │
        │ ─────────────────────────────────────────►  │
        │                                              │ 1. Busca usuario por email en BD
        │                                              │ 2. BCrypt.matches(password, hash)
        │                                              │ 3. Si OK: genera JWT con userId y rol
        │  200 OK                                      │
        │  {"token": "eyJ...", "usuario":{...}}        │
        │ ◄─────────────────────────────────────────   │
        │                                              │
        │  [Guarda token en SharedPreferences]         │
        │                                              │
        │  GET /api/viajes                             │
        │  Authorization: Bearer eyJhbGci...           │
        │ ─────────────────────────────────────────►  │
        │                                              │ 4. JwtAuthFilter intercepta la petición
        │                                              │ 5. Lee header "Authorization"
        │                                              │ 6. Extrae el token ("Bearer " + token)
        │                                              │ 7. Verifica firma HMAC-SHA256
        │                                              │ 8. Comprueba que no ha caducado (exp)
        │                                              │ 9. Extrae email, userId, rol del payload
        │                                              │ 10. Inyecta autenticación en SecurityContext
        │                                              │ 11. La petición llega al Controller
        │                                              │ 12. Controller lee userId de auth.getCredentials()
        │  200 OK [lista de viajes]                    │
        │ ◄─────────────────────────────────────────   │
```

### Implementación: JwtUtil.java

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Genera el token JWT firmado con HMAC-SHA256
    public String generateToken(String email, Long userId, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    // Verifica la firma y parsea todos los claims del token
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token)  { return extractClaims(token).getSubject(); }
    public Long extractUserId(String token)   { return extractClaims(token).get("userId", Long.class); }
    public String extractRol(String token)    { return extractClaims(token).get("rol", String.class); }

    public boolean isTokenValid(String token) {
        try {
            return !extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false; // Firma inválida, token malformado, o caducado
        }
    }
}
```

### Implementación: JwtAuthFilter.java

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay cabecera Authorization o no empieza por "Bearer ", continúa sin autenticar
        // Spring Security decidirá si el endpoint requiere auth o no
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // Quita "Bearer "

        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response); // Token inválido → pasa sin autenticar
            return;
        }

        // Token válido: extrae datos y construye el objeto de autenticación de Spring Security
        String email   = jwtUtil.extractEmail(token);
        String rol     = jwtUtil.extractRol(token);
        Long   userId  = jwtUtil.extractUserId(token);

        // authorities = ["ROLE_CLIENTE"] o ["ROLE_TRANSPORTISTA"]
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

        // principal = email, credentials = userId (así lo leen los controllers con auth.getCredentials())
        var auth = new UsernamePasswordAuthenticationToken(email, userId, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
```

### Configuración de seguridad por endpoint

Definida en `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // ← Habilita @PreAuthorize en los controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()     // Registro y login: sin token
                .requestMatchers("/api/tipo-mercancia").permitAll() // Catálogo: público
                .anyRequest().authenticated()                    // Todo lo demás: requiere token
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

Adicionalmente, los endpoints con restricción de rol usan `@PreAuthorize`:

```java
// Solo transportistas pueden ver viajes disponibles
@GetMapping("/disponibles")
@PreAuthorize("hasRole('TRANSPORTISTA')")
public ResponseEntity<List<ViajeDTO>> getDisponibles(Authentication auth) { ... }

// Solo clientes pueden crear viajes
@PostMapping
@PreAuthorize("hasRole('CLIENTE')")
public ResponseEntity<ViajeDTO> crearViaje(Authentication auth, @Valid @RequestBody CrearViajeRequest req) { ... }
```

### Tabla de acceso por rol

| Endpoint | Sin token | CLIENTE | TRANSPORTISTA |
|---|---|---|---|
| `POST /api/auth/registro` | ✅ 201 | — | — |
| `POST /api/auth/login` | ✅ 200 | — | — |
| `GET /api/tipo-mercancia` | ✅ 200 | ✅ 200 | ✅ 200 |
| `GET /api/viajes` | ❌ 403 | ✅ (sus viajes) | ✅ (sus viajes) |
| `POST /api/viajes` | ❌ 403 | ✅ 201 | ❌ 403 |
| `GET /api/viajes/disponibles` | ❌ 403 | ❌ 403 | ✅ 200 |
| `GET /api/viajes/{id}` | ❌ 403 | ✅ (si es suyo) | ✅ (si es suyo o PENDIENTE) |
| `PUT /api/viajes/{id}/aceptar` | ❌ 403 | ❌ 403 | ✅ 200 |
| `PUT /api/viajes/{id}/estado` | ❌ 403 | ❌ 403 | ✅ 200 |
| `GET /api/vehiculos/mis-vehiculos` | ❌ 403 | ❌ 403 | ✅ 200 |
| `POST /api/vehiculos` | ❌ 403 | ❌ 403 | ✅ 201 |
| `POST /api/viajes/{id}/simular` | ❌ 403 | ❌ 403 | ✅ 200 |

### BCrypt para contraseñas

Las contraseñas nunca se guardan en texto plano. Spring Security incluye `BCryptPasswordEncoder`:

```java
// Al registrar: se genera un hash BCrypt
String hash = passwordEncoder.encode("miPassword123");
// Resultado: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKr.VJsgG"

// Al hacer login: se compara sin necesitar la sal por separado
boolean coincide = passwordEncoder.matches("miPassword123", hashGuardado); // true
```

BCrypt incorpora automáticamente una "sal" aleatoria en cada hash, por lo que el mismo password siempre genera un hash diferente. Esto hace imposible un ataque por tabla rainbow.

---

## 10. API REST — Referencia completa de endpoints

Todos los endpoints tienen el prefijo base `/api/`. El servidor escucha en `http://localhost:8080`.

### Grupo AUTH: `/api/auth`

#### `POST /api/auth/registro` — Público (sin token)

Registra un nuevo usuario en el sistema. No requiere autenticación.

**Body JSON:**
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

Validaciones aplicadas:
- `nombre`: obligatorio, máximo 100 caracteres
- `apellidos`: obligatorio, máximo 150 caracteres
- `email`: obligatorio, formato válido (contiene @), único en BD
- `password`: obligatorio, mínimo 8 caracteres
- `telefono`: opcional, formato `^[0-9+\s()-]{7,20}$`
- `rol`: obligatorio, debe ser exactamente `"CLIENTE"` o `"TRANSPORTISTA"`

**Respuesta 201 Created:**
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
El campo `password` NO aparece en la respuesta — el DTO lo filtra.

**Errores:**
- `400` — Email duplicado: `{"mensaje": "Ya existe un usuario con ese email", "codigo": 400}`
- `400` — Campos inválidos: `{"mensaje": "password: La contraseña debe tener al menos 8 caracteres", "codigo": 400}`

---

#### `POST /api/auth/login` — Público (sin token)

Autentica un usuario y devuelve el token JWT.

**Body JSON:**
```json
{
  "email": "maria@ejemplo.com",
  "password": "MiPass123"
}
```

**Respuesta 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJpYUBlamVtcGxvLmNvbSIsInVzZXJJZCI6MSwicm9sIjoiQ0xJRU5URSIsImlhdCI6MTc0ODQ1Njc4OSwiZXhwIjoxNzQ4NTQzMTg5fQ.signature",
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

**Errores:**
- `400` — Credenciales incorrectas (email no existe o password mal): `{"mensaje": "Credenciales incorrectas", "codigo": 400}`. Se devuelve el mismo mensaje en ambos casos para no revelar si el email existe.

---

### Grupo TIPO-MERCANCIA: `/api/tipo-mercancia`

#### `GET /api/tipo-mercancia` — Público (sin token)

Devuelve el catálogo completo de 12 tipos de mercancía. Este endpoint es público porque el cliente necesita ver los tipos disponibles antes de hacer login.

**Respuesta 200 OK:**
```json
[
  { "id": 1,  "nombre": "Paquetería general",          "descripcion": "Paquetes, cajas, envíos estándar" },
  { "id": 2,  "nombre": "Electrónica frágil",          "descripcion": "Dispositivos frágiles, equipos electrónicos" },
  { "id": 3,  "nombre": "Alimentos frescos",           "descripcion": "Productos perecederos (0–8°C), requiere frío" },
  { "id": 4,  "nombre": "Alimentos congelados",        "descripcion": "Productos congelados (< -18°C)" },
  { "id": 5,  "nombre": "Materiales de construcción",  "descripcion": "Cemento, acero, materiales pesados" },
  { "id": 6,  "nombre": "Mobiliario",                  "descripcion": "Muebles, sofás, piezas grandes" },
  { "id": 7,  "nombre": "Mercancía peligrosa (ADR)",   "descripcion": "Sustancias químicas, inflamables, tóxicas" },
  { "id": 8,  "nombre": "Productos farmacéuticos",     "descripcion": "Medicinas, biologics — GDP recomendado" },
  { "id": 9,  "nombre": "Textil y moda",               "descripcion": "Ropa, telas, calzado" },
  { "id": 10, "nombre": "Maquinaria industrial",       "descripcion": "Máquinas, equipos pesados" },
  { "id": 11, "nombre": "Documentación y archivo",     "descripcion": "Documentos, libros, archivos" },
  { "id": 12, "nombre": "Vehículos y automoción",      "descripcion": "Coches, motos, piezas vehiculares" }
]
```

---

### Grupo VEHÍCULOS: `/api/vehiculos` — Solo TRANSPORTISTA

#### `GET /api/vehiculos/mis-vehiculos`

Devuelve todos los vehículos del transportista autenticado.

**Headers:** `Authorization: Bearer {token_transportista}`

**Respuesta 200 OK:**
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

Registra un nuevo vehículo para el transportista autenticado.

**Headers:** `Authorization: Bearer {token_transportista}`

**Body JSON:**
```json
{
  "matricula": "5678XYZ",
  "marca": "Mercedes",
  "modelo": "Sprinter 316",
  "tipoVehiculo": "FURGON_GRANDE",
  "subtipo": "Frigorífico",
  "taraKg": 2100,
  "capacidadKg": 1200,
  "mmaKg": 3500,
  "carnetRequerido": "B"
}
```

Valores válidos:
- `tipoVehiculo`: `FURGONETA`, `FURGON_GRANDE`, `FRIGORIFICO`, `CAMION_LIGERO`, `CAMION_PESADO`, `CAMION_ARTICULADO`, `PLATAFORMA`
- `carnetRequerido`: `B`, `C1`, `C`, `C_E`
- `subtipo`, `taraKg`, `mmaKg`: opcionales

**Respuesta 201 Created:** VehiculoDTO (mismo formato que GET)

**Errores:**
- `403` — Si el token es de un CLIENTE
- `400` — Matrícula duplicada (constraint UNIQUE en BD)

---

### Grupo VIAJES: `/api/viajes` — Mixto (por rol)

#### `GET /api/viajes`

Lista los viajes del usuario autenticado. El comportamiento varía según el rol:
- **CLIENTE**: devuelve los viajes que ese cliente ha creado
- **TRANSPORTISTA**: devuelve los viajes que ese transportista ha aceptado

**Headers:** `Authorization: Bearer {token}`

**Respuesta 200 OK:** Array de ViajeDTO.

---

#### `GET /api/viajes/disponibles` — Solo TRANSPORTISTA

Devuelve todos los viajes en estado `PENDIENTE` disponibles para que el transportista los acepte.

**Headers:** `Authorization: Bearer {token_transportista}`

**Respuesta 200 OK:** Array de ViajeDTO con `estado: "PENDIENTE"`.

**Errores:**
- `403` — Si el token es de un CLIENTE

---

#### `GET /api/viajes/{id}`

Detalle completo de un viaje específico.
- **CLIENTE**: solo puede ver los viajes donde él es el cliente
- **TRANSPORTISTA**: puede ver sus viajes asignados y cualquier viaje en PENDIENTE

**Respuesta 200 OK — ViajeDTO completo:**
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
  "descripcionMercancia": "12 palés ropa deportiva temporada verano",
  "direccionRecogida": "Polígono Industrial Can Fontanet, Terrassa, Barcelona",
  "direccionEntrega": "Centro Logístico Mercamadrid, Madrid",
  "pesoKg": 480.00,
  "estado": "ACEPTADO",
  "fechaSolicitud": "2026-06-03T10:00:00",
  "fechaInicio": "2026-06-03T10:05:00",
  "fechaFin": null
}
```

**Errores:**
- `404` — Viaje no encontrado

---

#### `POST /api/viajes` — Solo CLIENTE

Crea una nueva solicitud de transporte. El viaje se crea siempre en estado `PENDIENTE`.

**Headers:** `Authorization: Bearer {token_cliente}`

**Body JSON:**
```json
{
  "tipoMercanciaId": 1,
  "descripcionMercancia": "Descripción opcional de la carga",
  "direccionRecogida": "Calle Mayor 1, Madrid",
  "direccionEntrega": "Avenida Diagonal 200, Barcelona",
  "pesoKg": 480
}
```

- `tipoMercanciaId`: ID del tipo del catálogo (1-12)
- `pesoKg`: obligatorio, mayor que 0
- `descripcionMercancia`: opcional

**Respuesta 201 Created:** ViajeDTO con `estado: "PENDIENTE"`, `transportistaId: null`, `vehiculoId: null`.

**Errores:**
- `403` — Si el token es de un TRANSPORTISTA
- `400` — Campos obligatorios vacíos o tipo de mercancía no encontrado

---

#### `PUT /api/viajes/{id}/aceptar` — Solo TRANSPORTISTA

El transportista acepta un viaje PENDIENTE asignándole uno de sus vehículos.

**Headers:** `Authorization: Bearer {token_transportista}`

**Body JSON:**
```json
{
  "vehiculoId": 11
}
```

**Validaciones:**
1. El viaje debe estar en estado `PENDIENTE`
2. El vehículo debe pertenecer al transportista autenticado
3. El `pesoKg` del viaje no puede superar la `capacidadKg` del vehículo

Al aceptar: se asigna el transportista y el vehículo al viaje, el estado pasa a `ACEPTADO` y se registra `fechaInicio`.

**Respuesta 200 OK:** ViajeDTO con `estado: "ACEPTADO"` y `fechaInicio` rellenada.

**Errores:**
- `409 Conflict` — Viaje no está en PENDIENTE: `{"mensaje": "Solo se pueden aceptar viajes en estado PENDIENTE", "codigo": 409}`
- `400 Bad Request` — Peso excede capacidad: `{"mensaje": "El peso del viaje excede la capacidad del vehículo", "codigo": 400}`
- `400 Bad Request` — Vehículo ajeno: `{"mensaje": "El vehículo no pertenece a este transportista", "codigo": 400}`

---

#### `PUT /api/viajes/{id}/estado` — Solo TRANSPORTISTA asignado

Avanza el estado del viaje al siguiente en la secuencia. Solo el transportista asignado al viaje puede llamar a este endpoint.

**Headers:** `Authorization: Bearer {token_transportista}`

**Body JSON:**
```json
{
  "nuevoEstado": "SALIDA_RECOGIDA"
}
```

Estados válidos: `SALIDA_RECOGIDA`, `LLEGADA_RECOGIDA`, `MERCANCIA_RECOGIDA`, `SALIDA_ENTREGA`, `LLEGADA_ENTREGA`, `COMPLETADO`, `CANCELADO`.

**Respuesta 200 OK:** ViajeDTO actualizado.

**Errores:**
- `409 Conflict` — Transición inválida: `{"mensaje": "Transición inválida: ACEPTADO → COMPLETADO", "codigo": 409}`
- `403 Forbidden` — El transportista no es el asignado: `{"mensaje": "No tienes permiso para actualizar este viaje", "codigo": 403}`
- `409 Conflict` — Cancelar un viaje ya finalizado: `{"mensaje": "No se puede cancelar un viaje ya finalizado", "codigo": 409}`

---

#### `POST /api/viajes/{id}/simular` — Solo TRANSPORTISTA asignado

Inicia la simulación automática del viaje para la demo del TFG. El backend avanza los estados con delays configurables en `application.properties`. Responde inmediatamente (200 OK) mientras el proceso corre en segundo plano con `@Async`.

---

## 11. Lógica de negocio — Máquina de estados del viaje

El viaje atraviesa una secuencia lineal de estados. Las transiciones son estrictamente secuenciales: no se puede saltar ni retroceder. En cualquier punto antes del estado final, se puede ir a `CANCELADO`.

```
PENDIENTE
    │
    │ (transportista llama a PUT /aceptar con vehiculoId)
    ▼
ACEPTADO                    ← fechaInicio se registra aquí
    │
    │ (transportista llama a PUT /estado)
    ▼
SALIDA_RECOGIDA             ← Transportista sale hacia el punto de recogida
    │
    ▼
LLEGADA_RECOGIDA            ← Transportista llega al punto de recogida
    │
    ▼
MERCANCIA_RECOGIDA          ← La mercancía ha sido cargada en el vehículo
    │
    ▼
SALIDA_ENTREGA              ← Transportista sale hacia el destino
    │
    ▼
LLEGADA_ENTREGA             ← Transportista llega al destino
    │
    ▼
COMPLETADO                  ← Entrega confirmada ← fechaFin se registra aquí

Desde cualquier estado (excepto COMPLETADO y CANCELADO):
    └──► CANCELADO          ← fechaFin se registra aquí
```

### Implementación en ViajeService.java

```java
// Secuencia de estados normales (en orden)
private static final List<EstadoViaje> FLUJO_NORMAL = List.of(
    EstadoViaje.PENDIENTE,
    EstadoViaje.ACEPTADO,
    EstadoViaje.SALIDA_RECOGIDA,
    EstadoViaje.LLEGADA_RECOGIDA,
    EstadoViaje.MERCANCIA_RECOGIDA,
    EstadoViaje.SALIDA_ENTREGA,
    EstadoViaje.LLEGADA_ENTREGA,
    EstadoViaje.COMPLETADO
);

private void validarTransicion(EstadoViaje actual, EstadoViaje nuevo) {
    // Cancelación permitida desde cualquier estado excepto los finales
    if (nuevo == EstadoViaje.CANCELADO) {
        if (actual == EstadoViaje.COMPLETADO || actual == EstadoViaje.CANCELADO) {
            throw new IllegalStateException("No se puede cancelar un viaje ya finalizado");
        }
        return; // OK cancelar
    }

    int indexActual = FLUJO_NORMAL.indexOf(actual);
    int indexNuevo  = FLUJO_NORMAL.indexOf(nuevo);

    // La transición válida es siempre al siguiente estado (indexNuevo = indexActual + 1)
    if (indexNuevo != indexActual + 1) {
        throw new IllegalStateException("Transición inválida: " + actual + " → " + nuevo);
    }
}
```

---

## 12. Manejo de errores

Todas las excepciones se centralizan en `GlobalExceptionHandler.java` anotado con `@RestControllerAdvice`. Esto garantiza que cualquier excepción lanzada en cualquier capa del backend resulta siempre en una respuesta JSON con el formato uniforme:

```json
{
  "mensaje": "Descripción legible del error",
  "codigo": 400
}
```

| Excepción | Código HTTP | Cuándo ocurre |
|---|---|---|
| `MethodArgumentNotValidException` | 400 Bad Request | Validación de `@Valid` falla (campo vacío, formato incorrecto) |
| `IllegalArgumentException` | 400 Bad Request | Email duplicado, credenciales incorrectas, vehículo de otro transportista |
| `IllegalStateException` | 409 Conflict | Transición de estado inválida, viaje no en PENDIENTE al aceptar |
| `ResponseStatusException(404)` | 404 Not Found | Recurso no existe (viaje, usuario, vehículo) |
| `ResponseStatusException(403)` | 403 Forbidden | Acceso a recurso ajeno |
| `AccessDeniedException` | 403 Forbidden | Rol incorrecto para el endpoint (Spring Security) |
| `RuntimeException` (resto) | 500 Internal Server Error | Error inesperado del servidor |

---

## 13. Datos precargados — data.sql

**Ruta:** `src/main/resources/data.sql`

Se ejecuta automáticamente en cada arranque, después de que Hibernate crea/actualiza las tablas. El `INSERT IGNORE` evita duplicados si ya existen los datos:

```sql
INSERT IGNORE INTO tipo_mercancia (id, nombre, nombre_db, descripcion) VALUES
(1,  'Paquetería general',         'paqueteria_general',     'Paquetes, cajas, envíos estándar'),
(2,  'Electrónica frágil',         'electronica_fragil',     'Dispositivos frágiles, equipos electrónicos'),
(3,  'Alimentos frescos',          'alimentos_frescos',      'Productos perecederos (0–8°C), requiere frío'),
(4,  'Alimentos congelados',       'alimentos_congelados',   'Productos congelados (< -18°C)'),
(5,  'Materiales de construcción', 'materiales_construccion','Cemento, acero, materiales pesados'),
(6,  'Mobiliario',                 'mobiliario',             'Muebles, sofás, piezas grandes'),
(7,  'Mercancía peligrosa (ADR)',  'mercancia_peligrosa',    'Sustancias químicas, inflamables, tóxicas'),
(8,  'Productos farmacéuticos',    'productos_farmaceuticos','Medicinas, biologics — GDP recomendado'),
(9,  'Textil y moda',              'textil_moda',            'Ropa, telas, calzado'),
(10, 'Maquinaria industrial',      'maquinaria_industrial',  'Máquinas, equipos pesados'),
(11, 'Documentación y archivo',    'documentacion_archivo',  'Documentos, libros, archivos'),
(12, 'Vehículos y automoción',     'vehiculos_automocion',   'Coches, motos, piezas vehiculares');
```

Los usuarios y el vehículo de demo se crean por `DataInitializer` (CommandLineRunner) al arranque:

```
demo.cliente@pispax.com       / Demo1234  (CLIENTE)
demo.transportista@pispax.com / Demo1234  (TRANSPORTISTA)
Vehículo demo: Renault Master · Matrícula 1234ABC · 1500 kg · B
```

---

## 14. Sistema de simulación asíncrona

La simulación permite demostrar el ciclo completo de un viaje en ~38 segundos durante la demo del TFG, sin necesidad de un vehículo real.

### Arquitectura

```
Android: POST /api/viajes/{id}/simular
         │
         │ (Backend responde 200 OK inmediatamente)
         │
         └── SimulacionService @Async (corre en segundo plano)
              ├── Thread.sleep(4000) → ACEPTADO
              ├── Thread.sleep(5000) → SALIDA_RECOGIDA
              ├── Thread.sleep(4000) → LLEGADA_RECOGIDA
              ├── Thread.sleep(5000) → MERCANCIA_RECOGIDA
              ├── Thread.sleep(6000) → SALIDA_ENTREGA
              ├── Thread.sleep(4000) → LLEGADA_ENTREGA
              └── Thread.sleep(3000) → COMPLETADO
                            Total: ~38 segundos

Android: polling GET /api/viajes/{id} cada 3 segundos
         → Detecta cambios de estado
         → Actualiza el mapa y el timeline en tiempo real
```

El endpoint `/simular` debe estar protegido por JWT y solo el transportista asignado al viaje puede iniciarlo. Si el viaje no está en estado PENDIENTE o ACEPTADO, el backend responde 409.

---

## 15. Tests del backend

### 15.1 Tests unitarios de Service (con Mockito)

**AuthService:**
- Registro exitoso de cliente y transportista
- Registro fallido si el email ya existe → lanza IllegalArgumentException
- Login exitoso devuelve token JWT con userId y rol
- Login fallido con contraseña incorrecta → lanza IllegalArgumentException
- Login fallido si el usuario no existe → lanza IllegalArgumentException

**ViajeService:**
- Crear viaje exitoso (rol CLIENTE) → estado PENDIENTE
- Error al crear viaje con tipo de mercancía inexistente
- Aceptar viaje exitoso → estado ACEPTADO, fechaInicio rellenada
- Error al aceptar si peso supera capacidad del vehículo
- Error al aceptar si el vehículo no pertenece al transportista
- Todas las transiciones de estado válidas (PENDIENTE → ACEPTADO → ... → COMPLETADO)
- Error en transición inválida (ej: PENDIENTE → COMPLETADO) → IllegalStateException
- Cancelación válida desde estado intermedio
- Error: cancelar viaje ya COMPLETADO → IllegalStateException

### 15.2 Tests de integración con H2 en memoria

Para tests de integración sin depender de MySQL:

```xml
<!-- En pom.xml, scope test -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
```

### 15.3 Tests de endpoints con MockMvc

```java
@Test
void registro_retorna201() {
    mockMvc.perform(post("/api/auth/registro")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"nombre":"Test","apellidos":"User","email":"test@test.com",
                     "password":"Password1","rol":"CLIENTE"}"""))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rol").value("CLIENTE"));
}

@Test
void crearViaje_sinToken_retorna403() {
    mockMvc.perform(post("/api/viajes"))
        .andExpect(status().isForbidden());
}
```

### 15.4 Resultados de la test suite (49 tests)

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

## 16. Guía de arranque paso a paso

### Requisitos del entorno

| Herramienta | Versión | Ruta en la máquina |
|---|---|---|
| Java JDK | 17 | `C:\Dependencias\jdk-17.0.12` |
| Maven | 3.9.x | `C:\Dependencias\apache-maven-3.9.14` |
| MySQL | 8.0 | `C:\Program Files\MySQL\bin\` |
| Servicio Windows | MYSQL80 | — |

### Paso 1 — Arrancar MySQL

```powershell
# PowerShell como Administrador
Start-Service MYSQL80

# Verificar que está corriendo
(Get-Service MYSQL80).Status
# Debe mostrar: Running
```

### Paso 2 — Crear la base de datos (solo la primera vez)

```powershell
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS pispax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
# Password: nairda04

# Verificar que existe
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "SHOW DATABASES LIKE 'pispax';"
```

Las tablas **no hay que crearlas manualmente** — Hibernate las genera con `ddl-auto=update`.

### Paso 3 — Arrancar el backend

```powershell
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
mvn spring-boot:run
```

O compilar primero si hay cambios:
```powershell
mvn clean package -DskipTests
mvn spring-boot:run
```

### Paso 4 — Confirmar el arranque correcto

En el log de la consola deben aparecer estas líneas:

```
HikariPool-1 - Start completed.         ← Conexión MySQL: OK
Tomcat started on port 8080 (http)      ← Servidor HTTP: OK
Started PispaxApplication in X.XX seconds  ← Todo correcto
```

### Paso 5 — Verificar con petición de prueba

```powershell
# Endpoint público — debe devolver los 12 tipos de mercancía
Invoke-RestMethod -Uri "http://localhost:8080/api/tipo-mercancia" -Method GET

# Endpoint protegido — debe devolver 403 (correcto: no hay token)
Invoke-RestMethod -Uri "http://localhost:8080/api/viajes" -Method GET
```

### Paso 6 — Parar el servidor

En la terminal donde corre el servidor: `Ctrl + C`

---

## 17. Limitaciones conocidas del MVP

| Limitación | Descripción | Impacto |
|---|---|---|
| Compatibilidad vehículo-mercancía | La tabla `vehiculo_tipo_mercancia` existe pero el endpoint `/viajes/disponibles` no usa la compatibilidad para filtrar. Se devuelven todos los viajes PENDIENTES. | Bajo para demo |
| `VehiculoService` no popula `vehiculo_tipo_mercancia` | Al crear un vehículo, no se registran sus compatibilidades de mercancía. La tabla queda vacía. | Bajo para demo |
| Paginación | Los endpoints devuelven listas completas sin paginación. Con muchos registros puede ser lento. | Bajo para demo |
| Validación ADR/frío | No se verifica que un vehículo frigorífico transporte alimentos congelados, etc. | Medio |
| Notificaciones push | No hay sistema de notificaciones cuando un viaje cambia de estado. | Pendiente |
| HTTPS/TLS | Solo HTTP en desarrollo. En producción se requiere TLS. | Crítico para prod |
| `@EnableMethodSecurity` | Verificar que está presente en SecurityConfig para que `@PreAuthorize` funcione. | Crítico |

---

## 18. Resultados de la verificación del sistema

Verificación realizada el 28/05/2026:

| Verificación | Comando | Resultado |
|---|---|---|
| Servicio MySQL activo | `Get-Service -Name "MySQL*"` | MYSQL80: **Running** |
| Base de datos existe | `SHOW DATABASES LIKE 'pispax'` | **pispax** encontrada |
| Tablas creadas (5) | `SHOW TABLES` | **5 tablas** presentes |
| HikariCP conectado | Log de arranque | `HikariPool-1 - Start completed` ✅ |
| Tomcat activo | Log de arranque | `Tomcat started on port 8080` ✅ |
| Arranque completo | Log de arranque | `Started PispaxApplication in 2.82 seconds` ✅ |
| Endpoint público | `GET /api/tipo-mercancia` | **200 OK** (12 tipos) ✅ |
| Spring Security activo | `GET /api/viajes` (sin token) | **403 Forbidden** (correcto) ✅ |

---

*Documentación unificada generada el 08/06/2026 — TFG PisPax — Adrián Salazar Nicolás*
