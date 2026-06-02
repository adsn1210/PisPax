# Estructura Frontend Android — PisPax
### TFG DAM 2025/2026 — Adrián Salazar

> **Estado:** Estructura esqueleto creada (sin implementación UI).
> El diseño de cada pantalla se completará en la siguiente fase.

---

## Por qué se crea este módulo aquí

El proyecto ya tiene `pispax-backend/` en la raíz del repositorio.
El frontend se crea como `pispax-android/` al mismo nivel para mantener coherencia:

```
Code-Pispax/
├── docs/
├── pispax-backend/     ← Spring Boot (ya existía)
└── pispax-android/     ← Android nativo (nuevo)
```

Ambos módulos comparten el mismo repositorio Git pero son proyectos independientes.
El backend se arranca con Maven; el Android con Android Studio.

---

## Decisiones de diseño del proyecto Android

### Package raíz: `com.pispax.app`
Coherente con el backend (`com.pixpax.app`). Nombre de paquete en minúsculas según
convención Java/Android.

### `minSdk 21` (Android 5.0)
Cubre más del 98 % de dispositivos activos según los requisitos no funcionales del TFG
(RNF-05). Suficiente para todas las APIs usadas (Retrofit, RecyclerView, OSMDroid).

### `compileSdk / targetSdk 34` (Android 14)
Versión estable más reciente al momento del desarrollo. Garantiza acceso a las últimas
APIs de Material Design 3 y permite publicar en Google Play si llegara a ese punto.

### Java 17 (no Kotlin)
El proyecto completo (backend + frontend) usa Java 17 para coherencia de stack y para
aprovechar los conocimientos del alumno (DAM con Java). Kotlin queda fuera del alcance MVP.

### Material Components + NoActionBar
Se usa `Theme.MaterialComponents.DayNight.NoActionBar` porque PisPax implementa su
propia barra de navegación/cabecera con la identidad visual corporativa (naranja #FF8E3B
+ navy #2D2850). La ActionBar de sistema se elimina para tener control total.

---

## Archivos de configuración del proyecto

### `settings.gradle`
Define el nombre del proyecto (`pispax-android`) e incluye el módulo `:app`.
Usa el sistema de gestión de dependencias centralizado de Gradle 7+.

### `build.gradle` (nivel proyecto)
Solo declara el plugin `com.android.application`. No añade dependencias aquí —
eso va en el `build.gradle` del módulo `:app` para mantener el proyecto limpio.

### `gradle.properties`
- `android.useAndroidX=true` — activa las librerías AndroidX modernas
- `android.enableJetifier=true` — convierte librerías antiguas para usar AndroidX
- `org.gradle.jvmargs=-Xmx2048m` — evita errores de OutOfMemory en builds grandes

### `gradle/wrapper/gradle-wrapper.properties`
Fija la versión de Gradle en `8.4` para garantizar builds reproducibles
independientemente del entorno del desarrollador. Android Studio descarga el wrapper
automáticamente al sincronizar el proyecto por primera vez.

### `app/build.gradle` — Dependencias incluidas

| Librería | Versión | Por qué |
|---|---|---|
| `androidx.appcompat` | 1.6.1 | Compatibilidad con versiones antiguas de Android |
| `material` | 1.11.0 | Componentes UI Material Design (botones, cards, inputs) |
| `constraintlayout` | 2.1.4 | Layouts flexibles sin anidar vistas |
| `recyclerview` | 1.3.2 | Listas dinámicas eficientes (viajes, vehículos) |
| `retrofit2` | 2.9.0 | Cliente HTTP tipado para consumir la API REST del backend |
| `converter-gson` | 2.9.0 | Serialización automática JSON ↔ DTO Java |
| `okhttp3` | 4.12.0 | Cliente HTTP base de Retrofit (añade interceptor para JWT) |
| `logging-interceptor` | 4.12.0 | Muestra peticiones HTTP en Logcat (solo desarrollo) |
| `osmdroid-android` | 6.1.18 | Mapa OpenStreetMap gratuito sin API key para simulación |
| `lottie` | 6.4.0 | Animaciones JSON (celebración al completar viaje, carga) |

### `AndroidManifest.xml`
- `INTERNET` + `ACCESS_NETWORK_STATE` — necesarios para Retrofit
- `WRITE/READ_EXTERNAL_STORAGE` (hasta SDK 28/32) — caché de tiles de OSMDroid
- `android:usesCleartextTraffic="true"` — permite HTTP sin TLS en desarrollo local
  (el backend corre en `http://10.0.2.2:8080` desde el emulador). **En producción
  esto debe cambiarse a HTTPS y eliminarse este flag.**
- Actividad launcher: `SplashActivity`

---

## Estructura de paquetes Java

```
com.pispax.app/
├── PispaxApplication.java          ← Application global (init OSMDroid)
│
├── model/                          ← DTOs que mapean las respuestas del backend
│   ├── UsuarioDTO.java
│   ├── ViajeDTO.java
│   ├── VehiculoDTO.java
│   ├── TipoMercanciaDTO.java
│   ├── LoginResponse.java          ← Envuelve token + UsuarioDTO del login
│   └── request/                    ← Objetos que se envían al backend (body de POST/PUT)
│       ├── LoginRequest.java
│       ├── RegistroRequest.java
│       ├── CrearViajeRequest.java
│       ├── CrearVehiculoRequest.java
│       ├── AceptarViajeRequest.java
│       └── ActualizarEstadoRequest.java
│
├── network/                        ← Capa de red
│   ├── ApiService.java             ← Interfaz Retrofit con todos los endpoints
│   └── RetrofitClient.java         ← Singleton que crea el cliente con JWT interceptor
│
├── ui/                             ← Activities y Adapters separados por rol
│   ├── auth/                       ← Pantallas sin sesión (acceso libre)
│   │   ├── SplashActivity.java     ← Pantalla inicial: comprueba sesión guardada
│   │   ├── LoginActivity.java      ← Login con email + contraseña
│   │   └── RegistroActivity.java   ← Registro con rol CLIENTE o TRANSPORTISTA
│   │
│   ├── cliente/                    ← Solo accesible con rol CLIENTE
│   │   ├── HomeClienteActivity.java        ← Dashboard del cliente
│   │   ├── CrearViajeActivity.java         ← Formulario nuevo viaje
│   │   └── MisViajesClienteActivity.java   ← Lista de mis viajes + estados
│   │
│   ├── transportista/              ← Solo accesible con rol TRANSPORTISTA
│   │   ├── HomeTransportistaActivity.java          ← Dashboard del transportista
│   │   ├── ViajesDisponiblesActivity.java          ← Viajes PENDIENTES compatibles
│   │   ├── MisViajesTransportistaActivity.java     ← Viajes aceptados por mí
│   │   ├── MisVehiculosActivity.java               ← Lista de mis vehículos
│   │   └── CrearVehiculoActivity.java              ← Formulario alta vehículo
│   │
│   ├── shared/                     ← Accesibles desde ambos roles
│   │   ├── DetalleViajeActivity.java   ← Ficha completa de un viaje
│   │   └── MapaViajeActivity.java      ← Simulación visual en mapa OSMDroid
│   │
│   └── adapter/                    ← Adaptadores para RecyclerView
│       ├── ViajesAdapter.java
│       ├── VehiculosAdapter.java
│       └── TipoMercanciaAdapter.java   ← Para el Spinner/lista de tipos al crear viaje
│
└── util/                           ← Utilidades transversales
    ├── SessionManager.java         ← Lee/escribe JWT + datos usuario en SharedPreferences
    └── Constants.java              ← BASE_URL y otras constantes globales
```

### Por qué separar `cliente/` y `transportista/`

Cada rol tiene un flujo de uso completamente distinto:
- El CLIENTE crea viajes y consulta su estado.
- El TRANSPORTISTA busca viajes disponibles, los acepta y avanza estados.

Mezclar ambos flujos en las mismas Activities generaría condicionales `if (rol == CLIENTE)`
por toda la UI. Separar por paquete hace el código más claro y facilita el mantenimiento.

### Por qué existe `shared/`

`DetalleViajeActivity` y `MapaViajeActivity` son pantallas que tanto el cliente
(ve el estado de su viaje) como el transportista (avanza el estado) necesitan ver.
En lugar de duplicarlas, se parametrizan por Intent: la pantalla adapta su UI
según el rol guardado en `SessionManager`.

---

## Flujo de navegación

```
SplashActivity
    │
    ├── (sin sesión guardada) ──► LoginActivity ──► RegistroActivity
    │                                  │
    │                                  ▼ (login exitoso)
    │                           comprueba rol
    │
    ├── (rol == CLIENTE)  ──► HomeClienteActivity
    │                              ├── CrearViajeActivity
    │                              └── MisViajesClienteActivity
    │                                      └── DetalleViajeActivity
    │                                              └── MapaViajeActivity
    │
    └── (rol == TRANSPORTISTA) ──► HomeTransportistaActivity
                                       ├── ViajesDisponiblesActivity
                                       │       └── DetalleViajeActivity
                                       │               └── MapaViajeActivity
                                       ├── MisViajesTransportistaActivity
                                       │       └── DetalleViajeActivity
                                       └── MisVehiculosActivity
                                               └── CrearVehiculoActivity
```

**`SplashActivity`** es la clave del flujo: al arrancar comprueba si hay un token
válido en `SessionManager`. Si existe, redirige directamente al Home del rol correspondiente
sin pasar por Login. Esto es la experiencia "Uber-like" donde la app recuerda la sesión.

---

## Layouts XML creados

Todos los layouts se crean como esqueletos vacíos (solo el nodo raíz). El diseño
visual se implementará en la siguiente fase.

| Archivo | Activity | Notas |
|---|---|---|
| `activity_splash.xml` | SplashActivity | Logo + fondo naranja |
| `activity_login.xml` | LoginActivity | Email, password, botón |
| `activity_registro.xml` | RegistroActivity | Formulario completo + selector de rol |
| `activity_home_cliente.xml` | HomeClienteActivity | Dashboard cliente |
| `activity_home_transportista.xml` | HomeTransportistaActivity | Dashboard transportista |
| `activity_crear_viaje.xml` | CrearViajeActivity | Formulario de nuevo viaje |
| `activity_mis_viajes_cliente.xml` | MisViajesClienteActivity | RecyclerView de viajes |
| `activity_viajes_disponibles.xml` | ViajesDisponiblesActivity | RecyclerView de viajes |
| `activity_mis_viajes_transportista.xml` | MisViajesTransportistaActivity | RecyclerView de viajes |
| `activity_mis_vehiculos.xml` | MisVehiculosActivity | RecyclerView de vehículos |
| `activity_crear_vehiculo.xml` | CrearVehiculoActivity | Formulario de nuevo vehículo |
| `activity_detalle_viaje.xml` | DetalleViajeActivity | Ficha + botones de estado |
| `activity_mapa_viaje.xml` | MapaViajeActivity | MapView OSMDroid + timeline |
| `item_viaje.xml` | ViajesAdapter | Tarjeta de un viaje en lista |
| `item_vehiculo.xml` | VehiculosAdapter | Tarjeta de un vehículo en lista |

---

## Recursos de valores (`res/values/`)

### `colors.xml` — Identidad visual PisPax

| Nombre | Hex | Uso |
|---|---|---|
| `naranja_principal` | `#FF8E3B` | Botones primarios, cabeceras, elementos de acción |
| `navy_secundario` | `#2D2850` | Status bar, toolbar, textos de énfasis |
| `blanco` | `#FFFFFF` | Fondos de cards, textos sobre fondo oscuro |
| `bg_gris` | `#F5F5F5` | Fondo general de las pantallas |
| `texto_oscuro` | `#333333` | Texto principal del cuerpo |

### `themes.xml`
Tema base `Theme.MaterialComponents.DayNight.NoActionBar` con:
- `colorPrimary` → naranja (#FF8E3B)
- `colorSecondary` → navy (#2D2850)
- `statusBarColor` → navy (#2D2850)

### `strings.xml`
Solo contiene `app_name = "PisPax"`. Los demás strings se añadirán al implementar cada pantalla.

### `dimens.xml`
Espaciados y tamaños de texto estandarizados:
- `padding_small`: 8dp / `padding_normal`: 16dp / `padding_large`: 24dp
- `corner_radius`: 12dp (bordes redondeados de cards y botones)
- `text_title`: 22sp / `text_body`: 16sp / `text_caption`: 12sp

---

## Conexión con el backend

### URL base (constante en `Constants.java`)
```
http://10.0.2.2:8080/api/     ← Emulador Android (mapea a localhost del PC)
http://192.168.X.X:8080/api/  ← Dispositivo físico (IP WiFi del PC con el backend)
```

### Cómo funciona el JWT en Retrofit
`RetrofitClient.java` añade un `OkHttpClient` con un interceptor que lee
el token de `SessionManager` y lo inyecta automáticamente en cada petición:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
Así ninguna Activity tiene que preocuparse de añadir la cabecera manualmente.

---

## Pendiente (próxima fase)

- [ ] Diseño visual de cada pantalla (layouts XML)
- [ ] Implementación de `SessionManager` (SharedPreferences)
- [ ] Implementación de `RetrofitClient` + `ApiService`
- [ ] Implementación de DTOs con campos reales
- [ ] Implementación de cada Activity con lógica Retrofit
- [ ] Implementación de Adapters con ViewHolder
- [ ] `MapaViajeActivity` con OSMDroid + polling (ver `PLANTEAMIENTO_SIMULACION_VIAJE.md`)
- [ ] Correcciones pendientes en el backend antes de integrar:
  - Añadir `@EnableMethodSecurity` en `SecurityConfig.java`
  - Poblar `vehiculo_tipo_mercancia` al crear vehículo
  - Añadir `spring.jpa.open-in-view=false` en `application.properties`

---

*Documento generado el 02/06/2026 como parte del TFG PisPax — Adrián Salazar*
