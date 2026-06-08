# Documentación Técnica Unificada — Frontend Android PisPax
### TFG DAM 2025/2026 — Adrián Salazar Nicolás
### Versión unificada — Junio 2026

---

## Índice

1. [Visión general del frontend](#1-visión-general-del-frontend)
2. [Stack tecnológico y dependencias](#2-stack-tecnológico-y-dependencias)
3. [Estructura del proyecto y paquetes](#3-estructura-del-proyecto-y-paquetes)
4. [Configuración del proyecto Android](#4-configuración-del-proyecto-android)
5. [Permisos y AndroidManifest](#5-permisos-y-androidmanifest)
6. [Capa de infraestructura](#6-capa-de-infraestructura)
7. [Sistema de sesión — SessionManager](#7-sistema-de-sesión--sessionmanager)
8. [Capa de red — RetrofitClient y ApiService](#8-capa-de-red--retrofitclient-y-apiservice)
9. [Modelos y DTOs](#9-modelos-y-dtos)
10. [Flujo de navegación completo](#10-flujo-de-navegación-completo)
11. [Pantallas implementadas — Activities](#11-pantallas-implementadas--activities)
12. [Adaptadores — RecyclerView](#12-adaptadores--recyclerview)
13. [Sistema de temas claro y oscuro](#13-sistema-de-temas-claro-y-oscuro)
14. [Simulación visual del viaje — OSMDroid](#14-simulación-visual-del-viaje--osmdroid)
15. [Decisiones de diseño importantes](#15-decisiones-de-diseño-importantes)
16. [Conexión con el backend](#16-conexión-con-el-backend)
17. [Recursos de valores — colores, estilos, dimensiones](#17-recursos-de-valores--colores-estilos-dimensiones)
18. [Guía para la defensa del TFG](#18-guía-para-la-defensa-del-tfg)

---

## 1. Visión general del frontend

El frontend de PisPax es una **aplicación Android nativa** desarrollada en Java 17 con Android Studio. La app conecta visualmente a clientes y transportistas a través de una interfaz tipo "Uber": sencilla, inmediata, orientada al flujo de tareas del usuario.

La app es completamente **stateless desde el punto de vista de datos**: no tiene base de datos local. No usa SQLite ni Room. Toda la persistencia está en el backend. La única información guardada localmente es el token JWT y los datos básicos de la sesión del usuario, almacenados en `SharedPreferences` (sistema de clave-valor nativo de Android). Esta decisión simplifica enormemente el código y elimina los problemas de sincronización.

### Diagrama de arquitectura del frontend

```
┌──────────────────────────────────────────────────────────────────┐
│                      APP ANDROID (Frontend)                       │
│                                                                  │
│   UI: Activities (pantallas) + XML Layouts (vistas)              │
│          │                                                       │
│          │  Llaman a...                                          │
│          ▼                                                       │
│   SessionManager ←──────────────── SharedPreferences (local)     │
│          │                         (JWT, userId, rol, nombre)    │
│          │                                                       │
│   RetrofitClient (singleton)                                     │
│          │  OkHttpClient + Interceptor JWT                       │
│          ▼                                                       │
│   ApiService (interfaz Retrofit)                                 │
│          │  @GET / @POST / @PUT anotaciones                      │
└──────────┼───────────────────────────────────────────────────────┘
           │
           │  HTTP + JSON
           │  Authorization: Bearer {token}
           │
           ▼
    http://10.0.2.2:8080/api/       ← Emulador Android
    http://192.168.X.X:8080/api/    ← Dispositivo físico (misma WiFi)
           │
           ▼
    Backend Spring Boot (API REST)
    → Base de datos MySQL 8
```

### Estructura del repositorio

```
Code-Pispax/
├── pispax-backend/      ← Spring Boot (Maven)
└── pispax-android/      ← Android Studio (Gradle)
```

Ambos módulos son proyectos independientes en el mismo repositorio Git. El backend se arranca con Maven desde terminal; el Android con Android Studio.

---

## 2. Stack tecnológico y dependencias

| Componente | Tecnología | Versión | Justificación |
|---|---|---|---|
| **Lenguaje** | Java | 17 | Coherencia con el backend; conocimiento del alumno en DAM |
| **IDE** | Android Studio | 2024+ | Herramienta oficial de Android development |
| **SDK mínimo** | Android | 21 (Android 5.0) | Cubre el 98% de dispositivos activos según estadísticas |
| **SDK objetivo** | Android | 34 (Android 14) | Versión estable más reciente; acceso a APIs de Material Design 3 |
| **UI** | XML Layouts + Material Design | 1.11.0 | Estética moderna; componentes del sistema; tema DayNight |
| **Cliente HTTP** | Retrofit | 2.9.0 | Cliente tipado; integración nativa con Gson; callbacks simples |
| **Serialización JSON** | Gson (converter) | 2.9.0 | Mapeo automático JSON ↔ DTOs Java sin código manual |
| **HTTP base** | OkHttp3 | 4.12.0 | Cliente HTTP base de Retrofit; permite interceptores |
| **Logging HTTP** | OkHttp Logging Interceptor | 4.12.0 | Imprime peticiones/respuestas en Logcat (solo desarrollo) |
| **Sesión** | SharedPreferences | Nativo Android | Persistencia clave-valor para JWT y datos de sesión |
| **Listas dinámicas** | RecyclerView | 1.3.2 | Eficiente para listas largas; patrón ViewHolder |
| **Layouts** | ConstraintLayout | 2.1.4 | Layouts flexibles sin anidar vistas excesivamente |
| **Compatibilidad** | AndroidX AppCompat | 1.6.1 | Compatibilidad hacia atrás con Android 5.0+ |
| **Mapa** | OSMDroid | 6.1.18 | OpenStreetMap gratuito, sin API key, sin billing |
| **Animaciones** | Lottie | 6.4.0 | Animaciones JSON para celebración al completar viaje |

### Dependencias en `app/build.gradle`

```groovy
dependencies {
    // Android base
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'

    // Retrofit + OkHttp (HTTP y JSON)
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // Mapa OSMDroid (OpenStreetMap)
    implementation 'org.osmdroid:osmdroid-android:6.1.18'

    // Animaciones Lottie
    implementation 'com.airbnb.android:lottie:6.4.0'

    // Tests
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

---

## 3. Estructura del proyecto y paquetes

El proyecto sigue la convención estándar de Android con separación por dominio funcional:

```
pispax-android/
├── settings.gradle                  ← Define el nombre del proyecto y el módulo :app
├── build.gradle                     ← Plugin com.android.application (nivel proyecto)
├── gradle.properties                ← useAndroidX=true, enableJetifier=true, Xmx=2048m
├── gradle/wrapper/gradle-wrapper.properties  ← Fija Gradle en versión 8.4
│
└── app/
    ├── build.gradle                 ← Dependencias y configuración de la app
    └── src/main/
        ├── AndroidManifest.xml      ← Permisos, activities, configuración de la app
        │
        ├── java/com/pispax/app/
        │   │
        │   ├── PispaxApplication.java          ← Application global: init OSMDroid + tema
        │   │
        │   ├── model/                          ← DTOs: mapeo JSON del backend
        │   │   ├── LoginResponse.java          ← Envuelve token + UsuarioDTO
        │   │   ├── UsuarioDTO.java
        │   │   ├── ViajeDTO.java               ← Con helpers: estaEnCurso(), estaFinalizado()
        │   │   ├── VehiculoDTO.java
        │   │   ├── TipoMercanciaDTO.java
        │   │   └── request/                    ← Objetos enviados al backend (body POST/PUT)
        │   │       ├── LoginRequest.java
        │   │       ├── RegistroRequest.java
        │   │       ├── CrearViajeRequest.java
        │   │       ├── CrearVehiculoRequest.java
        │   │       ├── AceptarViajeRequest.java
        │   │       └── ActualizarEstadoRequest.java
        │   │
        │   ├── network/                        ← Capa de red (HTTP)
        │   │   ├── ApiService.java             ← Interfaz Retrofit: todos los endpoints
        │   │   └── RetrofitClient.java         ← Singleton: OkHttp + interceptor JWT
        │   │
        │   ├── ui/                             ← Pantallas (Activities) separadas por rol
        │   │   │
        │   │   ├── auth/                       ← Pantallas sin sesión (acceso libre)
        │   │   │   ├── SplashActivity.java     ← Pantalla inicial 1.2s; comprueba sesión
        │   │   │   ├── LoginActivity.java      ← Login con email + contraseña
        │   │   │   └── RegistroActivity.java   ← Registro con rol CLIENTE/TRANSPORTISTA
        │   │   │
        │   │   ├── cliente/                    ← Solo accesible con rol CLIENTE
        │   │   │   ├── HomeClienteActivity.java         ← Dashboard del cliente
        │   │   │   ├── CrearViajeActivity.java          ← Formulario de nueva solicitud
        │   │   │   └── MisViajesClienteActivity.java    ← Lista de todos mis viajes
        │   │   │
        │   │   ├── transportista/              ← Solo accesible con rol TRANSPORTISTA
        │   │   │   ├── HomeTransportistaActivity.java           ← Dashboard transportista
        │   │   │   ├── ViajesDisponiblesActivity.java           ← Viajes PENDIENTES
        │   │   │   ├── MisViajesTransportistaActivity.java      ← Viajes aceptados por mí
        │   │   │   ├── MisVehiculosActivity.java                ← Lista de mis vehículos
        │   │   │   └── CrearVehiculoActivity.java               ← Formulario nuevo vehículo
        │   │   │
        │   │   ├── shared/                     ← Pantallas compartidas por ambos roles
        │   │   │   ├── DetalleViajeActivity.java    ← Ficha completa; botones adaptativos
        │   │   │   └── MapaViajeActivity.java        ← Mapa OSMDroid + timeline + simulación
        │   │   │
        │   │   └── adapter/                    ← Adaptadores RecyclerView y Spinner
        │   │       ├── ViajesAdapter.java          ← Genérico; usado en 5 pantallas
        │   │       ├── VehiculosAdapter.java        ← Modos: lista y selector con checkbox
        │   │       ├── TipoMercanciaAdapter.java    ← Para Spinner en CrearViaje
        │   │       └── TimelineAdapter.java         ← Timeline de estados en MapaViaje
        │   │
        │   └── util/                           ← Utilidades transversales
        │       ├── SessionManager.java         ← JWT + datos usuario en SharedPreferences
        │       └── Constants.java             ← BASE_URL, claves, roles, extras de Intent
        │
        └── res/
            ├── layout/                         ← XMLs de pantallas e items
            ├── values/                         ← Colores, strings, estilos, dimensiones
            ├── values-night/                   ← Overrides para modo oscuro
            └── drawable/                       ← Iconos, imágenes, shapes
```

### Por qué se separan `cliente/`, `transportista/` y `shared/`

**`cliente/` y `transportista/`**: cada rol tiene un flujo de uso completamente distinto. Un cliente crea viajes y consulta su estado. Un transportista busca viajes disponibles, los acepta y avanza sus estados. Mezclar ambos flujos en las mismas Activities generaría lógica condicional (`if (rol == CLIENTE)`) por toda la UI, haciendo el código difícil de mantener. La separación hace cada pantalla más simple y enfocada.

**`shared/`**: `DetalleViajeActivity` y `MapaViajeActivity` son pantallas que ambos roles necesitan, pero con comportamiento diferente. En lugar de duplicar el código, se parametrizan: leen el rol del `SessionManager` y adaptan los botones visibles. Un cliente solo ve la información del viaje. Un transportista asignado puede además avanzar el estado.

---

## 4. Configuración del proyecto Android

### `settings.gradle`

Define el nombre del proyecto e incluye el módulo `:app`:

```groovy
rootProject.name = "pispax-android"
include ':app'
```

### `gradle.properties`

```properties
android.useAndroidX=true          # Activa librerías AndroidX modernas
android.enableJetifier=true       # Convierte librerías antiguas para usar AndroidX
org.gradle.jvmargs=-Xmx2048m     # Evita OutOfMemory en builds grandes
```

### `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
```

Fijar la versión de Gradle garantiza builds reproducibles en cualquier entorno. Android Studio descarga el wrapper automáticamente la primera vez.

### `app/build.gradle` (fragmento principal)

```groovy
android {
    namespace 'com.pispax.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.pispax.app"
        minSdk 21          // Android 5.0 — cubre 98% de dispositivos activos
        targetSdk 34       // Android 14
        versionCode 1
        versionName "1.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

---

## 5. Permisos y AndroidManifest

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Retrofit: llamadas HTTP al backend -->
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- Verificar conectividad antes de hacer peticiones -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- Caché de tiles OSMDroid (solo hasta API 28) -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />
    <!-- Leer caché de tiles OSMDroid (solo hasta API 32) -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />

    <application
        android:name=".PispaxApplication"
        android:label="PisPax"
        android:theme="@style/Theme.PisPax"
        android:usesCleartextTraffic="true"  <!-- Permite HTTP sin TLS en desarrollo local -->
        tools:targetApi="28">

        <!-- Activity launcher: SplashActivity es el punto de entrada -->
        <activity android:name=".ui.auth.SplashActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Resto de activities declaradas (sin intent-filter) -->
        <activity android:name=".ui.auth.LoginActivity" />
        <activity android:name=".ui.auth.RegistroActivity" />
        <activity android:name=".ui.cliente.HomeClienteActivity" />
        <!-- ... demás activities ... -->
    </application>
</manifest>
```

> **Nota de seguridad**: `android:usesCleartextTraffic="true"` permite HTTP sin TLS, necesario para la conexión a `http://10.0.2.2:8080` en el emulador de desarrollo. En una versión de producción, el backend usaría HTTPS y este flag debe eliminarse.

---

## 6. Capa de infraestructura

### `PispaxApplication.java`

Es la clase `Application` que Android instancia **antes que cualquier Activity**. Se encarga de dos inicializaciones globales:

```java
public class PispaxApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Inicializar OSMDroid con el User-Agent de la app
        // OSMDroid necesita esta configuración antes de poder usar el mapa
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // 2. Aplicar el tema guardado (claro u oscuro)
        // Se lee la preferencia antes de que cualquier pantalla se dibuje
        boolean darkMode = SessionManager.isDarkMode(this);
        AppCompatDelegate.setDefaultNightMode(
            darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
```

### `Constants.java`

Centraliza todas las constantes del proyecto para evitar magic strings dispersas:

```java
public class Constants {

    // URL base del backend
    // 10.0.2.2 es la dirección especial que el emulador Android usa para
    // conectar con localhost del PC donde corre el emulador
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    // Para dispositivo físico en la misma WiFi:
    // public static final String BASE_URL = "http://192.168.1.100:8080/api/";

    // Claves de SharedPreferences (sesión)
    public static final String PREFS_NAME    = "pispax_prefs";
    public static final String KEY_TOKEN     = "jwt_token";
    public static final String KEY_USER_ID   = "user_id";
    public static final String KEY_ROL       = "user_rol";
    public static final String KEY_NOMBRE    = "user_nombre";
    public static final String KEY_DARK_MODE = "dark_mode";

    // Claves para pasar datos entre Activities (Intent extras)
    public static final String EXTRA_VIAJE_ID = "viaje_id";

    // Roles (strings que devuelve el backend)
    public static final String ROL_CLIENTE       = "CLIENTE";
    public static final String ROL_TRANSPORTISTA = "TRANSPORTISTA";
}
```

---

## 7. Sistema de sesión — SessionManager

`SessionManager` es una clase de utilidad **estática** que gestiona la sesión del usuario en `SharedPreferences`. No instancia objetos — todos los métodos son estáticos y reciben el `Context` como parámetro. Esto la hace usable desde cualquier punto de la app sin necesidad de inyectarla.

### Datos guardados en SharedPreferences

| Clave | Tipo | Contenido |
|---|---|---|
| `jwt_token` | String | Token JWT devuelto por el backend (válido 24 horas) |
| `user_id` | long | ID del usuario en la base de datos del backend |
| `user_rol` | String | `"CLIENTE"` o `"TRANSPORTISTA"` |
| `user_nombre` | String | Nombre completo del usuario (para el saludo personalizado) |
| `dark_mode` | boolean | Preferencia de tema claro/oscuro (persiste entre sesiones) |

### Implementación

```java
public class SessionManager {

    // Guarda todos los datos de sesión tras un login exitoso
    public static void guardarSesion(Context ctx, String token, UsuarioDTO usuario) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences(
            Constants.PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(Constants.KEY_TOKEN,   token);
        editor.putLong(Constants.KEY_USER_ID,   usuario.getId());
        editor.putString(Constants.KEY_ROL,     usuario.getRol());
        editor.putString(Constants.KEY_NOMBRE,  usuario.getNombre() + " " + usuario.getApellidos());
        editor.apply();
    }

    // Borra todos los datos de sesión al cerrar sesión
    // NOTA: preserva la preferencia de tema para que no se pierda al hacer logout
    public static void cerrarSesion(Context ctx) {
        boolean darkMode = isDarkMode(ctx);
        ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply();
        setDarkMode(ctx, darkMode); // Restaura la preferencia de tema
    }

    // Comprueba si hay sesión activa (token presente)
    public static boolean haySesion(Context ctx) {
        return getToken(ctx) != null;
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(Constants.KEY_TOKEN, null);
    }

    public static String getRol(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(Constants.KEY_ROL, null);
    }

    public static long getUserId(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(Constants.KEY_USER_ID, -1);
    }

    public static String getNombre(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(Constants.KEY_NOMBRE, "");
    }

    // Métodos para el tema claro/oscuro
    public static boolean isDarkMode(Context ctx) {
        return ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(Constants.KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context ctx, boolean dark) {
        ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(Constants.KEY_DARK_MODE, dark).apply();
    }
}
```

### Flujo completo de sesión

```
1. Usuario hace login en LoginActivity
          │
          ▼
2. Backend responde 200 OK con {token, usuario}
          │
          ▼
3. SessionManager.guardarSesion(ctx, token, usuario)
   → Persiste en SharedPreferences
          │
          ▼
4. RetrofitClient interceptor lee SessionManager.getToken()
   → Lo añade automáticamente en Authorization: Bearer {token}
   → En CADA petición HTTP, de forma transparente
          │
          ▼
5. Al cerrar sesión: SessionManager.cerrarSesion()
   → Borra todo de SharedPreferences (preserva tema)
   → Redirige a LoginActivity
          │
          ▼
6. Al relanzar la app: SplashActivity comprueba haySesion()
   → Si hay token: redirige directamente al Home del rol (sin pasar por login)
   → Si no hay token: redirige a LoginActivity
```

---

## 8. Capa de red — RetrofitClient y ApiService

### `RetrofitClient.java` — Singleton con interceptor JWT

Es una clase singleton que crea y mantiene una única instancia de Retrofit durante toda la vida de la app. La clave es el **interceptor JWT**: en cada petición HTTP, lee automáticamente el token de `SessionManager` y añade la cabecera `Authorization: Bearer {token}`. Así ninguna Activity necesita preocuparse del token manualmente.

```java
public class RetrofitClient {

    private static Retrofit retrofit;

    public static ApiService getApi(Context ctx) {
        if (retrofit == null) {
            // Logger para Logcat (solo en debug)
            HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
            logger.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cliente HTTP con interceptor JWT
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = SessionManager.getToken(ctx);
                    Request.Builder builder = chain.request().newBuilder();
                    if (token != null) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    builder.addHeader("Content-Type", "application/json");
                    return chain.proceed(builder.build());
                })
                .addInterceptor(logger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

            // Construir Retrofit con la URL base y Gson como serializador
            retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)       // "http://10.0.2.2:8080/api/"
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(ApiService.class);
    }

    // Reiniciar el cliente (necesario si cambia la URL base)
    public static void reset() {
        retrofit = null;
    }
}
```

### `ApiService.java` — Interfaz de endpoints

Define todos los endpoints de la API como métodos Java anotados. Retrofit genera el código HTTP internamente. La correlación entre estos métodos y los endpoints del backend es uno a uno.

```java
public interface ApiService {

    // ── AUTH ───────────────────────────────────────────────────────
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/registro")
    Call<UsuarioDTO> registro(@Body RegistroRequest request);

    // ── CATÁLOGO (sin token) ───────────────────────────────────────
    @GET("tipo-mercancia")
    Call<List<TipoMercanciaDTO>> getTiposMercancia();

    // ── VIAJES ────────────────────────────────────────────────────
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

    // ── VEHÍCULOS ─────────────────────────────────────────────────
    @GET("vehiculos/mis-vehiculos")
    Call<List<VehiculoDTO>> getMisVehiculos();

    @POST("vehiculos")
    Call<VehiculoDTO> crearVehiculo(@Body CrearVehiculoRequest request);
}
```

### Patrón de uso en las Activities

Todas las llamadas al backend siguen el mismo patrón con callbacks de Retrofit:

```java
RetrofitClient.getApi(this).getMisViajes().enqueue(new Callback<List<ViajeDTO>>() {
    @Override
    public void onResponse(Call<List<ViajeDTO>> call, Response<List<ViajeDTO>> response) {
        if (response.isSuccessful() && response.body() != null) {
            // Éxito: actualizar la UI con los datos
            actualizarLista(response.body());
        } else {
            // Error HTTP (400, 403, 404, etc.)
            switch (response.code()) {
                case 401: cerrarSesionYRedirigir(); break;
                case 403: mostrarError("Sin permisos"); break;
                default:  mostrarError("Error del servidor: " + response.code()); break;
            }
        }
    }

    @Override
    public void onFailure(Call<List<ViajeDTO>> call, Throwable t) {
        // Error de red (sin conexión, timeout)
        mostrarError("Error de conexión: " + t.getMessage());
    }
});
```

Los callbacks de Retrofit se ejecutan en el **hilo principal** (main thread) cuando son respuestas HTTP, lo que permite actualizar la UI directamente sin `runOnUiThread()`.

---

## 9. Modelos y DTOs

Los DTOs (Data Transfer Objects) son clases Java simples que Gson mapea automáticamente desde/hacia el JSON del backend. Los nombres de los campos Java corresponden directamente a las claves del JSON (con la convención camelCase de Java).

### `ViajeDTO.java` — El DTO más importante

Además de los campos del viaje, incluye dos **métodos helper** que evitan duplicar lógica de estado en la UI:

```java
public class ViajeDTO {
    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private Long transportistaId;
    private String transportistaNombre;
    private Long vehiculoId;
    private String vehiculoMatricula;
    private Long tipoMercanciaId;
    private String tipoMercanciaNombre;
    private String descripcionMercancia;
    private String direccionRecogida;
    private String direccionEntrega;
    private BigDecimal pesoKg;
    private String estado;
    private String fechaSolicitud;
    private String fechaInicio;
    private String fechaFin;

    // Helper: true para estados intermedios (ACEPTADO → LLEGADA_ENTREGA)
    // Usado en UI para mostrar el botón "Ver en mapa" y para controlar el polling
    public boolean estaEnCurso() {
        return estado != null
            && !estado.equals("PENDIENTE")
            && !estado.equals("COMPLETADO")
            && !estado.equals("CANCELADO");
    }

    // Helper: true si el viaje ya terminó (bien o mal)
    // Usado en UI para parar el polling del mapa y mostrar el estado final
    public boolean estaFinalizado() {
        return "COMPLETADO".equals(estado) || "CANCELADO".equals(estado);
    }
}
```

Sin estos helpers, la misma comprobación `!PENDIENTE && !COMPLETADO && !CANCELADO` estaría duplicada en 5 lugares distintos de la UI.

### `LoginResponse.java`

Mapea la respuesta del login que el backend devuelve como un objeto que envuelve el token y el DTO del usuario:

```java
public class LoginResponse {
    private String token;
    private UsuarioDTO usuario;
    // getters/setters
}
```

### `VehiculoDTO.java`

```java
public class VehiculoDTO {
    private Long id;
    private String matricula;
    private String marca;
    private String modelo;
    private String tipoVehiculo;
    private String subtipo;
    private BigDecimal taraKg;
    private BigDecimal capacidadKg;
    private BigDecimal mmaKg;
    private String carnetRequerido;
    private Long transportistaId;
    // getters/setters

    // Helper para mostrar texto legible del tipo de vehículo en la UI
    public String getTipoVehiculoLegible() {
        switch (tipoVehiculo) {
            case "FURGON_GRANDE":      return "Furgón Grande";
            case "FRIGORIFICO":        return "Frigorífico";
            case "CAMION_LIGERO":      return "Camión Ligero";
            case "CAMION_PESADO":      return "Camión Pesado";
            case "CAMION_ARTICULADO":  return "Camión Articulado";
            default: return tipoVehiculo; // FURGONETA, PLATAFORMA
        }
    }

    // Muestra "C+E" en lugar del valor interno "C_E"
    public String getCarnetLegible() {
        return "C_E".equals(carnetRequerido) ? "C+E" : carnetRequerido;
    }
}
```

### Objetos de petición (`request/`)

Son clases simples con constructores que representan el body JSON de las peticiones POST/PUT:

```java
// CrearViajeRequest — se convierte a JSON automáticamente por Gson
// Resultado: {"tipoMercanciaId":1, "direccionRecogida":"...", "pesoKg":50.0}
public class CrearViajeRequest {
    private Long tipoMercanciaId;
    private String descripcionMercancia;
    private String direccionRecogida;
    private String direccionEntrega;
    private BigDecimal pesoKg;

    public CrearViajeRequest(Long tipoId, String desc, String recogida, String entrega, BigDecimal peso) {
        this.tipoMercanciaId = tipoId;
        this.descripcionMercancia = desc;
        this.direccionRecogida = recogida;
        this.direccionEntrega = entrega;
        this.pesoKg = peso;
    }
}

// AceptarViajeRequest — {"vehiculoId": 11}
public class AceptarViajeRequest {
    private Long vehiculoId;
    public AceptarViajeRequest(Long vehiculoId) { this.vehiculoId = vehiculoId; }
}

// ActualizarEstadoRequest — {"nuevoEstado": "SALIDA_RECOGIDA"}
public class ActualizarEstadoRequest {
    private String nuevoEstado;
    public ActualizarEstadoRequest(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}
```

---

## 10. Flujo de navegación completo

```
SplashActivity (1200ms, logo PisPax)
    │
    ├── (sin sesión → token nulo en SharedPreferences)
    │         └──► LoginActivity
    │                   ├── → RegistroActivity
    │                   │       └── (registro OK → finish() → vuelve a Login)
    │                   └── (login OK → comprueba rol)
    │
    ├── (rol == CLIENTE) ──────────────────────────────────────────────────
    │         └──► HomeClienteActivity
    │                   ├── "Nuevo viaje" ──► CrearViajeActivity
    │                   │                         └── (éxito → finish(), Home recarga en onResume)
    │                   └── "Mis viajes" ──► MisViajesClienteActivity
    │                                             └── [click viaje] ──► DetalleViajeActivity
    │                                                                         └── "Ver en mapa" ──► MapaViajeActivity
    │
    └── (rol == TRANSPORTISTA) ──────────────────────────────────────────
              └──► HomeTransportistaActivity
                        ├── "Ver viajes" ──► ViajesDisponiblesActivity
                        │                         └── [click viaje] ──► DetalleViajeActivity
                        │                                                     └── "Aceptar viaje" (dialog)
                        │                                                     └── "Ver en mapa" ──► MapaViajeActivity
                        │                                                           └── "Iniciar simulación"
                        ├── "Mis viajes" ──► MisViajesTransportistaActivity
                        │                         └── [click viaje] ──► DetalleViajeActivity
                        └── "Mis vehículos" ──► MisVehiculosActivity
                                                      └── [FAB] ──► CrearVehiculoActivity
```

### Patrón `onResume()` como punto de refresco

La mayoría de Activities que muestran listas recargan sus datos en `onResume()`. Esto garantiza que al volver atrás (por ejemplo, después de crear un viaje), la lista se actualiza automáticamente. No se necesita `startActivityForResult()` ni callbacks complejos.

```java
@Override
protected void onResume() {
    super.onResume();
    cargarMisViajes(); // Llama al API backend y actualiza el RecyclerView
}
```

---

## 11. Pantallas implementadas — Activities

### 11.1 SplashActivity

**Función**: Pantalla de carga inicial con el logo de PisPax durante 1200ms.

**Lógica**: Comprueba si hay sesión activa en `SessionManager.haySesion()`. Si la hay, lee el rol y redirige directamente al Home del rol correspondiente. Si no, va a `LoginActivity`. El usuario no necesita hacer login cada vez que abre la app.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        Intent intent;
        if (SessionManager.haySesion(this)) {
            String rol = SessionManager.getRol(this);
            intent = new Intent(this, Constants.ROL_TRANSPORTISTA.equals(rol)
                ? HomeTransportistaActivity.class
                : HomeClienteActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish(); // Evita volver al Splash con el botón "atrás"
    }, 1200);
}
```

---

### 11.2 LoginActivity

**Función**: Autenticación de usuario.

**Flujo**:
1. Usuario escribe email y contraseña
2. Se llama a `POST /api/auth/login`
3. Si 200 OK con token: `SessionManager.guardarSesion()` y navega al Home del rol
4. Si error: muestra un Toast con el mensaje de error del servidor

**Características**:
- `ProgressBar` durante la petición para indicar carga y evitar doble tap
- Toggle de tema (icono luna/sol) en la esquina superior derecha
- `finish()` al navegar al Home para que el botón "atrás" no regrese al login

---

### 11.3 RegistroActivity

**Función**: Creación de nueva cuenta.

**Campos**: Nombre, Apellidos, Email, Contraseña (mín. 8 caracteres), Teléfono (opcional), Rol (RadioGroup con dos opciones: Cliente / Transportista).

**Validaciones locales**:
- Campos obligatorios no vacíos
- Contraseña mínimo 8 caracteres

**Flujo**: Al completar el registro exitoso, `finish()` regresa al Login para que el usuario entre con sus credenciales recién creadas (no se hace login automático tras el registro).

---

### 11.4 HomeClienteActivity

**Función**: Dashboard principal del cliente.

**Elementos**:
- Saludo personalizado: "Hola, {primer nombre}" estilo Uber
- `RecyclerView` con los viajes activos (no finalizados) del cliente
- Botón primario naranja "Nuevo viaje" → `CrearViajeActivity`
- Enlace "Ver todos mis viajes" → `MisViajesClienteActivity`
- Toggle de tema y botón de cerrar sesión

**Actualización**: `onResume()` recarga la lista. Al volver de `CrearViajeActivity`, el nuevo viaje aparece de inmediato.

---

### 11.5 HomeTransportistaActivity

**Función**: Dashboard principal del transportista.

**Elementos**:
- Saludo personalizado
- `RecyclerView` con los viajes en curso del transportista (aceptados, no completados)
- Botón principal "Ver viajes disponibles" → `ViajesDisponiblesActivity`
- Botones secundarios: "Mis viajes", "Mis vehículos"

---

### 11.6 CrearViajeActivity

**Función**: Formulario para que el cliente solicite un nuevo transporte.

**Flujo**:
1. Al abrir, carga el catálogo: `GET /api/tipo-mercancia`
2. El catálogo puebla un `Spinner` con los 12 tipos disponibles
3. El usuario rellena: dirección de recogida, dirección de entrega, peso (en kg)
4. Al pulsar "Solicitar viaje": `POST /api/viajes`
5. Si 201 Created: Toast de éxito + `finish()` (vuelve al Home que refresca en `onResume`)
6. Si 403: mensaje de error (solo clientes pueden crear viajes)

---

### 11.7 ViajesDisponiblesActivity

**Función**: Lista de viajes en estado PENDIENTE que el transportista puede aceptar.

**Características**:
- `RecyclerView` con `ViajesAdapter`
- Botón "Actualizar" para refrescar manualmente
- `onResume()` recarga automáticamente (al volver del detalle de un viaje que se acaba de aceptar, ese viaje desaparece de la lista)

---

### 11.8 MisViajesClienteActivity / MisViajesTransportistaActivity

**Función**: Lista completa de viajes del usuario (todos los estados, incluyendo completados y cancelados).

Ambas siguen el mismo patrón: `RecyclerView` + `onResume()` reload. La diferencia es solo el título y el endpoint que se llama (aunque ambos llaman a `GET /api/viajes` — el backend filtra por el JWT automáticamente según el rol).

---

### 11.9 MisVehiculosActivity

**Función**: Lista de vehículos del transportista.

- `RecyclerView` con `VehiculosAdapter` en modo lista (sin checkbox)
- FAB (Floating Action Button) naranja en la esquina inferior derecha para añadir un nuevo vehículo
- Click en un vehículo muestra un `AlertDialog` o `Toast` con los datos detallados

---

### 11.10 CrearVehiculoActivity

**Función**: Formulario de alta de un nuevo vehículo.

**Campos**:
- Matrícula (se convierte automáticamente a mayúsculas con `addTextChangedListener`)
- Marca y Modelo
- Tipo de vehículo (`Spinner` con 7 tipos con nombres legibles)
- Subtipo (opcional, texto libre)
- Capacidad kg (obligatorio), Tara kg (opcional), MMA kg (opcional)
- Carnet requerido (`Spinner`: B, C1, C, C+E)

**Importante**: Los Spinners muestran etiquetas legibles al usuario ("Furgón Grande", "C+E") pero envían los valores exactos del enum del backend ("FURGON_GRANDE", "C_E") en la petición JSON.

---

### 11.11 DetalleViajeActivity — La pantalla más compleja

Muestra la ficha completa de un viaje y adapta los controles disponibles según el rol del usuario autenticado y el estado actual del viaje.

**Datos mostrados**:
- Número de viaje (#ID)
- Badge de estado con color semántico
- Cards separadas para dirección de recogida y entrega
- Tipo de mercancía, descripción libre y peso en kg
- Nombre del cliente, transportista y matrícula del vehículo asignados

**Botones adaptativos**:

| Condición | Botón visible |
|---|---|
| Estado ACEPTADO en adelante (no cancelado) | "Ver en el mapa" |
| Rol TRANSPORTISTA + estado PENDIENTE | "Aceptar viaje" |
| Rol TRANSPORTISTA asignado + estado en curso | "Marcar: {nombre del siguiente estado}" |

**Flujo de aceptación de viaje** (cuando el transportista pulsa "Aceptar viaje"):
1. `GET /api/vehiculos/mis-vehiculos` — carga los vehículos del transportista
2. Muestra un `AlertDialog` con la lista de vehículos en `VehiculosAdapter` modo selector (con checkbox)
3. El transportista selecciona un vehículo y confirma
4. `PUT /api/viajes/{id}/aceptar` con el `vehiculoId` seleccionado
5. La pantalla recarga automáticamente el viaje actualizado con el nuevo estado

**Flujo de avance de estado**: la secuencia de estados está definida localmente en la Activity:

```java
private static final List<String> SECUENCIA_ESTADOS = Arrays.asList(
    "ACEPTADO", "SALIDA_RECOGIDA", "LLEGADA_RECOGIDA", "MERCANCIA_RECOGIDA",
    "SALIDA_ENTREGA", "LLEGADA_ENTREGA", "COMPLETADO"
);

private String getSiguienteEstado(String estadoActual) {
    int idx = SECUENCIA_ESTADOS.indexOf(estadoActual);
    if (idx >= 0 && idx < SECUENCIA_ESTADOS.size() - 1) {
        return SECUENCIA_ESTADOS.get(idx + 1);
    }
    return null; // Ya está en COMPLETADO
}
```

El botón siempre muestra el nombre legible del siguiente estado ("Marcar: En camino a recogida"). Cada clic llama a `PUT /api/viajes/{id}/estado` con el `nuevoEstado`.

---

### 11.12 MapaViajeActivity — La pantalla más visual

Muestra el seguimiento del viaje en un mapa real y permite iniciar la simulación automática para la demo del TFG.

**Layout visual** (dos mitades verticales):
- **55% superior**: `MapView` de OSMDroid con la ruta dibujada
- **45% inferior**: Panel con `RecyclerView` de timeline + botón "Iniciar simulación"

**Elementos del mapa**:
- Marcador "A" rojo en la dirección de recogida (Terrassa, Barcelona — `41.5638, 2.0085`)
- Marcador "B" azul en la dirección de entrega (Mercamadrid, Madrid — `40.3838, -3.6765`)
- Marcador "🚛 PisPax" que se desplaza según el estado del viaje
- Línea naranja (Polyline) dibujando la ruta entre A y B

**Posición del marcador según el estado**:

| Estado | Posición en la ruta |
|---|---|
| ACEPTADO | 0% (en el origen) |
| SALIDA_RECOGIDA | 25% del trayecto |
| LLEGADA_RECOGIDA | 40% (parado en recogida) |
| MERCANCIA_RECOGIDA | 40% (cargando) |
| SALIDA_ENTREGA | 75% del trayecto |
| LLEGADA_ENTREGA | 100% (en el destino) |
| COMPLETADO | 100% + icono check verde |

La posición se calcula por **interpolación lineal** entre las coordenadas de origen y destino. Al cambiar de estado, `ValueAnimator` anima el marcador suavemente durante 1.5 segundos.

**Polling cada 3 segundos**:

```java
private static final int POLLING_INTERVAL_MS = 3000;

private final Runnable pollingRunnable = () -> {
    RetrofitClient.getApi(this).getViaje(viajeId).enqueue(new Callback<ViajeDTO>() {
        @Override
        public void onResponse(Call<ViajeDTO> call, Response<ViajeDTO> response) {
            if (response.isSuccessful() && response.body() != null) {
                ViajeDTO viaje = response.body();
                actualizarMapa(viaje);       // Mueve el marcador
                actualizarTimeline(viaje);   // Actualiza los estados del timeline
                if (!viaje.estaFinalizado()) {
                    // Programar siguiente poll si el viaje sigue activo
                    pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS);
                }
            }
        }
        // ...
    });
};

@Override
protected void onResume() {
    super.onResume();
    pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS); // Inicia el polling
}

@Override
protected void onPause() {
    super.onPause();
    pollingHandler.removeCallbacks(pollingRunnable); // Para el polling al salir de pantalla
}
```

**Botón "Iniciar simulación"** (solo visible para el transportista asignado):
1. Llama a `POST /api/viajes/{id}/simular`
2. El backend responde 200 OK inmediatamente
3. El proceso `@Async` del backend avanza los estados con delays
4. El polling detecta los cambios y actualiza el mapa en tiempo real
5. El botón se oculta tras activar la simulación

---

## 12. Adaptadores — RecyclerView

### ViajesAdapter

**Usado en**: HomeCliente, HomeTransportista, MisViajesCliente, MisViajesTransportista, ViajesDisponibles — un único adaptador genérico para las cinco pantallas de lista.

```java
public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViewHolder> {

    public interface OnViajeClickListener {
        void onViajeClick(ViajeDTO viaje);
    }

    private final List<ViajeDTO> viajes;
    private final OnViajeClickListener listener;

    // El Adapter no sabe qué hacer al hacer click — cada Activity decide
    // mediante la interfaz de callback (patrón Observer)
    public ViajesAdapter(List<ViajeDTO> viajes, OnViajeClickListener listener) {
        this.viajes = viajes;
        this.listener = listener;
    }

    // ...
}
```

**Colores semánticos de estado en cada tarjeta**:
- 🟠 Naranja suave = PENDIENTE
- 🔵 Azul claro = estados en curso (ACEPTADO, SALIDA_RECOGIDA, etc.)
- 🟢 Verde = COMPLETADO
- 🔴 Rojo suave = CANCELADO

Todos los colores están definidos en `res/values/colors.xml` y sus variantes en `res/values-night/colors.xml` para el modo oscuro. Se leen con `ContextCompat.getColor()` para compatibilidad con Android 5.

### VehiculosAdapter — Dos modos de uso

```java
public class VehiculosAdapter extends RecyclerView.Adapter<...> {

    public enum Modo { LISTA, SELECTOR }

    private final Modo modo;
    private VehiculoDTO vehiculoSeleccionado = null;

    // Modo LISTA: en MisVehiculosActivity — muestra los vehículos sin checkbox
    // Modo SELECTOR: en el dialog de aceptar viaje — muestra checkbox, resalta el seleccionado
}
```

**Información mostrada en cada tarjeta**:
- Marca + Modelo (título)
- Matrícula con badge naranja
- Tipo de vehículo con texto legible ("Furgón Grande", no "FURGON_GRANDE")
- Capacidad en kg y carnet requerido ("C+E" no "C_E")
- En modo SELECTOR: checkbox en el margen derecho

### TipoMercanciaAdapter

Extiende `ArrayAdapter<TipoMercanciaDTO>` para poblar el `Spinner` en `CrearViajeActivity`. El método `toString()` de `TipoMercanciaDTO` devuelve el nombre del tipo, haciendo que el Spinner muestre los nombres automáticamente sin código adicional.

### TimelineAdapter

Muestra la secuencia de 7 estados del viaje (de ACEPTADO a COMPLETADO) en un `RecyclerView` vertical. Cada ítem tiene:
- Punto circular: gris (pendiente) / naranja parpadeante (actual) / verde (completado)
- Nombre legible del estado
- Línea de conexión vertical (se oculta en el último elemento)

```java
// Cada ítem del timeline sabe su estado visual
private int getEstadoVisual(int posicion, int estadoActualIndex) {
    if (posicion < estadoActualIndex) return COMPLETADO;   // Verde
    if (posicion == estadoActualIndex) return EN_CURSO;   // Naranja parpadeante
    return PENDIENTE;                                      // Gris
}
```

---

## 13. Sistema de temas claro y oscuro

PisPax implementa un sistema de temas dual usando el componente `DayNight` de Material Components. Android selecciona automáticamente los recursos de `values/` (claro) o `values-night/` (oscuro) según el modo activo.

### Paleta de colores corporativa

| Nombre | Modo claro | Modo oscuro | Uso |
|---|---|---|---|
| `naranja_principal` | `#FF8E3B` | `#FF8E3B` | Botones primarios, badges, FAB, línea de ruta del mapa |
| `navy_secundario` | `#2D2850` | `#3D3B5A` | Cabeceras (Splash), status bar |
| `fondo_pantalla` | `#F7F7F7` | `#121212` | Fondo general de las pantallas |
| `fondo_card` | `#FFFFFF` | `#1E1E1E` | Cards, containers, items de lista |
| `texto_primario` | `#1A1A1A` | `#EFEFEF` | Títulos y datos principales |
| `texto_secundario` | `#757575` | `#9E9E9E` | Subtítulos, etiquetas, hints |

### Tema base (`res/values/themes.xml`)

```xml
<style name="Theme.PisPax" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="colorPrimary">@color/naranja_principal</item>
    <item name="colorSecondary">@color/navy_secundario</item>
    <item name="android:statusBarColor">@color/navy_secundario</item>
    <item name="colorOnPrimary">@color/blanco</item>
</style>
```

Se usa `NoActionBar` porque PisPax implementa su propia barra de navegación con la identidad visual corporativa. La `ActionBar` del sistema se elimina para tener control total del diseño.

### Cómo funciona el toggle de tema

```java
// En cualquier Activity, al pulsar el botón luna/sol:
btnToggleTema.setOnClickListener(v -> {
    boolean nuevoModo = !SessionManager.isDarkMode(this);
    SessionManager.setDarkMode(this, nuevoModo);
    AppCompatDelegate.setDefaultNightMode(
        nuevoModo ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
    );
    recreate(); // Reinicia la Activity aplicando el nuevo tema
});
```

`recreate()` reinicia la Activity completamente, aplicando el nuevo tema en todos los recursos. `PispaxApplication.onCreate()` garantiza que el tema correcto se aplica también al relanzar la app.

---

## 14. Simulación visual del viaje — OSMDroid

La simulación es la característica más impactante del TFG. Permite demostrar el ciclo completo de un viaje en ~38 segundos, mostrando un camión moviéndose en un mapa real.

### Arquitectura completa de la simulación

```
╔══════════════════════════════════════════════════════════════════╗
║  ANDROID: MapaViajeActivity                                      ║
║                                                                  ║
║  [Botón "Iniciar simulación"]                                    ║
║       │                                                          ║
║       │ POST /api/viajes/{id}/simular                            ║
║       │                                                          ║
╚═══════╪══════════════════════════════════════════════════════════╝
        │
        ▼
╔══════════════════════════════════════════════════════════════════╗
║  BACKEND: SimulacionService @Async                               ║
║                                                                  ║
║  200 OK (respuesta inmediata)                                    ║
║                                                                  ║
║  Thread separado:                                                ║
║  PENDIENTE → sleep(4s) → ACEPTADO                               ║
║  ACEPTADO → sleep(5s) → SALIDA_RECOGIDA                         ║
║  SALIDA_RECOGIDA → sleep(4s) → LLEGADA_RECOGIDA                 ║
║  LLEGADA_RECOGIDA → sleep(5s) → MERCANCIA_RECOGIDA              ║
║  MERCANCIA_RECOGIDA → sleep(6s) → SALIDA_ENTREGA                ║
║  SALIDA_ENTREGA → sleep(4s) → LLEGADA_ENTREGA                   ║
║  LLEGADA_ENTREGA → sleep(3s) → COMPLETADO                       ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
        ↑
        │ GET /api/viajes/{id} cada 3 segundos (polling)
        │
╔══════════════════════════════════════════════════════════════════╗
║  ANDROID: Polling + Animación                                    ║
║                                                                  ║
║  pollingRunnable detecta cambio de estado                        ║
║       → actualizarMapa(viaje)                                    ║
║             → calcular posición por interpolación lineal         ║
║             → ValueAnimator 1500ms mueve el marcador suavemente  ║
║       → actualizarTimeline(viaje)                                ║
║             → actualizar colores de los puntos del timeline      ║
║       → si estaFinalizado(): parar polling                       ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

### Coordenadas de la ruta demo

```java
// Hardcodeadas en MapaViajeActivity para la demo del TFG
private static final GeoPoint ORIGEN  = new GeoPoint(41.5638, 2.0085);
// Polígono Industrial Can Fontanet, Terrassa (Barcelona)

private static final GeoPoint DESTINO = new GeoPoint(40.3838, -3.6765);
// Centro Logístico Mercamadrid, Madrid

// Ruta real de ~600km — hace la demo visualmente impactante en el mapa
```

### Cálculo de posición por interpolación lineal

```java
private GeoPoint calcularPosicion(double porcentaje) {
    double lat = ORIGEN.getLatitude()  + porcentaje * (DESTINO.getLatitude()  - ORIGEN.getLatitude());
    double lon = ORIGEN.getLongitude() + porcentaje * (DESTINO.getLongitude() - ORIGEN.getLongitude());
    return new GeoPoint(lat, lon);
}

// Mapa de estado → porcentaje de la ruta
private double getPorcentajeRuta(String estado) {
    switch (estado) {
        case "ACEPTADO":           return 0.0;
        case "SALIDA_RECOGIDA":    return 0.25;
        case "LLEGADA_RECOGIDA":   return 0.40;
        case "MERCANCIA_RECOGIDA": return 0.40;
        case "SALIDA_ENTREGA":     return 0.75;
        case "LLEGADA_ENTREGA":    return 1.0;
        case "COMPLETADO":         return 1.0;
        default:                   return 0.0;
    }
}
```

### Animación con ValueAnimator

```java
private void animarMarcador(GeoPoint desde, GeoPoint hasta) {
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
    animator.setDuration(1500); // 1.5 segundos de animación suave
    animator.setInterpolator(new AccelerateDecelerateInterpolator());

    animator.addUpdateListener(animation -> {
        float t = (float) animation.getAnimatedValue();
        double lat = desde.getLatitude()  + t * (hasta.getLatitude()  - desde.getLatitude());
        double lon = desde.getLongitude() + t * (hasta.getLongitude() - desde.getLongitude());
        marcadorCamion.setPosition(new GeoPoint(lat, lon));
        mapaView.invalidate(); // Redibuja el mapa
    });

    animator.start();
}
```

---

## 15. Decisiones de diseño importantes

### 1. Stateless — sin base de datos local

La app no usa SQLite ni Room. Todo se consulta al backend cuando se necesita. Esto simplifica el código (sin sincronización, sin cachés de datos), es consistente con la filosofía MVP y garantiza que el cliente siempre tiene datos actuales.

### 2. Singleton de Retrofit

Un único cliente HTTP para toda la app, con el interceptor JWT configurado una sola vez. No existe riesgo de olvidarse de añadir el token en alguna petición: el interceptor lo gestiona de forma transparente.

### 3. ContextCompat para colores

```java
// ✅ Correcto: compatible con Android 5.0 y 5.1 (API 21-22)
int color = ContextCompat.getColor(context, R.color.naranja_principal);

// ❌ Incorrecto: falla en API 21-22 (el método no existe en Context)
int color = context.getColor(R.color.naranja_principal);
```

El `minSdk 21` obliga a usar `ContextCompat` para compatibilidad total.

### 4. `onResume()` como punto de refresco universal

En lugar de añadir `startActivityForResult()` y manejar sus callbacks en cada transición, todas las listas se recargan en `onResume()`. Patrón simple, consistente y que funciona en todos los flujos de navegación (incluyendo el botón "atrás" nativo).

### 5. DTOs con métodos helper

`ViajeDTO.estaEnCurso()` y `ViajeDTO.estaFinalizado()` centralizan la lógica de estado. Sin ellos, la misma comprobación `!PENDIENTE && !COMPLETADO && !CANCELADO` estaría duplicada en al menos 5 lugares distintos de la UI.

### 6. ViajesAdapter genérico con callback

Un único adaptador sirve para cinco pantallas distintas. La interfaz `OnViajeClickListener` desacopla el adaptador de la lógica de navegación. Cada Activity decide qué hacer al hacer tap, pasando una lambda al constructor del adaptador.

### 7. Spinners con valores internos vs legibles

Los Spinners muestran etiquetas legibles ("Furgón Grande", "C+E") pero internamente almacenan el valor del enum del backend ("FURGON_GRANDE", "C_E"). Al construir la petición JSON, se usa el valor interno, no la etiqueta visible.

---

## 16. Conexión con el backend

### URLs de conexión según entorno

```
Emulador Android Studio:
BASE_URL = "http://10.0.2.2:8080/api/"
→ 10.0.2.2 es la dirección especial que el emulador usa para conectar
  con localhost del PC donde corre el emulador

Dispositivo físico en la misma red WiFi:
BASE_URL = "http://192.168.1.100:8080/api/"
→ La IP local del PC (consultarla con: ipconfig → Dirección IPv4 del adaptador WiFi)
```

### Ciclo completo de una petición autenticada

```
1. Activity llama a:
   RetrofitClient.getApi(this).getMisViajes()
          │
          ▼
2. RetrofitClient ya tiene configurado el interceptor JWT.
   El interceptor lee: SessionManager.getToken(ctx) → "eyJhbGciOi..."
          │
          ▼
3. OkHttp construye la petición HTTP:
   GET http://10.0.2.2:8080/api/viajes
   Authorization: Bearer eyJhbGciOi...
   Content-Type: application/json
          │
          ▼ (viaja por la red WiFi o loopback virtual)
          │
4. Backend recibe la petición:
   → JwtAuthFilter lee el header Authorization
   → Verifica la firma HMAC-SHA256 del token
   → Extrae userId y rol del payload
   → Inyecta la autenticación en el SecurityContext de Spring
   → La petición pasa al Controller
          │
          ▼
5. ViajeController.getMisViajes() se ejecuta:
   → Lee userId de auth.getCredentials()
   → Llama a ViajeService.getMisViajes(userId)
   → ViajeService consulta ViajeRepository
   → JPA/Hibernate ejecuta SELECT en MySQL
   → Devuelve List<ViajeDTO> como JSON
          │
          ▼
6. OkHttp recibe la respuesta JSON
7. Gson convierte automáticamente: JSON → List<ViajeDTO>
8. Callback onResponse() se ejecuta en el hilo principal
9. Activity actualiza el RecyclerView con los datos recibidos
```

---

## 17. Recursos de valores — colores, estilos, dimensiones

### `res/values/colors.xml`

```xml
<resources>
    <!-- Colores corporativos PisPax -->
    <color name="naranja_principal">#FF8E3B</color>
    <color name="navy_secundario">#2D2850</color>

    <!-- Fondos modo claro -->
    <color name="fondo_pantalla">#F7F7F7</color>
    <color name="fondo_card">#FFFFFF</color>

    <!-- Textos modo claro -->
    <color name="texto_primario">#1A1A1A</color>
    <color name="texto_secundario">#757575</color>

    <!-- Estados de viaje (badges) — modo claro -->
    <color name="estado_pendiente">#FFF3E0</color>
    <color name="estado_en_curso">#E3F2FD</color>
    <color name="estado_completado">#E8F5E9</color>
    <color name="estado_cancelado">#FFEBEE</color>

    <!-- Otros -->
    <color name="blanco">#FFFFFF</color>
    <color name="negro">#000000</color>
    <color name="divider">#E0E0E0</color>
</resources>
```

### `res/values-night/colors.xml` (overrides para modo oscuro)

```xml
<resources>
    <!-- Los colores corporativos se mantienen igual -->
    <!-- Solo cambian los fondos y textos -->
    <color name="fondo_pantalla">#121212</color>
    <color name="fondo_card">#1E1E1E</color>
    <color name="texto_primario">#EFEFEF</color>
    <color name="texto_secundario">#9E9E9E</color>
    <color name="divider">#333333</color>
</resources>
```

### `res/values/dimens.xml`

```xml
<resources>
    <!-- Espaciados estándar -->
    <dimen name="padding_small">8dp</dimen>
    <dimen name="padding_normal">16dp</dimen>
    <dimen name="padding_large">24dp</dimen>

    <!-- Bordes redondeados -->
    <dimen name="corner_radius">12dp</dimen>

    <!-- Tipografía -->
    <dimen name="text_title">22sp</dimen>
    <dimen name="text_subtitle">16sp</dimen>
    <dimen name="text_body">14sp</dimen>
    <dimen name="text_caption">12sp</dimen>
</resources>
```

### Layouts XML principales

| Archivo | Activity | Descripción |
|---|---|---|
| `activity_splash.xml` | SplashActivity | Logo PisPax centrado sobre fondo naranja |
| `activity_login.xml` | LoginActivity | Email, password, botón login, toggle tema |
| `activity_registro.xml` | RegistroActivity | Formulario completo + RadioGroup de rol |
| `activity_home_cliente.xml` | HomeClienteActivity | Saludo + RecyclerView + botón nuevo viaje |
| `activity_home_transportista.xml` | HomeTransportistaActivity | Saludo + RecyclerView + botones |
| `activity_crear_viaje.xml` | CrearViajeActivity | Spinner tipo + inputs + botón solicitar |
| `activity_mis_viajes_cliente.xml` | MisViajesClienteActivity | RecyclerView con todos los viajes |
| `activity_viajes_disponibles.xml` | ViajesDisponiblesActivity | RecyclerView + botón actualizar |
| `activity_mis_vehiculos.xml` | MisVehiculosActivity | RecyclerView + FAB |
| `activity_crear_vehiculo.xml` | CrearVehiculoActivity | Formulario completo de vehículo |
| `activity_detalle_viaje.xml` | DetalleViajeActivity | Ficha completa + botones dinámicos |
| `activity_mapa_viaje.xml` | MapaViajeActivity | MapView + Panel inferior deslizante |
| `item_viaje.xml` | ViajesAdapter | Tarjeta de viaje con badge de estado |
| `item_vehiculo.xml` | VehiculosAdapter | Tarjeta de vehículo con datos |
| `item_timeline.xml` | TimelineAdapter | Punto + línea + nombre del estado |

---

## 18. Guía para la defensa del TFG

### Estructura de la demo en vivo (5-6 minutos)

**Parte 1 — Registro y login (1 min)**
1. Registrar un usuario CLIENTE ("María García")
2. Registrar un usuario TRANSPORTISTA ("Carlos Martínez")
3. Hacer login como CLIENTE → mostrar el HomeCliente
4. Toggle de modo oscuro para demostrar el sistema de temas

**Parte 2 — Ciclo completo de un viaje (2-3 min)**
1. Como CLIENTE: crear un viaje (seleccionar tipo de mercancía del catálogo, origen, destino, peso)
2. Mostrar que el viaje aparece en "Mis viajes" con estado PENDIENTE
3. Cerrar sesión → login como TRANSPORTISTA
4. "Ver viajes disponibles" → aparece el viaje creado
5. Entrar al detalle → "Aceptar viaje" → seleccionar el vehículo del dialog → confirmar
6. Mostrar que el estado cambió a ACEPTADO

**Parte 3 — Simulación en el mapa (2 min)**
1. Desde el detalle del viaje → "Ver en el mapa"
2. Mostrar el mapa con la ruta Terrassa → Madrid (marcadores A y B)
3. Pulsar "Iniciar simulación"
4. Ver cómo el camión se desplaza por el mapa y el timeline avanza automáticamente
5. Esperar a COMPLETADO (~38 segundos) — animación de celebración

### Respuestas a preguntas frecuentes del tribunal

**P: "¿Por qué no usaste Kotlin?"**
R: El ciclo DAM trabaja principalmente con Java. Mantener el mismo lenguaje en backend y frontend redujo la curva de aprendizaje y simplificó la gestión del proyecto. Kotlin es una evolución natural para una versión 2.0.

**P: "¿Cómo garantizas que solo el CLIENTE crea viajes?"**
R: La seguridad está implementada en dos niveles. En el backend, `@PreAuthorize("hasRole('CLIENTE')")` en `ViajeController.crearViaje()` garantiza que solo usuarios con token de CLIENTE puedan crear viajes — aunque alguien falsifique la UI, el servidor lo rechazará con 403. La UI del frontend también filtra según el rol, pero solo como capa de experiencia de usuario.

**P: "¿Por qué no se persisten datos con SQLite?"**
R: En una app de transporte en tiempo real, los datos cambian constantemente. SQLite local introduciría problemas de sincronización. El polling de la pantalla de mapa es la solución más sencilla y correcta para este caso de uso.

**P: "¿El mapa usa Google Maps?"**
R: No. Se usa OSMDroid con tiles de OpenStreetMap, completamente gratuito y de código abierto. No requiere API key ni billing. Para producción se podría migrar a Google Maps o Mapbox.

**P: "¿Por qué minSdk 21?"**
R: Android 5.0 (API 21) cubre el 98% de dispositivos Android activos según Google Dashboard. Todas las librerías usadas son compatibles con esta API.

---

*Documentación unificada generada el 08/06/2026 — TFG PisPax — Adrián Salazar Nicolás*
