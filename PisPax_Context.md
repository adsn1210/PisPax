# PISPAX — Archivo de contexto del proyecto

> **v3.0 — Mayo 2026** | Integración completa con datos del TFG  
> Pega este archivo al inicio de cualquier conversación nueva para que Claude tenga el contexto completo del proyecto sin necesidad de volver a explicarlo.

---

## 📋 Identidad del proyecto

| Campo | Valor |
|---|---|
| **Nombre del proyecto** | PisPax |
| **Tipo académico** | Trabajo de Fin de Ciclo (TFG) — Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM) |
| **Autor** | Adrián Salazar Nicolás |
| **Curso académico** | 2025/2026 |
| **Enfoque del desarrollo** | MVP (Producto Mínimo Viable) — funcionalidad esencial, sin sobreingeniería |
| **Estado del TFG** | En desarrollo (secciones 1–4 completadas; análisis y conclusiones pendientes) |

---

## 📱 Descripción del producto

**PisPax** es una aplicación móvil que conecta **clientes** que necesitan enviar mercancías con **transportistas** que tienen vehículos disponibles. El sistema gestiona el ciclo completo del viaje: solicitud, asignación, seguimiento de estados y finalización.

### Problema que resuelve
Muchas empresas y autónomos del sector del transporte de mercancías poseen flotas de vehículos que permanecen inactivos durante varias horas del día, generando costes operativos elevados sin producir ingresos. PisPax permite rentabilizar esa capacidad ociosa mediante un sistema digital seguro, transparente y accesible.

### Inspiración de mercado
OnTruck, Uber, Cabify, Blablacar — adaptados al sector de mercancías por carretera en el contexto europeo y español.

### Objetivo general del proyecto
> Desarrollar una aplicación móvil funcional que permita la gestión de servicios de transporte entre empresas, autónomos y clientes, optimizando el uso de vehículos disponibles mediante un sistema digital centralizado y seguro.

---

## 🎯 Objetivos específicos

1. **Implementar un sistema de creación de viajes óptimos** entre un punto A y un punto B, permitiendo que clientes soliciten transportes de forma ágil e inmediata.

2. **Integrar un modelo de seguridad** que garantice la correcta entrega de mercancías, trazabilidad completa y responsabilidad legal de ambas partes.

3. **Desarrollar una arquitectura escalable basada en servicios cloud** para soportar incrementos futuros de demanda sin necesidad de infraestructura propia.

4. **Incorporar un sistema de pagos interno** que permita realizar cobros y liquidaciones desde la app (futuro MVP+).

5. **Diseñar una interfaz gráfica moderna, sencilla y orientada** tanto a clientes como a transportistas, basada en principios UX centrados en el usuario.

6. **Gestionar correctamente la base de datos** de usuarios, vehículos, viajes y operaciones mediante un backend eficiente y escalable.

7. **Implementar geolocalización de la mercancía** en el momento de reparto con actualización de diferentes estados de envío.

---

## 🔍 Justificación del proyecto

### Contexto del mercado
El mercado del transporte de mercancías está experimentando una transición hacia modelos digitales similares a OnTruck, Uber, Cabify y Blablacar. Sin embargo, en el sector de mercancías aún existe una **gran oportunidad de optimización**: muchas empresas infrautilizan sus vehículos durante varias horas al día.

### Oportunidad de negocio
- Usuarios de servicios logísticos demandan **inmediatez, trazabilidad, pagos integrados** y una experiencia móvil sencilla
- El transporte terrestre de mercancías es fundamental en la economía española y europea
- La cercanía geográfica en UE permite implementar plataformas digitales viables
- Infraestructuras cloud permiten lanzar productos sin grandes inversiones iniciales

### Viabilidad técnica y académica
Este proyecto es adecuado como TFG en Desarrollo de Aplicaciones Multiplataforma porque permite trabajar aspectos reales del sector tecnológico:
- Conexiones en tiempo real
- Gestión de pagos
- Diseño de APIs REST
- Bases de datos complejas y relacionales
- Despliegue sobre servicios cloud
- Diseño UI/UX funcional

---

## 🏗️ Metodología del proyecto

### Tipo de investigación
**Investigación aplicada** de carácter tecnológico, orientada a resolver un problema real en el sector del transporte de mercancías mediante una solución software escalable.

### Enfoque metodológico
**Mixto** con predominio de **análisis cualitativo**, centrado en:
- Análisis de necesidades reales del sector
- Diseño centrado en experiencia de usuario (UX)
- Validación de viabilidad técnica
- Aplicación práctica de conocimientos académicos

### Prioridades de diseño
1. **Experiencia de usuario (UX)**: interfaz sencilla, intuitiva, rápida, similar a aplicaciones "Uber-like"
2. **Seguridad**: trazabilidad completa, responsabilidad legal clara
3. **Escalabilidad**: arquitectura cloud desde el inicio
4. **Simplicidad técnica**: evitar sobreenginierización; dejar como evolución futura tecnologías complejas (IA, algoritmos TSP avanzados)

---

## 📊 Perfiles de usuario identificados

### Cliente (demandante del servicio)
- **Necesidades principales**: rapidez en la contratación, sencillez de uso, fiabilidad del transportista, seguridad del envío
- **Comportamiento esperado**: experiencia similar a Uber; proceso ágil, transparente, accesible desde móvil
- **Casos de uso**: solicitar transporte urgente, consultar estado en tiempo real, historial de viajes

### Transportista (proveedor del servicio)
- **Perfil**: empresa con flota infrautilizada O autónomo con vehículo propio (cumpliendo requisitos legales)
- **Necesidades principales**: facilidad en aceptación de viajes, claridad de información, seguridad en pagos, gestión eficiente de trayectos
- **Comportamiento esperado**: optimizar recursos, generar ingresos adicionales, visibilidad de ganancias
- **Casos de uso**: buscar viajes disponibles, aceptar, actualizar estado de entrega, consultar pagos

---

## 📅 Procedimiento y fases del proyecto

### Fase 1: Análisis y definición de requisitos
- Estudio exhaustivo del sector del transporte de mercancías por carretera
- Identificación de oportunidades de optimización
- Análisis de plataformas existentes (OnTruck, Uber, Cabify)
- Recopilación de información mediante análisis documental e investigación de normativa vigente
- **Status**: ✅ Completado (información en documento TFG)

### Fase 2: Diseño conceptual y arquitectura
- Definición de arquitectura basada en principios de Transportation Management System (TMS) simplificado
- Especificación de requisitos funcionales y no funcionales
- Diseño del esquema de base de datos relacional
- Selección y justificación del stack tecnológico
- **Status**: ✅ Completado (documentado en secciones 4.2–4.3 del TFG)

### Fase 3: Optimización de rutas (Travelling Salesman Problem)
- Referencia teórica para futuras mejoras en planificación de múltiples entregas
- Minimización de tiempos de desplazamiento y costes operativos
- **Status**: ⏳ Fuera del alcance MVP; propuesto para evolución futura

### Fase 4: Diseño e implementación de la aplicación
- Desarrollo del backend (Spring Boot + MySQL)
- Desarrollo del frontend (Android nativo)
- Interfaces centradas en UX
- Integración de servicios cloud
- **Status**: 🔄 En curso (próximas 6–8 semanas)

### Fase 5: Pruebas y validación
- Pruebas funcionales (creación de viajes, aceptación, cambios de estado)
- Pruebas de compatibilidad vehículo-mercancía
- Pruebas de seguridad (JWT, validación de tokens)
- Validación con usuarios piloto
- **Status**: ⏳ Pendiente (después de Fase 4)

---

## 🏛️ Contextualización

### Contexto sectorial europeo y español
- El transporte terrestre de mercancías juega un papel fundamental en la economía
- Transición lenta hacia soluciones digitales comparado con otros sectores
- Métodos de contratación y planificación históricamente poco digitalizados
- Elevado volumen de transporte hace viable la implantación de plataformas digitales

### Contexto tecnológico
- Infraestructuras cloud consolidadas como estándar para aplicaciones modernas
- Modelos cloud permiten escalabilidad y flexibilidad sin inversión inicial en infraestructura propia
- Usuarios demandan trazabilidad, seguimiento en tiempo real, agilidad en contratación
- Aplicaciones móviles se han convertido en el principal canal de interacción

---

## 🛠️ Arquitectura general

```
┌─────────────────────────────────────────────────────────────────┐
│                   ARQUITECTURA DE PISPAX                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  [App Android nativa]                                           │
│  ├─ UI XML layouts (Material Design)                            │
│  ├─ Retrofit + Gson (HTTP/JSON)                                │
│  ├─ RecyclerView (listas)                                      │
│  └─ SharedPreferences (sesión local)                           │
│                                                                 │
│  ════════════════ HTTP + JSON + JWT ════════════════           │
│                                                                 │
│  [API REST Spring Boot] (stateless)                             │
│  ├─ Controller (recepción y validación)                         │
│  ├─ Service (lógica de negocio)                                │
│  ├─ Repository (acceso a datos)                                │
│  └─ Security (Spring Security + JWT)                           │
│                                                                 │
│  ════════════════ JPA / Hibernate ════════════════              │
│                                                                 │
│  [MySQL 8] Base de datos relacional                            │
│  └─ 5 tablas principales + 3 ENUMs                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Decisiones de diseño
- **Frontend único Android**: reducir complejidad en MVP; iOS diferido a evolución futura
- **Backend stateless**: cada request lleva autenticación (JWT), permitiendo escalabilidad horizontal
- **DTOs separados**: nunca se expone la entidad JPA directamente al cliente
- **Validación en Service**: jamás en Controller; permite reutilización y testabilidad

---

## 🛠️ Stack tecnológico

### Backend

| Componente | Tecnología | Versión | Justificación |
|---|---|---|---|
| **Lenguaje** | Java | 17 | Robustez, tipado fuerte, ecosistema maduro |
| **Framework** | Spring Boot | 3.2.x | Velocidad de desarrollo, convenciones, integración completa |
| **ORM** | Spring Data JPA + Hibernate | incluido | Abstracción de BD, queries tipadas, validación automática |
| **Seguridad** | Spring Security + JWT (jjwt) | 0.12.x | Estándar en APIs REST; stateless; escalable |
| **Build** | Maven | 3.9 | Gestión de dependencias; integración CI/CD |
| **Base de datos** | MySQL | 8 | Relacional; soporte ACID; estándar en producción |
| **Validación** | spring-boot-starter-validation | 3.2.x | Bean Validation; anotaciones estándar |
| **Utilidad** | Lombok | 1.18.x | Reduce boilerplate (getters, setters, equals) |

### Android

| Componente | Tecnología | Versión | Justificación |
|---|---|---|---|
| **Lenguaje** | Java | 17 | Compatibilidad con ecosistema Android maduro |
| **IDE** | Android Studio | 2024+ | Herramienta oficial, emulador integrado |
| **Interfaz** | XML layouts | nativo | Control total; Material Design |
| **Cliente HTTP** | Retrofit | 2.9.0 | Tipado; integración con Gson; callbacks simples |
| **Serialización JSON** | Gson + Converter | 2.9.0 | DTOs ↔ JSON automático |
| **Listas dinámicas** | RecyclerView | 1.3.2 | Eficiente; patrón ViewHolder; reutilizable |
| **Componentes UI** | Material Design | 1.11.0 | Estética moderna; componentes de sistema |
| **Sesión local** | SharedPreferences | nativo | Almacenamiento clave-valor; persistencia de JWT |

### Servicios cloud (futuro MVP+)
- Firebase Authentication / Google Cloud Identity
- AWS S3 o Firebase Storage (imágenes)
- Google Maps API (geolocalización)
- Stripe / PayPal (pagos)

---

## 💾 Base de datos — Esquema completo v3.0

### Entidades y relaciones

#### `usuario`
```sql
CREATE TABLE usuario (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  apellidos VARCHAR(150) NOT NULL,
  email VARCHAR(200) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  telefono VARCHAR(20),
  rol ENUM('CLIENTE','TRANSPORTISTA') NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

#### `vehiculo`
```sql
CREATE TABLE vehiculo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  matricula VARCHAR(15) NOT NULL UNIQUE,
  marca VARCHAR(80) NOT NULL,
  modelo VARCHAR(80) NOT NULL,
  tipo_vehiculo ENUM(...) NOT NULL,
  subtipo VARCHAR(50),
  tara_kg DECIMAL(8,2),
  capacidad_kg DECIMAL(8,2) NOT NULL,
  mma_kg DECIMAL(8,2),
  carnet_requerido ENUM('B','C1','C','C+E') NOT NULL DEFAULT 'B',
  transportista_id BIGINT NOT NULL,
  FOREIGN KEY (transportista_id) REFERENCES usuario(id)
);
```

#### `tipo_mercancia`
```sql
CREATE TABLE tipo_mercancia (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL UNIQUE,
  nombre_db VARCHAR(100) NOT NULL UNIQUE,
  descripcion VARCHAR(500)
);
```

#### `vehiculo_tipo_mercancia`
```sql
CREATE TABLE vehiculo_tipo_mercancia (
  vehiculo_id BIGINT NOT NULL,
  tipo_mercancia_id BIGINT NOT NULL,
  compatible ENUM('SI','CON_REQUISITOS') NOT NULL DEFAULT 'SI',
  PRIMARY KEY (vehiculo_id, tipo_mercancia_id),
  FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id),
  FOREIGN KEY (tipo_mercancia_id) REFERENCES tipo_mercancia(id)
);
```

#### `viaje`
```sql
CREATE TABLE viaje (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cliente_id BIGINT NOT NULL,
  transportista_id BIGINT,
  vehiculo_id BIGINT,
  tipo_mercancia_id BIGINT,
  descripcion_mercancia VARCHAR(500),
  direccion_recogida VARCHAR(300) NOT NULL,
  direccion_entrega VARCHAR(300) NOT NULL,
  peso_kg DECIMAL(8,2) NOT NULL,
  estado ENUM(...) NOT NULL DEFAULT 'PENDIENTE',
  fecha_solicitud TIMESTAMP DEFAULT NOW(),
  fecha_inicio TIMESTAMP,
  fecha_fin TIMESTAMP,
  FOREIGN KEY (cliente_id) REFERENCES usuario(id),
  FOREIGN KEY (transportista_id) REFERENCES usuario(id),
  FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id),
  FOREIGN KEY (tipo_mercancia_id) REFERENCES tipo_mercancia(id)
);
```

### ENUMs

**`rol`** (usuario):
```
CLIENTE | TRANSPORTISTA
```

**`tipo_vehiculo`**:

| Valor | Descripción | MMA máx. | Carnet |
|---|---|---|---|
| `FURGONETA` | Vehículo ligero de reparto (compacta/mediana) | 3.500 kg | B |
| `FURGON_GRANDE` | Furgón de alto volumen (mobiliario) | 3.500 kg | B |
| `FRIGORIFICO` | Cámara de temperatura controlada (0°C–-18°C) | 3.500 kg | B |
| `CAMION_LIGERO` | Camión rígido pequeño (distribución regional) | 3.500–7.500 kg | C1 |
| `CAMION_PESADO` | Camión rígido gran tonelaje (requiere CAP) | > 7.500 kg | C + CAP |
| `CAMION_ARTICULADO` | Semirremolque (larga distancia; requiere CAP) | Hasta 42.000 kg | C+E + CAP |
| `PLATAFORMA` | Para cargas voluminosas, palets, maquinaria | Variable | C1/C |

**`carnet_requerido`**:

| Valor | Rango MMA | Descripción |
|---|---|---|
| `B` | Hasta 3.500 kg | Carnet estándar |
| `C1` | 3.500–7.500 kg | Requiere formación adicional |
| `C` | > 7.500 kg | Requiere CAP (Certificado de Aptitud Profesional) |
| `C+E` | Articulado > 750 kg | Requiere C previo y CAP |

**`estado` (viaje)** — 6 estados principales + transición de cancelación:

```
PENDIENTE
    ↓
ACEPTADO
    ↓
SALIDA_RECOGIDA
    ↓
LLEGADA_RECOGIDA
    ↓
MERCANCIA_RECOGIDA
    ↓
SALIDA_ENTREGA
    ↓
LLEGADA_ENTREGA
    ↓
COMPLETADO

(Cualquier estado anterior) → CANCELADO (transición lateral)
```

#### Estados detallados

1. **PENDIENTE**: Viaje creado por cliente, esperando aceptación de transportista
2. **ACEPTADO**: Transportista ha aceptado el viaje y se asignó un vehículo
3. **SALIDA_RECOGIDA**: Transportista sale hacia la dirección de recogida
4. **LLEGADA_RECOGIDA**: Transportista ha llegado al punto de recogida
5. **MERCANCIA_RECOGIDA**: La mercancía ha sido recogida y cargada en el vehículo
6. **SALIDA_ENTREGA**: Transportista sale hacia la dirección de entrega
7. **LLEGADA_ENTREGA**: Transportista ha llegado al punto de entrega
8. **COMPLETADO**: Mercancía descargada y viaje finalizado
9. **CANCELADO**: Viaje cancelado (desde cualquier estado anterior)

---

## 🛒 Catálogo de mercancías (12 tipos — v3.0)

| ID | Nombre | nombre_db | Descripción | Veh. recomendado | Requisitos especiales |
|---|---|---|---|---|---|
| 1 | Paquetería general | `paqueteria_general` | Paquetes, cajas, envíos estándar | Furgoneta / Furgón | Ninguno |
| 2 | Electrónica frágil | `electronica_fragil` | Dispositivos frágiles, equipos | Furgón con protección | Amortiguación |
| 3 | Alimentos frescos | `alimentos_frescos` | Productos perecederos (0–8°C) | Frigorífico | Cadena frío |
| 4 | Alimentos congelados | `alimentos_congelados` | Productos congelados (< -18°C) | Frigorífico | Cadena frío estricta |
| 5 | Materiales construcción | `materiales_construccion` | Cemento, acero, materiales pesados | Camión / Furgón carga pesada | Ninguno |
| 6 | Mobiliario | `mobiliario` | Muebles, sofás, piezas grandes | Furgón grande / Camión | Manipulación cuidadosa |
| 7 | Mercancía peligrosa (ADR) | `mercancia_peligrosa` | Sustancias químicas, inflamables, tóxicas | Vehículo ADR habilitado | **Certificado ADR vigente** |
| 8 | Productos farmacéuticos | `productos_farmaceuticos` | Medicinas, biologics | Furgón temp. controlada | **GDP** (recomendado) |
| 9 | Textil y moda | `textil_moda` | Ropa, telas, calzado | Furgoneta / Furgón | Ninguno |
| 10 | Maquinaria industrial | `maquinaria_industrial` | Máquinas, equipos pesados | Camión plataforma | Aseguramiento especial |
| 11 | Documentación y archivo | `documentacion_archivo` | Documentos, libros, archivos | Furgoneta estándar | Ninguno |
| 12 | Vehículos y automoción | `vehiculos_automocion` | Coches, motos, piezas vehiculares | Camión plataforma / portavehículos | Homologación |

### Matriz de compatibilidad vehículo × mercancía

✅ Compatible | ⚠️ Compatible con requisitos | ❌ No compatible

| Mercancía ↓ / Vehículo → | Furgoneta | Furgón Grande | Frigorífico | Camión Ligero C1 | Camión Pesado C | Camión Articulado C+E | Plataforma |
|---|---|---|---|---|---|---|---|
| Paquetería general | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Electrónica frágil | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Alimentos frescos | ❌ | ❌ | ✅ | ⚠️ | ⚠️ | ⚠️ | ❌ |
| Alimentos congelados | ❌ | ❌ | ✅ | ⚠️ | ⚠️ | ⚠️ | ❌ |
| Materiales construcción | ❌ | ⚠️ | ❌ | ✅ | ✅ | ✅ | ✅ |
| Mobiliario | ❌ | ✅ | ❌ | ✅ | ✅ | ✅ | ⚠️ |
| Mercancía peligrosa | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ⚠️ ADR | ❌ |
| Productos farmacéuticos | ❌ | ⚠️ GDP | ✅ | ⚠️ GDP | ⚠️ GDP | ⚠️ GDP | ❌ |
| Textil y moda | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Maquinaria industrial | ❌ | ❌ | ❌ | ⚠️ Aseguro | ✅ | ✅ | ✅ |
| Documentación y archivo | ✅ | ✅ | ❌ | ✅ | ✅ | ✅ | ❌ |
| Vehículos y automoción | ❌ | ❌ | ❌ | ❌ | ⚠️ | ⚠️ | ✅ |

**Notas**:
- ⚠️ ADR: requiere conductor con certificado ADR vigente
- ⚠️ GDP: Good Distribution Practice (farmacéuticos); recomendado pero no obligatorio en MVP
- ⚠️ Aseguro: requiere póliza de seguros para mercancía de alto valor

---

## 🎨 Identidad visual y marca

### Colores corporativos
- **Color principal**: `#FF8E3B` (naranja)
- **Color secundario**: `#2D2850` (navy)
- **Blanco**: `#FFFFFF`
- **Grises**: `#F5F5F5` (fondo), `#333333` (texto)

### Fuentes
- **Títulos**: sans-serif robusto (ej. Roboto Bold)
- **Cuerpo**: sans-serif legible (ej. Roboto Regular, Open Sans)

---

## 📚 Estructura del proyecto Android

```
com.pispax.app/
├── model/                          ← DTOs
│   ├── UsuarioDTO.java
│   ├── ViajeDTO.java
│   ├── VehiculoDTO.java
│   ├── TipoMercanciaDTO.java
│   └── request/
│       ├── LoginRequest.java
│       ├── RegistroRequest.java
│       ├── CrearViajeRequest.java
│       └── ActualizarEstadoRequest.java
├── network/                        ← Cliente HTTP
│   ├── ApiService.java             ← Interfaz Retrofit
│   └── RetrofitClient.java         ← Singleton
├── ui/                             ← Activities y Adapters
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   └── RegistroActivity.java
│   ├── viajes/
│   │   ├── ListaViajesActivity.java
│   │   ├── DetalleViajeActivity.java
│   │   └── ViajesAdapter.java
│   └── vehiculos/
│       ├── ListaVehiculosActivity.java
│       └── VehiculosAdapter.java
├── util/
│   ├── SessionManager.java         ← Gestión JWT en SharedPreferences
│   └── Constants.java
└── res/
    ├── layout/                     ← XML layouts
    ├── values/                     ← Colores, strings, estilos
    └── drawable/                   ← Iconos, imágenes
```

### Configuración de red
```java
// RetrofitClient.java
private static final String BASE_URL = "http://10.0.2.2:8080/api/";
// 10.0.2.2 mapea a localhost de la máquina host desde el emulador Android
```

---

## 🏛️ Arquitectura backend — capas

```
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE CONTROLLER                         │
│  Recibe peticiones HTTP, valida entrada, delega en Service     │
├─────────────────────────────────────────────────────────────────┤
│                      CAPA DE SERVICE                            │
│  Lógica de negocio, validaciones, reglas de compatibilidad     │
├─────────────────────────────────────────────────────────────────┤
│                     CAPA DE REPOSITORY                          │
│  Interfaz JpaRepository, consultas personalizadas              │
├─────────────────────────────────────────────────────────────────┤
│                     BASE DE DATOS (MySQL)                       │
│  Persistencia relacional con ACID                              │
└─────────────────────────────────────────────────────────────────┘
```

### Patrones y decisiones de diseño

**DTOs (Data Transfer Objects)**:
- Objetos separados para request/response
- Nunca se expone la entidad JPA directamente al cliente
- Mapeo mediante constructores o builders

**Validación**:
- Siempre en la capa **Service**, jamás en Controller
- Permite reutilización; facilita testing
- Ejemplos: compatibilidad vehículo-mercancía, transiciones de estado válidas

**Seguridad JWT**:
- Login genera token JWT con tiempo de expiración
- Todas las rutas (excepto `/api/auth/**`) requieren header `Authorization: Bearer {token}`
- `JwtAuthFilter` valida antes de que llegue al controller
- Responde `401 Unauthorized` si token inválido o expirado

**Gestión de estados**:
- Estados modelados como **ENUM Java** en backend
- Validación en Service antes de persistir
- Transiciones permitidas: PENDIENTE → ACEPTADO → SALIDA_RECOGIDA → ... → COMPLETADO
- Transición lateral a CANCELADO desde cualquier estado

**Contraseñas**:
- Siempre cifradas con **BCrypt** antes de guardar en BD
- Nunca se almacenan en texto plano

**Respuestas de error**:
- `ResponseEntity<T>` con códigos HTTP semánticos:
  - `400 Bad Request` — entrada inválida
  - `401 Unauthorized` — token inválido/expirado
  - `403 Forbidden` — acceso denegado
  - `404 Not Found` — recurso no existe
  - `409 Conflict` — estado inválido para operación

---

## 🔌 Endpoints principales

| Método | Ruta | Actor | Descripción | Status |
|---|---|---|---|---|
| POST | `/api/auth/login` | Ambos | Login, devuelve JWT | 🔄 Por implementar |
| POST | `/api/auth/registro` | Ambos | Registro de nuevo usuario | 🔄 Por implementar |
| GET | `/api/viajes` | Ambos | Listar mis viajes (filtra por rol) | 🔄 Por implementar |
| POST | `/api/viajes` | Cliente | Crear nueva solicitud de viaje | 🔄 Por implementar |
| PUT | `/api/viajes/{id}/estado` | Transportista | Actualizar estado del viaje | 🔄 Por implementar |
| GET | `/api/viajes/disponibles` | Transportista | Viajes PENDIENTES compatibles con sus vehículos | 🔄 Por implementar |
| PUT | `/api/viajes/{id}/aceptar` | Transportista | Aceptar un viaje (asignar transportista + vehículo) | 🔄 Por implementar |
| GET | `/api/vehiculos/mis-vehiculos` | Transportista | Listar mis vehículos | 🔄 Por implementar |
| POST | `/api/vehiculos` | Transportista | Dar de alta un nuevo vehículo | 🔄 Por implementar |
| GET | `/api/tipo-mercancia` | Ambos | Catálogo de tipos de mercancía disponibles | 🔄 Por implementar |

---

## 🔍 Lógica de compatibilidad vehículo-mercancía

### Regla de negocio
Un transportista solo ve viajes cuya mercancía puede transportar con alguno de sus vehículos.

### Consulta base

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

### Validaciones adicionales en Service (próxima fase)
Entradas con `compatible = CON_REQUISITOS` deben pasar validación adicional:
- **ADR**: verificar que conductor tiene certificado ADR vigente
- **GDP**: verificar que vehículo frigorífico está homologado
- **Alimentos frescos/congelados**: verificar temperaturas de cámara
- **Maquinaria industrial**: verificar póliza de seguros

---

## 📋 Requisitos del sistema (por completar en TFG sección 4.1)

### Requisitos Funcionales (RF) — estructura propuesta

| RF | Descripción | Actor | Prioridad |
|---|---|---|---|
| RF-01 | Registrar usuario (Cliente/Transportista) | Ambos | **ALTA** |
| RF-02 | Autenticarse con email/contraseña | Ambos | **ALTA** |
| RF-03 | Crear viaje (solicitud de transporte) | Cliente | **ALTA** |
| RF-04 | Listar viajes disponibles | Transportista | **ALTA** |
| RF-05 | Aceptar viaje (asignar transportista + vehículo) | Transportista | **ALTA** |
| RF-06 | Actualizar estado del viaje | Transportista | **ALTA** |
| RF-07 | Consultar estado actual del viaje | Cliente | **ALTA** |
| RF-08 | Listar mis vehículos | Transportista | **ALTA** |
| RF-09 | Registrar nuevo vehículo | Transportista | **ALTA** |
| RF-10 | Validar compatibilidad vehículo-mercancía | Sistema | **MEDIA** |
| RF-11 | Listar catálogo de tipos de mercancía | Ambos | **MEDIA** |
| ... | ... | ... | ... |

### Requisitos No Funcionales (RNF) — estructura propuesta

| RNF | Descripción | Métrica |
|---|---|---|
| RNF-01 | Seguridad: autenticación JWT | Token válido 24h |
| RNF-02 | Escalabilidad: backend stateless | Soportar 100+ usuarios concurrentes |
| RNF-03 | Disponibilidad: recuperación de fallos | 99.5% uptime |
| RNF-04 | Rendimiento: tiempo de respuesta API | < 500ms en 95% de casos |
| RNF-05 | Compatibilidad: versiones Android | API 21+ (Android 5.0+) |
| RNF-06 | Usabilidad: interfaz intuitiva | Tarea completada en < 3 clics |
| ... | ... | ... |

---

## ✅ Alcance MVP — qué está IN y qué está OUT

### Incluido en MVP (6–8 semanas)

**Core functionality**:
- ✅ Registro y login con JWT
- ✅ Gestión básica de vehículos (tipo, capacidad, carnet requerido)
- ✅ Creación de viajes por cliente
- ✅ Listado de viajes disponibles (con filtro de compatibilidad)
- ✅ Aceptación de viajes por transportista
- ✅ Actualización de 8 estados del viaje (PENDIENTE → COMPLETADO/CANCELADO)
- ✅ Consulta de estado en tiempo real
- ✅ Validación de compatibilidad vehículo-mercancía (SI/CON_REQUISITOS)
- ✅ Persistencia en MySQL
- ✅ Catálogo de 12 tipos de mercancía

**Seguridad**:
- ✅ BCrypt para contraseñas
- ✅ JWT para sesiones
- ✅ Validación de roles (CLIENTE/TRANSPORTISTA)

### Excluido de MVP (futuras evoluciones)

**Pagos y facturación**:
- ❌ Sistema de pagos integrado (Stripe, PayPal)
- ❌ Facturación y liquidación automática
- ❌ Historial de transacciones

**Ubicación en tiempo real**:
- ❌ Geolocalización GPS en tiempo real
- ❌ Tracking de vehículos en mapa
- ❌ Notificaciones de proximidad

**Notificaciones avanzadas**:
- ❌ Push notifications (Firebase Cloud Messaging)
- ❌ Notificaciones SMS
- ❌ Sistema de alertas

**Optimización y IA**:
- ❌ Algoritmos TSP (Travelling Salesman Problem)
- ❌ Precios dinámicos con IA
- ❌ Matching automático de viajes

**Plataformas y escalado**:
- ❌ Versión iOS
- ❌ Panel de administración web
- ❌ Despliegue en cloud (AWS, GCP, Azure)
- ❌ Caché distribuido (Redis)
- ❌ Message Queue (RabbitMQ, Kafka)

**Integraciones externas**:
- ❌ Integración con ERP de transportistas
- ❌ APIs de terceros (Google Maps, OpenWeather)
- ❌ Single Sign-On (Google, GitHub)

---

## 📐 Convenciones del proyecto

### Idioma del código
- **Español** para entidades de dominio: `usuario`, `viaje`, `vehiculo`, `transportista`, `mercancia`
- **Inglés** para patrones técnicos: `Controller`, `Service`, `Repository`, `DTO`, `Enum`, `Exception`

### Estructura de paquetes
```java
com.pispax.app
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── security
├── service
└── util
```

### Validación y lógica de negocio
- **SIEMPRE en Service**, jamás en Controller
- Permite reutilización y testabilidad
- Responde con códigos HTTP semánticos

### Gestión de estados
- Modelados como **ENUM Java**
- Validación de transiciones en Service
- Transiciones laterales a CANCELADO permitidas desde cualquier estado

### Contraseñas
- Cifradas con **BCrypt** (`BCryptPasswordEncoder` de Spring Security)
- Nunca se almacenan en texto plano

### Respuestas de error
```java
// Ejemplo: recurso no encontrado
return ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(new ErrorResponse("Viaje no encontrado", 404));

// Ejemplo: validación fallida
return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    .body(new ErrorResponse("Peso del viaje excede capacidad del vehículo", 400));
```

---

## 🎯 Estado actual del proyecto (Mayo 2026)

| Componente | Estado | Notas |
|---|---|---|
| **Análisis de requisitos** | ✅ Completado | Documentado en secciones 1–3 del TFG |
| **Diseño de BD** | ✅ Completado | Esquema E/R con 5 tablas + 3 ENUMs |
| **Catálogo de vehículos** | ✅ Completado | 7 tipos con carnet y MMA |
| **Catálogo de mercancías** | ✅ Completado | 12 tipos con nombre_db y matriz de compatibilidad |
| **Stack tecnológico** | ✅ Definido | Java 17, Spring Boot 3.2, Android, MySQL 8 |
| **Arquitectura de sistema** | ✅ Diseñada | Capas Controller → Service → Repository → BD |
| **Endpoints API** | ✅ Especificados | 10 rutas principales definidas |
| **Estructura Android** | ✅ Planificada | Paquetes, Activities, Adapters listos para codificar |
| **Desarrollo backend** | 🔄 En progreso | Próximas 3–4 semanas |
| **Desarrollo Android** | 🔄 En progreso | Paralelo al backend |
| **Pruebas funcionales** | ⏳ Pendiente | Después de desarrollo completado |
| **Documentación TFG completa** | ⏳ Pendiente | Secciones 5 (Análisis) y 6 (Conclusiones) pendientes |

---

## 📑 Checklist TFG — PIXPAX (v3.0)

### Sección 4 — DESARROLLO (en progreso)

- [x] **4.1 Análisis del sistema**
  - [x] Descripción general del proyecto
  - [x] Justificación y contexto
  - [x] Objetivos generales y específicos
  - [x] Identificación de perfiles de usuario
  - [ ] Tabla formal de Requisitos Funcionales (RF-01 a RF-N)
  - [ ] Tabla formal de Requisitos No Funcionales (RNF-01 a RNF-N)
  - [ ] Diagrama de casos de uso (UML)

- [x] **4.2 Tecnologías utilizadas**
  - [x] Justificación del stack backend
  - [x] Justificación del stack Android
  - [x] Justificación de BD relacional (MySQL)
  - [x] Servicios cloud (deferred, documentados como futuro)

- [x] **4.3 Diseño del sistema**
  - [x] Diagrama de arquitectura general
  - [x] Diagrama Entidad-Relación (E/R) de BD
  - [x] Esquema de tablas y ENUMs
  - [x] Estructura de paquetes Android
  - [ ] Wireframes / prototipos de pantallas principales
  - [ ] Diagrama de flujo de estados de viaje

- [ ] **4.4 Implementación**
  - [ ] Descripción de módulos desarrollados (una vez codificados)
  - [ ] Capturas de pantalla de la app
  - [ ] Fragmentos de código relevantes comentados
  - [ ] Explicación del sistema de estados (8 estados + cancelación)

- [ ] **4.5 Pruebas realizadas**
  - [ ] Plan de pruebas (unitarias, integración, funcionales)
  - [ ] Tabla de resultados de pruebas (una vez ejecutadas)

### Sección 5 — ANÁLISIS (por completar)

- [ ] **5.1 Análisis del cumplimiento de objetivos**
- [ ] **5.2 Evaluación del sistema desarrollado**
- [ ] **5.3 Comparación con soluciones existentes**
- [ ] **5.4 Limitaciones del proyecto**

### Sección 6 — CONCLUSIONES (por completar)

- [ ] Valoración personal y profesional
- [ ] Líneas de mejora y continuidad
- [ ] Impacto potencial del proyecto

---

## 🔗 Referencias y bibliografía (TFG)

### Plataformas competidoras analizadas
- **OnTruck** (https://www.ontruck.com/) — modelo inspirador principal
- **Uber** (https://www.uber.com/) — UX y modelo de marketplace
- **Cabify** (https://cabify.com/) — experiencia móvil
- **Blablacar** (https://www.blablacar.es/) — modelo de matching de viajes

### Recursos técnicos
- Spring Boot Official Documentation
- Android Developers Guide (developer.android.com)
- JWT Best Practices
- MySQL 8 Documentation

### Contexto sectorial
- Movertis: [Las 7 mejores apps para transportistas](https://www.movertis.com/blog/las-7-mejores-apps-para-transportistas)
- ActiRuta: [Optimizador de rutas](https://www.actiruta.com/Optimizador_de_rutas.aspx)
- TFG UPM (referencia similar): [TFG_JOEL_VAL_GARCIA.pdf](https://oa.upm.es/67562/1/TFG_JOEL_VAL_GARCIA.pdf)

---

## 💡 Notas finales para el desarrollo

1. **MVP discipline**: mantener el alcance restringido; NO agregar pagos, mapas, IA hasta v2.0
2. **User testing**: validar con usuarios piloto de la red de Adrián (transportistas reales) después de completar el core
3. **Documentación inline**: comentar lógica compleja (compatibilidad de mercancías, transiciones de estado)
4. **Testing**: escribir pruebas unitarias en Service antes de integrar con Android
5. **Git workflow**: commits pequeños, ramas por feature, PRs con revisión
6. **Backend primero**: implementar y testear APIs antes de construir UI Android
7. **Configuración externa**: no hardcodear URLs, puertos, credenciales; usar `application.properties`

---
