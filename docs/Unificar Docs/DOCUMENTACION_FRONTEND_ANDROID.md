# Documentación Frontend Android — PisPax
### TFG DAM 2025/2026 — Adrián Salazar Nicolás

---

## Índice

1. [Visión general del frontend](#1-vision-general)
2. [Stack tecnológico y dependencias](#2-stack)
3. [Arquitectura del proyecto Android](#3-arquitectura)
4. [Capa de infraestructura](#4-infraestructura)
5. [Modelos y DTOs](#5-modelos)
6. [Capa de red: Retrofit + JWT](#6-red)
7. [Sistema de sesión: SessionManager](#7-sesion)
8. [Flujo de navegación completo](#8-navegacion)
9. [Pantallas implementadas (Activities)](#9-activities)
10. [Adaptadores (RecyclerView y Spinner)](#10-adapters)
11. [Sistema de temas: claro y oscuro](#11-temas)
12. [Simulación visual del viaje (OSMDroid)](#12-simulacion)
13. [Decisiones de diseño destacables](#13-decisiones)
14. [Cómo funciona la conexión con el backend](#14-conexion)
15. [Cómo explicarlo en la defensa del TFG](#15-defensa)

---

## 1. Visión general del frontend

El frontend de PisPax es una **aplicación Android nativa** desarrollada en Java 17 con Android Studio. La app permite a dos tipos de usuario — **clientes** y **transportistas** — interactuar con el sistema de transporte de mercancías a través de una interfaz tipo Uber.

El frontend es completamente **stateless**: no guarda datos en una base de datos local. Toda la persistencia está en el backend. La única información local es el token JWT y los datos de sesión del usuario, guardados en `SharedPreferences`.

### Diagrama de arquitectura general

```
┌──────────────────────────────────────────────────────────┐
│                   APP ANDROID (Frontend)                  │
│                                                          │
│   UI (Activities / Adapters)                             │
│         │                                                │
│   SessionManager  ←→  SharedPreferences (local)          │
│         │                                                │
│   RetrofitClient (singleton)                             │
│         │                                                │
│   ApiService (interfaz Retrofit)                         │
│         │                                                │
└─────────┼────────────────────────────────────────────────┘
          │  HTTP + JSON + JWT (Authorization header)
          ▼
    Backend Spring Boot
    (http://10.0.2.2:8080/api/)
```

---

## 2. Stack tecnológico y dependencias

| Componente | Tecnología | Versión | Justificación |
|---|---|---|---|
| Lenguaje | Java | 17 | Coherencia con el backend; conocimiento del alumno |
| IDE | Android Studio | 2024+ | Herramienta oficial de Android |
| SDK mínimo | Android | 21 (Android 5.0) | Cubre el 98% de dispositivos activos |
| SDK objetivo | Android | 34 (Android 14) | Versión estable más reciente |
| UI | XML Layouts + Material Design | 1.11.0 | Estética moderna; componentes sistema |
| HTTP | Retrofit | 2.9.0 | Cliente tipado; integración con Gson |
| JSON | Gson | 2.9.0 | Mapeo automático JSON ↔ DTOs Java |
| Sesión | SharedPreferences | nativo | Persistencia clave-valor para JWT |
| Listas | RecyclerView | 1.3.2 | Eficiente; patrón ViewHolder |
| Mapa | OSMDroid | 6.1.18 | OpenStreetMap gratuito, sin API key |
| Animaciones | Lottie | 6.4.0 | Animaciones JSON (celebración, carga) |

### Permisos requeridos (`AndroidManifest.xml`)

```
INTERNET               → Para Retrofit (llamadas al backend)
ACCESS_NETWORK_STATE   → Para verificar conectividad
WRITE_EXTERNAL_STORAGE → Caché de tiles OSMDroid (solo hasta API 28)
READ_EXTERNAL_STORAGE  → Caché de tiles OSMDroid (solo hasta API 32)
android:usesCleartextTraffic="true" → HTTP sin TLS en desarrollo local
```

---

## 3. Arquitectura del proyecto Android

### Estructura de paquetes Java

```
com.pispax.app/
│
├── PispaxApplication.java          ← Punto de entrada: inicializa OSMDroid y el tema
│
├── model/                          ← DTOs que mapean las respuestas JSON del backend
│   ├── LoginResponse.java          ← Envuelve token + UsuarioDTO
│   ├── UsuarioDTO.java
│   ├── ViajeDTO.java               ← Incluye helpers: estaEnCurso(), estaFinalizado()
│   ├── VehiculoDTO.java
│   ├── TipoMercanciaDTO.java
│   └── request/                    ← Objetos enviados al backend (body de POST/PUT)
│       ├── LoginRequest.java
│       ├── RegistroRequest.java
│       ├── CrearViajeRequest.java
│       ├── CrearVehiculoRequest.java
│       ├── AceptarViajeRequest.java
│       └── ActualizarEstadoRequest.java
│
├── network/
│   ├── ApiService.java             ← Interfaz Retrofit: todos los endpoints
│   └── RetrofitClient.java         ← Singleton: JWT interceptor + logger
│
├── ui/
│   ├── auth/
│   │   ├── SplashActivity.java     ← Pantalla inicial: comprueba sesión 1.2s
│   │   ├── LoginActivity.java
│   │   └── RegistroActivity.java
│   ├── cliente/
│   │   ├── HomeClienteActivity.java
│   │   ├── CrearViajeActivity.java
│   │   └── MisViajesClienteActivity.java
│   ├── transportista/
│   │   ├── HomeTransportistaActivity.java
│   │   ├── ViajesDisponiblesActivity.java
│   │   ├── MisViajesTransportistaActivity.java
│   │   ├── MisVehiculosActivity.java
│   │   └── CrearVehiculoActivity.java
│   ├── shared/
│   │   ├── DetalleViajeActivity.java   ← Ficha completa; botones adaptativos por rol
│   │   └── MapaViajeActivity.java      ← Mapa OSMDroid + timeline + simulación
│   └── adapter/
│       ├── ViajesAdapter.java          ← Genérico; usado en 5 pantallas distintas
│       ├── VehiculosAdapter.java       ← Con modo selector para aceptar viajes
│       ├── TipoMercanciaAdapter.java   ← Para Spinner en CrearViaje
│       └── TimelineAdapter.java        ← Timeline de estados en el mapa
│
└── util/
    ├── SessionManager.java         ← JWT + datos usuario en SharedPreferences
    └── Constants.java              ← BASE_URL, claves, roles
```

### Por qué se separan los paquetes `cliente/` y `transportista/`

Cada rol tiene un flujo completamente distinto. El cliente crea viajes y consulta su estado. El transportista busca viajes disponibles, los acepta y avanza los estados. Mezclar ambos flujos en las mismas Activities generaría lógica condicional por toda la UI. La separación hace el código más claro y mantenible.

### Por qué existe `shared/`

`DetalleViajeActivity` y `MapaViajeActivity` son pantallas que ambos roles necesitan, pero con **comportamiento distinto**. En lugar de duplicar el código, la pantalla se parametriza: lee el rol de `SessionManager` y adapta los botones visibles. Un cliente ve solo la información. Un transportista asignado puede además avanzar el estado.

---

## 4. Capa de infraestructura

### `PispaxApplication.java`

Es la clase `Application` que Android instancia antes que cualquier Activity. Se encarga de dos tareas al arrancar:

1. **Inicializar OSMDroid**: el mapa necesita que se configure el `UserAgent` antes de usarse.
2. **Aplicar el tema guardado**: lee la preferencia de modo oscuro/claro de `SessionManager` y aplica el modo con `AppCompatDelegate`.

```java
Configuration.getInstance().setUserAgentValue(getPackageName());
boolean dark = SessionManager.isDarkMode(this);
AppCompatDelegate.setDefaultNightMode(
    dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
);
```

### `Constants.java`

Centraliza todas las constantes del proyecto:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/api/";
// 10.0.2.2 es la dirección que el emulador Android usa para conectar
// con localhost del PC. En dispositivo físico se usa la IP WiFi del PC.

public static final String PREFS_NAME  = "pispax_prefs";
public static final String KEY_TOKEN   = "jwt_token";
public static final String KEY_USER_ID = "user_id";
public static final String KEY_ROL     = "user_rol";
public static final String KEY_NOMBRE  = "user_nombre";
public static final String KEY_DARK_MODE = "dark_mode";
public static final String EXTRA_VIAJE_ID = "viaje_id"; // Para pasar ID entre Activities
```

---

## 5. Modelos y DTOs

Los DTOs (Data Transfer Objects) son clases Java planas que Gson mapea automáticamente desde el JSON del backend. No tienen lógica de negocio compleja, solo getters y algún helper.

### `ViajeDTO.java` — el DTO más importante

Además de los campos del viaje, incluye dos métodos de utilidad que simplifican la lógica de UI:

```java
// Devuelve true para estados intermedios (ACEPTADO → LLEGADA_ENTREGA)
public boolean estaEnCurso() {
    return estado != null && !estado.equals("PENDIENTE")
            && !estado.equals("COMPLETADO")
            && !estado.equals("CANCELADO");
}

// Devuelve true si el viaje ya terminó (bien o mal)
public boolean estaFinalizado() {
    return "COMPLETADO".equals(estado) || "CANCELADO".equals(estado);
}
```

Estos helpers se usan en múltiples pantallas para mostrar u ocultar botones y parar el polling del mapa.

### `LoginResponse.java`

Mapea la respuesta del login, que el backend devuelve como un objeto que envuelve el token JWT y los datos del usuario:

```java
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": { "id": 1, "nombre": "Juan", "rol": "CLIENTE", ... }
}
```

### Objetos de petición (`request/`)

Son objetos simples con constructor que representan el body JSON de las peticiones POST/PUT al backend. Ejemplo:

```java
// Se convierte automáticamente a JSON por Gson:
// {"tipoMercanciaId":1, "direccionRecogida":"...", "pesoKg":50.0, ...}
new CrearViajeRequest(tipoId, descripcion, recogida, entrega, peso)
```

---

## 6. Capa de red: Retrofit + JWT

### `RetrofitClient.java` — Singleton

Es una clase singleton que crea y mantiene una única instancia de Retrofit durante toda la vida de la app. La clave es el **interceptor JWT**: en cada petición HTTP, lee automáticamente el token de `SessionManager` y añade la cabecera `Authorization: Bearer {token}`.

```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        String token = SessionManager.getToken(ctx);
        Request.Builder builder = chain.request().newBuilder();
        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        return chain.proceed(builder.build());
    })
    .addInterceptor(logger)  // Imprime peticiones en Logcat
    .build();
```

Así ninguna Activity necesita preocuparse de añadir la cabecera manualmente. El interceptor lo hace siempre de forma transparente.

### `ApiService.java` — Interfaz de endpoints

Define todos los endpoints de la API como métodos Java. Retrofit genera el código HTTP internamente:

```java
// AUTH
@POST("auth/login")
Call<LoginResponse> login(@Body LoginRequest request);

@POST("auth/registro")
Call<UsuarioDTO> registro(@Body RegistroRequest request);

// VIAJES
@GET("viajes")
Call<List<ViajeDTO>> getMisViajes();

@GET("viajes/disponibles")
Call<List<ViajeDTO>> getViajesDisponibles();

@GET("viajes/{id}")
Call<ViajeDTO> getViaje(@Path("id") long id);

@POST("viajes")
Call<ViajeDTO> crearViaje(@Body CrearViajeRequest request);

@PUT("viajes/{id}/aceptar")
Call<ViajeDTO> aceptarViaje(@Path("id") long id, @Body AceptarViajeRequest request);

@PUT("viajes/{id}/estado")
Call<ViajeDTO> actualizarEstado(@Path("id") long id, @Body ActualizarEstadoRequest request);

@POST("viajes/{id}/simular")
Call<Void> simularViaje(@Path("id") long id);

// VEHÍCULOS
@GET("vehiculos/mis-vehiculos")
Call<List<VehiculoDTO>> getMisVehiculos();

@POST("vehiculos")
Call<VehiculoDTO> crearVehiculo(@Body CrearVehiculoRequest request);

// CATÁLOGO (sin token)
@GET("tipo-mercancia")
Call<List<TipoMercanciaDTO>> getTiposMercancia();
```

---

## 7. Sistema de sesión: SessionManager

`SessionManager` es una clase de utilidad estática que gestiona la sesión del usuario en `SharedPreferences`. No instancia objetos — todos los métodos son estáticos y reciben el `Context` como parámetro.

### Datos guardados

| Clave | Tipo | Valor |
|---|---|---|
| `jwt_token` | String | Token JWT del backend (24h de validez) |
| `user_id` | long | ID del usuario en la base de datos |
| `user_rol` | String | "CLIENTE" o "TRANSPORTISTA" |
| `user_nombre` | String | Nombre completo (para el saludo) |
| `dark_mode` | boolean | Preferencia de tema claro/oscuro |

### Flujo de sesión

```
1. Usuario hace login
2. Backend devuelve token + datos del usuario
3. SessionManager.guardarSesion() persiste todo en SharedPreferences
4. RetrofitClient lee el token automáticamente en cada petición
5. Al cerrar sesión: SessionManager.cerrarSesion() borra todo
   (preserva la preferencia de tema para que no se pierda)
6. Al relanzar la app: SplashActivity comprueba haySesion()
   y redirige sin pasar por login si el token existe
```

---

## 8. Flujo de navegación completo

```
SplashActivity (1.2 segundos)
    │
    ├── (sin sesión) ──→ LoginActivity
    │                         ├── → RegistroActivity
    │                         └── (login OK) → [según rol]
    │
    ├── (rol CLIENTE) ──→ HomeClienteActivity
    │                          ├── → CrearViajeActivity
    │                          └── → MisViajesClienteActivity
    │                                    └── → DetalleViajeActivity
    │                                              └── → MapaViajeActivity
    │
    └── (rol TRANSPORTISTA) ──→ HomeTransportistaActivity
                                    ├── → ViajesDisponiblesActivity
                                    │         └── → DetalleViajeActivity
                                    │                   └── → MapaViajeActivity
                                    ├── → MisViajesTransportistaActivity
                                    │         └── → DetalleViajeActivity
                                    └── → MisVehiculosActivity
                                               └── → CrearVehiculoActivity
```

### Comportamiento de `onResume()`

La mayoría de Activities que muestran listas las recargan en `onResume()`. Esto garantiza que al volver atrás (por ejemplo, después de crear un viaje), la lista se actualiza automáticamente sin que el usuario tenga que refrescar manualmente.

---

## 9. Pantallas implementadas (Activities)

### 9.1 SplashActivity

**Función:** Pantalla de carga inicial con el logo de PisPax durante 1200ms.

**Lógica:** Comprueba si hay sesión activa en `SessionManager`. Si la hay, redirige directamente al Home del rol correspondiente. Si no, va al Login. El usuario no necesita hacer login cada vez que abre la app.

```java
new Handler(Looper.getMainLooper()).postDelayed(this::redirigir, 1200);

// redirigir():
if (SessionManager.haySesion(this)) {
    destino = ROL_TRANSPORTISTA.equals(rol)
        ? HomeTransportistaActivity.class
        : HomeClienteActivity.class;
} else {
    destino = LoginActivity.class;
}
```

---

### 9.2 LoginActivity

**Función:** Autenticación de usuario con email y contraseña.

**Flujo:**
1. Usuario rellena email y contraseña
2. Se llama a `POST /api/auth/login`
3. Si el backend responde 200 OK con el token:
   - Se guarda la sesión con `SessionManager.guardarSesion()`
   - Se navega al Home del rol correspondiente
4. Si responde error → Toast con mensaje de error

**Características:**
- `ProgressBar` durante la petición para bloquear doble tap
- Toggle de tema (luna/sol) en esquina superior derecha
- Enlace directo a `RegistroActivity`
- `finish()` al navegar al Home para que el botón "atrás" no regrese al login

---

### 9.3 RegistroActivity

**Función:** Creación de nueva cuenta de cliente o transportista.

**Campos:** Nombre, Apellidos, Email, Contraseña (mín. 8 caracteres), Teléfono (opcional), Rol (RadioGroup con dos opciones).

**Validaciones locales:**
- Campos obligatorios no vacíos
- Contraseña mínimo 8 caracteres

**Flujo:** Al completar el registro, `finish()` regresa al login para que el usuario entre con sus credenciales recién creadas.

---

### 9.4 HomeClienteActivity

**Función:** Dashboard principal del cliente.

**Elementos:**
- Saludo personalizado: "Hola, {nombre}" (solo primer nombre, estilo Uber)
- Lista de **viajes activos** (no finalizados) en un `RecyclerView`
- Botón primario naranja "Nuevo viaje"
- Enlace "Ver todos mis viajes"
- Toggle de tema y botón de cerrar sesión

**Actualización automática:** `onResume()` recarga la lista, así al volver de `CrearViajeActivity` el nuevo viaje aparece de inmediato.

---

### 9.5 HomeTransportistaActivity

**Función:** Dashboard principal del transportista.

**Elementos:**
- Saludo personalizado
- Lista de **viajes en curso** (aceptados pero no completados)
- Botón principal "Ver viajes disponibles" → `ViajesDisponiblesActivity`
- Botones secundarios: Mis viajes, Mis vehículos

---

### 9.6 CrearViajeActivity

**Función:** Formulario para que el cliente solicite un nuevo transporte.

**Flujo:**
1. Al abrir, carga el catálogo de tipos de mercancía: `GET /api/tipo-mercancia`
2. El catálogo puebla un `Spinner` con los 12 tipos disponibles
3. El usuario rellena: dirección de recogida, dirección de entrega, peso
4. Al pulsar "Solicitar viaje": `POST /api/viajes`
5. Si 201 Created → Toast de éxito + `finish()` (vuelve al home que se refresca)
6. Si 403 → mensaje de error (solo clientes pueden crear viajes)

---

### 9.7 ViajesDisponiblesActivity

**Función:** Lista de viajes en estado PENDIENTE que el transportista puede aceptar.

**Características:**
- `RecyclerView` con `ViajesAdapter`
- Botón "Actualizar" para refrescar manualmente
- `onResume()` recarga automáticamente (al volver del detalle de un viaje aceptado, el viaje desaparece de la lista)

---

### 9.8 MisViajesClienteActivity / MisViajesTransportistaActivity

**Función:** Lista completa de viajes del usuario (todos los estados, incluyendo completados y cancelados).

Ambas siguen el mismo patrón: `RecyclerView` + `onResume()` reload. La diferencia está solo en el título y en qué endpoint llaman (aunque ambos llaman a `GET /api/viajes` — el backend filtra por el JWT del token automáticamente).

---

### 9.9 MisVehiculosActivity

**Función:** Lista de vehículos del transportista.

- `RecyclerView` con `VehiculosAdapter` en modo lista (sin checkbox)
- FAB naranja en esquina inferior derecha para añadir un nuevo vehículo
- Click en un vehículo muestra un Toast con los datos del vehículo

---

### 9.10 CrearVehiculoActivity

**Función:** Formulario de alta de un nuevo vehículo.

**Campos:**
- Matrícula (se convierte a mayúsculas automáticamente)
- Marca y Modelo
- Tipo de vehículo (Spinner con 7 tipos: Furgoneta, Furgón Grande, Frigorífico, Camión Ligero, Camión Pesado, Camión Articulado, Plataforma)
- Subtipo (opcional, texto libre)
- Capacidad kg (obligatorio), Tara kg (opcional), MMA kg (opcional)
- Carnet requerido (Spinner: B, C1, C, C+E)

**Importante:** Los Spinners muestran etiquetas legibles al usuario pero envían los valores exactos del enum del backend (ej: "C+E" → `"C_E"` en la petición JSON).

---

### 9.11 DetalleViajeActivity

**La pantalla más compleja del frontend.** Muestra la ficha completa de un viaje y adapta los controles disponibles según el rol del usuario autenticado y el estado actual del viaje.

**Datos mostrados:**
- ID del viaje (#número)
- Estado actual (badge con color semántico)
- Dirección de recogida y entrega (cards separadas)
- Tipo de mercancía, descripción y peso
- Nombre del cliente, transportista y vehículo asignados

**Botones adaptativos:**

| Condición | Botón visible |
|---|---|
| Viaje en estado ACEPTADO en adelante (no cancelado) | "Ver en el mapa" |
| Rol TRANSPORTISTA + estado PENDIENTE | "Aceptar viaje" |
| Rol TRANSPORTISTA asignado + estado en curso | "Marcar: {siguiente estado}" |

**Flujo de aceptación de viaje:**
1. El transportista pulsa "Aceptar viaje"
2. Se lanza `GET /api/vehiculos/mis-vehiculos` para cargar sus vehículos
3. Se muestra un `AlertDialog` con la lista de vehículos en modo selector (con checkbox)
4. El transportista selecciona su vehículo y confirma
5. `PUT /api/viajes/{id}/aceptar` con el `vehiculoId`
6. La pantalla se actualiza automáticamente con el nuevo estado

**Flujo de avance de estado:**
La secuencia de estados está definida localmente:
```
ACEPTADO → SALIDA_RECOGIDA → LLEGADA_RECOGIDA → MERCANCIA_RECOGIDA
         → SALIDA_ENTREGA → LLEGADA_ENTREGA → COMPLETADO
```

El botón siempre muestra el nombre legible del siguiente estado: "Marcar: En camino a recogida", etc. Cada clic hace `PUT /api/viajes/{id}/estado` y la pantalla se redibuja con los nuevos datos.

---

### 9.12 MapaViajeActivity

**La pantalla más visual del TFG.** Muestra el seguimiento del viaje en un mapa real y permite iniciar la simulación automática.

**Estructura visual:**
- 55% superior: mapa OSMDroid con la ruta dibujada (línea naranja)
- 45% inferior: panel deslizante con timeline de estados y botón de simulación

**Elementos del mapa:**
- Marcador "A" en el punto de recogida (Terrassa, Barcelona)
- Marcador "B" en el punto de entrega (Mercamadrid, Madrid)
- Marcador del camión "🚛 PisPax" que se desplaza según el estado
- Línea de ruta naranja entre A y B

**Movimiento del marcador:**
El camión se posiciona en la ruta mediante interpolación lineal. Cada estado tiene asignado un porcentaje de la ruta (0% = origen, 100% = destino):

| Estado | Posición en la ruta |
|---|---|
| ACEPTADO | 0% (origen) |
| SALIDA_RECOGIDA | 25% |
| LLEGADA_RECOGIDA | 40% |
| MERCANCIA_RECOGIDA | 40% (parado, cargando) |
| SALIDA_ENTREGA | 75% |
| LLEGADA_ENTREGA | 100% (destino) |
| COMPLETADO | 100% |

Cuando el estado cambia, `ValueAnimator` anima el marcador suavemente durante 1.5 segundos.

**Timeline de estados:**
`RecyclerView` vertical con `TimelineAdapter`. Cada estado tiene:
- Punto circular: gris (pendiente) / naranja parpadeante (actual) / verde (completado)
- Nombre legible del estado
- Línea de conexión vertical (oculta en el último elemento)

**Polling cada 3 segundos:**
```java
private static final int POLLING_INTERVAL_MS = 3000;

// pollingRunnable:
RetrofitClient.getApi().getViaje(viajeId).enqueue(callback);
// Si sigue activo y no finalizado: schedula de nuevo

// Se detiene cuando:
// - El viaje está COMPLETADO o CANCELADO
// - La Activity pasa a onPause()
// Se reanuda cuando la Activity vuelve a onResume()
```

**Botón "Iniciar simulación":**
Solo visible para el transportista. Llama a `POST /api/viajes/{id}/simular`:
- El backend inicia un proceso `@Async` que avanza los estados automáticamente
- La respuesta llega inmediatamente (200 OK)
- El polling detecta los cambios de estado y actualiza el mapa en tiempo real
- El botón desaparece tras activar la simulación

---

## 10. Adaptadores (RecyclerView y Spinner)

### ViajesAdapter

**Usado en:** HomeCliente, HomeTransportista, MisViajesCliente, MisViajesTransportista, ViajesDisponibles — un solo adaptador genérico para todas las pantallas.

**Interface de callback:** `OnViajeClickListener` permite que cada Activity defina qué hacer al hacer tap en un viaje (normalmente navegar al detalle).

**Colores de estado:**
Cada tarjeta tiene una línea lateral de color y un badge, ambos en el color semántico del estado:
- Naranja suave = PENDIENTE
- Azul claro = estados intermedios (en curso)
- Verde = COMPLETADO
- Rojo suave = CANCELADO

En modo oscuro, estos colores están adaptados para mantener el contraste sobre fondos oscuros.

### VehiculosAdapter

**Dos modos de uso:**
1. **Modo lista** (MisVehiculosActivity): muestra los vehículos sin checkbox
2. **Modo selector** (selector de vehículo al aceptar un viaje): muestra checkbox, resalta el seleccionado

**Información mostrada:** Marca + Modelo + Matrícula, Tipo de vehículo (en texto legible: "Furgón Grande" no "FURGON_GRANDE"), Capacidad en kg, Carnet requerido (con "C+E" en lugar de "C_E").

### TipoMercanciaAdapter

Extiende `ArrayAdapter<TipoMercanciaDTO>` para poblar el Spinner de creación de viajes. `TipoMercanciaDTO.toString()` devuelve el nombre del tipo, lo que hace que el Spinner muestre los nombres automáticamente.

### TimelineAdapter

Muestra la secuencia de 7 estados del viaje (de ACEPTADO a COMPLETADO). Cada ítem sabe si está pendiente, en curso o ya completado y aplica el estilo correspondiente. La línea de conexión se oculta en el último elemento.

---

## 11. Sistema de temas: claro y oscuro

### Paleta de colores

| Color | Modo claro | Modo oscuro | Uso |
|---|---|---|---|
| Naranja principal | `#FF8E3B` | `#FF8E3B` | Botones, badges, FAB |
| Navy secundario | `#2D2850` | `#3D3B5A` | Cabeceras (en Splash) |
| Fondo principal | `#F7F7F7` | `#121212` | Fondo de pantallas |
| Fondo card | `#FFFFFF` | `#1E1E1E` | Cards y containers |
| Texto primario | `#1A1A1A` | `#EFEFEF` | Títulos y datos |
| Texto secundario | `#757575` | `#9E9E9E` | Subtítulos y hints |

### Cómo funciona el toggle de tema

1. El usuario pulsa el botón 🌙/☀️ en cualquier pantalla
2. `SessionManager.setDarkMode()` guarda la preferencia
3. `AppCompatDelegate.setDefaultNightMode()` cambia el modo
4. `recreate()` reinicia la Activity aplicando el nuevo tema
5. `PispaxApplication.applyTheme()` garantiza que el tema correcto se aplica al relanzar la app

El tema usa `DayNight` de Material Components, que hace que Android seleccione automáticamente los recursos de `values/` (claro) o `values-night/` (oscuro).

---

## 12. Simulación visual del viaje (OSMDroid)

La simulación es la característica más llamativa del TFG. Permite demostrar el ciclo completo de un viaje en ~38 segundos sin necesidad de un vehículo real.

### Arquitectura de la simulación

```
[Android: Botón "Iniciar simulación"]
         │
         │ POST /api/viajes/{id}/simular
         ▼
[Backend: responde 200 OK inmediatamente]
         │
         │ Proceso @Async en background:
         │ PENDIENTE → ACEPTADO (4s)
         │ → SALIDA_RECOGIDA (5s)
         │ → LLEGADA_RECOGIDA (4s)
         │ → MERCANCIA_RECOGIDA (5s)
         │ → SALIDA_ENTREGA (6s)
         │ → LLEGADA_ENTREGA (4s)
         │ → COMPLETADO (3s)
         │
[Android: polling cada 3s]
         │ GET /api/viajes/{id}
         ▼
actualizarUI() → anima marcador → actualiza timeline → para polling si COMPLETADO
```

### Coordenadas de la demo

La ruta está hardcodeada para la demo del TFG:
- **Origen:** Polígono Can Fontanet, Terrassa (Barcelona) — `41.5638, 2.0085`
- **Destino:** Centro Logístico Mercamadrid, Madrid — `40.3838, -3.6765`

Esta ruta real de ~600km hace que la demo sea visualmente impactante.

---

## 13. Decisiones de diseño destacables

### 1. Stateless — sin base de datos local

La app no usa SQLite ni Room. Todo se consulta al backend cuando se necesita. Esto simplifica enormemente el código (sin sincronización, sin cachés) y es consistente con la filosofía MVP.

### 2. Singleton de Retrofit

Un único cliente HTTP para toda la app, con el interceptor JWT configurado una sola vez. No hay riesgo de olvidarse de añadir el token en alguna petición.

### 3. ContextCompat para colores

Todos los colores de recursos se leen con `ContextCompat.getColor(ctx, R.color.xxx)` en lugar de `ctx.getColor(R.color.xxx)`. Esto garantiza compatibilidad con Android 5.0 y 5.1 (API 21-22), que no tienen el método `getColor()` en `Context`.

### 4. onResume() como refresh point

En lugar de añadir `startActivityForResult()` y sus callbacks, todas las listas se recargan en `onResume()`. Patrón simple y consistente que funciona en todos los flujos de navegación.

### 5. DTOs con helpers

`ViajeDTO.estaEnCurso()` y `ViajeDTO.estaFinalizado()` centralizan la lógica de estado. Sin ellos, la misma comprobación `!PENDIENTE && !COMPLETADO && !CANCELADO` estaría duplicada en 5 sitios distintos.

### 6. ViajesAdapter genérico

Un único adaptador sirve 5 pantallas distintas. La interfaz `OnViajeClickListener` desacopla el adaptador de la lógica de navegación. Cada Activity decide qué hacer al hacer tap.

---

## 14. Cómo funciona la conexión con el backend

### En el emulador de Android Studio

El emulador tiene una red virtual. `10.0.2.2` es la dirección especial que mapea al `localhost` del PC donde corre el emulador. Por eso la `BASE_URL` es `http://10.0.2.2:8080/api/`.

### En un dispositivo físico

Si se usa un móvil real, el dispositivo y el PC deben estar en la misma red WiFi. La `BASE_URL` debe cambiarse a la IP local del PC (ej: `http://192.168.1.100:8080/api/`).

### Ciclo completo de una petición autenticada

```
1. Activity llama a:
   RetrofitClient.getInstance(this).getApi().getMisViajes()

2. RetrofitClient ya tiene el cliente OkHttp configurado con el interceptor.
   El interceptor lee SessionManager.getToken() → "eyJhbGciOi..."

3. OkHttp construye la petición HTTP:
   GET http://10.0.2.2:8080/api/viajes
   Authorization: Bearer eyJhbGciOi...

4. Backend recibe la petición:
   - JwtAuthFilter valida el token
   - Extrae el userId y rol del payload
   - El controller responde con la lista filtrada por rol

5. OkHttp recibe la respuesta JSON
6. Gson convierte automáticamente JSON → List<ViajeDTO>
7. onResponse() del Callback se ejecuta en el hilo principal
8. La Activity actualiza el RecyclerView con los datos
```

---

## 15. Cómo explicarlo en la defensa del TFG

### Estructura recomendada para la demo en vivo

1. **Mostrar registro e inicio de sesión** (60s)
   - Registrar un usuario CLIENTE
   - Registrar un usuario TRANSPORTISTA
   - Hacer login como CLIENTE → mostrar el Home

2. **Ciclo completo de un viaje** (2-3 min)
   - Como CLIENTE: crear un viaje (seleccionar mercancía, ruta, peso)
   - Mostrar que el viaje aparece en "Mis viajes" en estado PENDIENTE
   - Cambiar de sesión (cerrar sesión, login como TRANSPORTISTA)
   - Como TRANSPORTISTA: ir a "Ver viajes disponibles" → aparece el viaje
   - Aceptar el viaje (seleccionar vehículo)
   - Mostrar que el estado cambió a ACEPTADO

3. **Simulación en el mapa** (2 min)
   - Abrir el detalle del viaje → pulsar "Ver en el mapa"
   - Mostrar el mapa con la ruta Terrassa → Madrid
   - Pulsar "Iniciar simulación"
   - Ver cómo el camión se desplaza por el mapa y el timeline avanza automáticamente
   - Esperar a COMPLETADO (~38 segundos)

4. **Dark mode** (15s)
   - Pulsar el toggle 🌙 para mostrar el modo oscuro en acción

### Puntos técnicos a destacar ante el tribunal

**Sobre la arquitectura:**
> "El frontend es completamente stateless — solo guarda el token JWT localmente. Toda la lógica de negocio está en el backend. Esta decisión simplifica el código Android y hace el sistema escalable."

**Sobre la seguridad:**
> "Todas las peticiones al backend llevan el token JWT en la cabecera Authorization, que se añade automáticamente gracias al interceptor de OkHttp. El backend valida el token antes de procesar cualquier petición."

**Sobre el diseño por roles:**
> "Los paquetes `cliente/` y `transportista/` separan los flujos de cada rol. La pantalla de detalle y el mapa son compartidas, pero adaptan sus botones leyendo el rol del SessionManager."

**Sobre el mapa y la simulación:**
> "Para la demo usé OSMDroid — OpenStreetMap, sin necesidad de API key de Google. El backend procesa la simulación de forma asíncrona con @Async de Spring, y el móvil hace polling cada 3 segundos para detectar los cambios de estado y animar el marcador."

**Sobre la compatibilidad:**
> "El minSdk es 21, que cubre el 98% de dispositivos Android activos. Usé ContextCompat para la lectura de colores, que garantiza compatibilidad con Android 5.0 y 5.1."

**Sobre el patrón MVP:**
> "Esta es la primera versión funcional del producto. Funcionalidades como pagos, geolocalización GPS real, notificaciones push o algoritmos de optimización de rutas están documentadas como líneas de evolución futura, fuera del alcance del MVP."

### Posibles preguntas del tribunal y cómo responderlas

**P: "¿Por qué no usaste Kotlin?"**
> R: "El ciclo de DAM trabaja principalmente con Java. Mantener el mismo lenguaje en backend y frontend redujo la curva de aprendizaje y simplificó la gestión del proyecto."

**P: "¿Cómo garantizas que solo el CLIENTE crea viajes?"**
> R: "En el backend, los endpoints están protegidos con @PreAuthorize('hasRole(CLIENTE)'). Aunque el frontend también filtra la UI por rol, la seguridad real está en el servidor. Sin un JWT válido de CLIENTE, el backend devuelve 403."

**P: "¿Por qué no se persisten datos localmente con SQLite?"**
> R: "En una app de transporte en tiempo real, los datos cambian constantemente — el estado de un viaje puede avanzar mientras el usuario tiene la app abierta. SQLite local introduciría problemas de sincronización. El polling de la pantalla de mapa es la solución más sencilla y correcta para este caso de uso."

**P: "¿El mapa usa datos de Google Maps?"**
> R: "No. Uso OSMDroid con tiles de OpenStreetMap, que es completamente gratuito y de código abierto. No requiere API key ni billing. Para una versión de producción se podría migrar a Google Maps o Mapbox."

---

*Documento generado el 08/06/2026 como parte del TFG PisPax — Adrián Salazar*
