# Guía Completa de Postman y JWT — PisPax Backend
### TFG DAM 2025/2026 — Adrián Salazar

---

## Índice

**PARTE 1 — JWT: Qué es, cómo funciona y cómo está implementado**

1. [Qué es JWT y por qué se usa](#1-que-es-jwt)
2. [Anatomía de un token JWT](#2-anatomia-del-token)
3. [Cómo funciona el cifrado HMAC-SHA256](#3-cifrado-hmac-sha256)
4. [Flujo completo de autenticación en PisPax](#4-flujo-de-autenticacion)
5. [Implementación en el código de PisPax](#5-implementacion-en-codigo)
6. [Ciclo de vida de un token](#6-ciclo-de-vida)

**PARTE 2 — Endpoints y pruebas con Postman**

7. [Configuración inicial de Postman](#7-configuracion-postman)
8. [Variables de entorno en Postman](#8-variables-entorno)
9. [Endpoint 1 — Registro de cliente](#9-registro-cliente)
10. [Endpoint 2 — Registro de transportista](#10-registro-transportista)
11. [Endpoint 3 — Login](#11-login)
12. [Endpoint 4 — Tipos de mercancía (público)](#12-tipos-mercancia)
13. [Endpoint 5 — Registrar vehículo](#13-registrar-vehiculo)
14. [Endpoint 6 — Mis vehículos](#14-mis-vehiculos)
15. [Endpoint 7 — Crear viaje](#15-crear-viaje)
16. [Endpoint 8 — Ver mis viajes](#16-mis-viajes)
17. [Endpoint 9 — Ver detalle de viaje](#17-detalle-viaje)
18. [Endpoint 10 — Viajes disponibles (transportista)](#18-viajes-disponibles)
19. [Endpoint 11 — Aceptar viaje](#19-aceptar-viaje)
20. [Endpoint 12 — Actualizar estado del viaje](#20-actualizar-estado)
21. [Flujo completo de prueba de extremo a extremo](#21-flujo-completo)
22. [Pruebas de error — qué debe fallar y por qué](#22-pruebas-de-error)

---

# PARTE 1 — JWT

---

## 1. Qué es JWT y por qué se usa

### El problema que JWT resuelve

Las aplicaciones web tradicionales usan **sesiones**: cuando el usuario hace login, el servidor crea una sesión en memoria o en base de datos y le devuelve una cookie con un identificador. En cada petición siguiente, el servidor busca esa sesión para saber quién es el usuario.

```
SISTEMA CON SESIONES (tradicional):

  Cliente          Servidor          Base de Datos
    │                │                    │
    │ login ────────►│                    │
    │                │ INSERT session ───►│
    │ ◄── cookie ────│                    │
    │                │                    │
    │ GET /perfil ──►│                    │
    │  (con cookie)  │ SELECT session ───►│
    │                │◄─── datos ─────────│
    │ ◄── respuesta ─│                    │
```

Este sistema tiene un problema: **el servidor tiene que guardar estado**. En una app móvil con miles de usuarios simultáneos, eso escala mal.

### La solución: JWT (stateless)

JWT (JSON Web Token) elimina el almacenamiento en servidor. Toda la información necesaria va dentro del propio token, firmado criptográficamente. El servidor no guarda nada.

```
SISTEMA CON JWT (stateless):

  Cliente          Servidor
    │                │
    │ login ────────►│
    │                │ genera token JWT
    │                │ (sin guardar nada)
    │ ◄── JWT ───────│
    │                │
    │ GET /perfil ──►│
    │  (con JWT)     │ verifica firma JWT
    │                │ (sin consultar BD)
    │ ◄── respuesta ─│
```

### Ventajas para PisPax

- **Stateless**: el backend no guarda sesiones. Cada petición es autocontenida.
- **Escalable**: se pueden tener múltiples instancias del backend sin compartir sesiones.
- **Móvil-friendly**: perfecto para Android. El token se guarda en SharedPreferences y se envía en cada petición.
- **Información embebida**: el token ya lleva el `userId` y el `rol`, así los controllers saben quién llama sin consultar la BD.

---

## 2. Anatomía de un token JWT

Un token JWT es una cadena de texto con tres partes separadas por puntos (`.`):

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSIsInVzZXJJZCI6MSwicm9sIjoiQ0xJRU5URSIsImlhdCI6MTc0ODQ1Njc4OSwiZXhwIjoxNzQ4NTQzMTg5fQ.XyZ_firma_aqui

│────────────────────│.│──────────────────────────────────────────────────────────│.│─────────────│
       HEADER                                  PAYLOAD                                 SIGNATURE
```

### HEADER (Cabecera)

Contiene el tipo de token y el algoritmo de firma. En Base64:

```
eyJhbGciOiJIUzI1NiJ9
```

Decodificado:
```json
{
  "alg": "HS256"
}
```

- `alg`: algoritmo usado para firmar → `HS256` = HMAC con SHA-256

### PAYLOAD (Carga útil)

Contiene los datos (llamados "claims"). En Base64:

```
eyJzdWIiOiJ1c3VhcmlvQGVtYWlsLmNvbSIsInVzZXJJZCI6MSwicm9sIjoiQ0xJRU5URSIsImlhdCI6MTc0ODQ1Njc4OSwiZXhwIjoxNzQ4NTQzMTg5fQ
```

Decodificado:
```json
{
  "sub": "usuario@email.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

| Campo | Nombre completo | Qué contiene en PisPax |
|---|---|---|
| `sub` | subject | Email del usuario |
| `userId` | (custom claim) | ID del usuario en MySQL |
| `rol` | (custom claim) | `CLIENTE` o `TRANSPORTISTA` |
| `iat` | issued at | Timestamp Unix de cuando se generó |
| `exp` | expiration | Timestamp Unix de cuando caduca |

**IMPORTANTE**: el payload NO está cifrado, solo está en Base64. Cualquiera puede decodificarlo y leer su contenido. Por eso NUNCA debe ir información sensible como la contraseña en el payload. Lo que garantiza JWT es la **integridad** (que nadie ha modificado el token), no la **confidencialidad** del contenido.

Puedes decodificar cualquier token en https://jwt.io para ver su contenido.

### SIGNATURE (Firma)

Es la parte que garantiza que el token es auténtico y no ha sido manipulado:

```
HMACSHA256(
  base64url(header) + "." + base64url(payload),
  clave_secreta
)
```

La firma se genera combinando el header y el payload con la **clave secreta** del servidor. Si alguien modifica cualquier parte del token, la firma no coincide y el servidor lo rechaza.

---

## 3. Cifrado HMAC-SHA256

### SHA-256: función hash

SHA-256 es una **función hash criptográfica**. Toma cualquier entrada y produce una salida de exactamente 256 bits (64 caracteres hexadecimales). Sus propiedades clave:

- **Determinista**: la misma entrada siempre produce la misma salida.
- **Irreversible**: no se puede obtener la entrada a partir de la salida.
- **Efecto avalancha**: un cambio mínimo en la entrada cambia completamente la salida.
- **Sin colisiones prácticas**: dos entradas distintas no producen la misma salida.

Ejemplo:
```
SHA256("hola")        = "b94d27b9934d3e08a52e52d7da7dabfac484efe04294e576b2938af69e4c3f58"
SHA256("holA")        = "05e92e8a0e1f73b56c3b79e57c18fa2ee8e16d8ab23c76cd63dfb1cb5ebc5b1c"
                         ↑ Completamente diferente con solo cambiar una letra
```

### HMAC: autenticación con hash

HMAC (Hash-based Message Authentication Code) añade una **clave secreta** al proceso de hashing:

```
HMAC-SHA256(mensaje, clave_secreta) = firma
```

Con HMAC:
- Solo quien conoce la `clave_secreta` puede generar una firma válida.
- Cualquiera puede verificar la firma si tiene la clave.
- Si el mensaje cambia, la firma no coincide → el servidor detecta la manipulación.

### Cómo lo usa PisPax

La clave secreta está en `application.properties`:
```
jwt.secret=pispax_jwt_secret_key_2026_muy_larga_para_seguridad_hmac_sha256
```

Esta clave la conoce **solo el servidor**. El proceso completo:

```
GENERACIÓN (en login):
  datos = base64(header) + "." + base64(payload)
  firma = HMAC-SHA256(datos, "pispax_jwt_secret_key_2026...")
  token = datos + "." + base64(firma)

VERIFICACIÓN (en cada petición):
  1. Extraer header, payload y firma del token recibido
  2. Recalcular: firma_esperada = HMAC-SHA256(header+"."+payload, clave_secreta)
  3. Comparar firma_recibida con firma_esperada
  4. Si no coinciden → token inválido (rechazar)
  5. Si coinciden → comprobar que no ha caducado (campo exp)
  6. Si todo OK → extraer userId y rol del payload
```

### ¿Qué pasaría si alguien intenta falsificar un token?

Supón que un atacante intercepta el token y cambia `"rol": "CLIENTE"` por `"rol": "TRANSPORTISTA"`:

```
Token original:
  eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJDTElFTlRFIn0.FIRMA_VALIDA

Token manipulado:
  eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJUUkFOU1BPUlRJU1RBIn0.FIRMA_VALIDA
                                                              ↑ firma ya no corresponde al nuevo payload
```

El servidor recalcula la firma con el payload modificado y **no coincide** con la firma original. El token se rechaza con 401/403. Sin la clave secreta del servidor, es computacionalmente imposible generar una firma válida.

---

## 4. Flujo completo de autenticación en PisPax

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        FLUJO DE REGISTRO Y LOGIN                            │
└─────────────────────────────────────────────────────────────────────────────┘

  Android / Postman                          PisPax Backend
        │                                           │
        │  1. POST /api/auth/registro               │
        │     Body: {nombre, email, password, rol}  │
        │ ─────────────────────────────────────────►│
        │                                           │ 2. Valida campos (@Valid)
        │                                           │ 3. Comprueba email único
        │                                           │ 4. BCrypt.encode(password)
        │                                           │    → genera hash de 60 chars
        │                                           │ 5. INSERT en tabla usuario
        │  6. 201 Created                           │
        │     Body: UsuarioDTO (sin password)       │
        │ ◄─────────────────────────────────────────│
        │                                           │
        │  7. POST /api/auth/login                  │
        │     Body: {email, password}               │
        │ ─────────────────────────────────────────►│
        │                                           │ 8. SELECT usuario WHERE email
        │                                           │ 9. BCrypt.matches(pass, hash)
        │                                           │10. Genera JWT:
        │                                           │    - sub = email
        │                                           │    - userId = usuario.id
        │                                           │    - rol = usuario.rol
        │                                           │    - iat = ahora
        │                                           │    - exp = ahora + 86400000ms
        │                                           │    - firma con HMAC-SHA256
        │  11. 200 OK                               │
        │      Body: { token: "eyJ...", usuario:{}} │
        │ ◄─────────────────────────────────────────│
        │                                           │
        │  [Guarda el token]                        │
        │                                           │

┌─────────────────────────────────────────────────────────────────────────────┐
│                      FLUJO DE PETICIÓN AUTENTICADA                          │
└─────────────────────────────────────────────────────────────────────────────┘

  Android / Postman                   JwtAuthFilter           Controller
        │                                   │                      │
        │  GET /api/viajes                  │                      │
        │  Header: Authorization: Bearer eyJ│                      │
        │ ─────────────────────────────────►│                      │
        │                                   │ 1. Lee header Auth   │
        │                                   │ 2. Extrae token      │
        │                                   │ 3. Verifica firma    │
        │                                   │ 4. Comprueba exp     │
        │                                   │ 5. Extrae email,     │
        │                                   │    userId, rol       │
        │                                   │ 6. Inyecta en        │
        │                                   │    SecurityContext   │
        │                                   │ ────────────────────►│
        │                                   │                      │ 7. Lee userId de
        │                                   │                      │    auth.getCredentials()
        │                                   │                      │ 8. Ejecuta lógica
        │  200 OK + datos                   │                      │
        │ ◄─────────────────────────────────────────────────────── │
```

---

## 5. Implementación en el código de PisPax

### JwtUtil.java — Genera y valida tokens

```java
@Component
public class JwtUtil {

    @Value("${jwt.secret}")       // Lee la clave de application.properties
    private String secret;

    @Value("${jwt.expiration}")   // Lee 86400000 (24 horas en ms)
    private long expiration;

    // Construye la clave criptográfica a partir del string
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Genera el token JWT con tres claims personalizados
    public String generateToken(String email, Long userId, String rol) {
        return Jwts.builder()
                .subject(email)              // claim estándar: sub
                .claim("userId", userId)     // claim personalizado
                .claim("rol", rol)           // claim personalizado
                .issuedAt(new Date())        // claim estándar: iat
                .expiration(new Date(System.currentTimeMillis() + expiration))  // iat + 24h
                .signWith(getKey())          // firma con HMAC-SHA256
                .compact();                  // serializa a String
    }

    // Verifica la firma y devuelve todos los claims
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())        // verifica que la firma es válida
                .build()
                .parseSignedClaims(token)    // parsea y verifica expiración
                .getPayload();
    }

    // Extrae el email (claim "sub")
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Extrae el userId (claim personalizado)
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    // Extrae el rol (claim personalizado)
    public String extractRol(String token) {
        return extractClaims(token).get("rol", String.class);
    }

    // Devuelve true si el token es válido y no ha caducado
    public boolean isTokenValid(String token) {
        try {
            return !extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;   // token malformado, firma inválida o caducado
        }
    }
}
```

### JwtAuthFilter.java — Intercepta cada petición HTTP

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Lee la cabecera Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Si no hay cabecera o no empieza por "Bearer ", deja pasar sin autenticar
        //    (Spring Security decidirá luego si el endpoint necesita auth o no)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrae el token (todo lo que hay después de "Bearer ")
        String token = authHeader.substring(7);

        // 4. Si el token no es válido (firma incorrecta o caducado), deja pasar sin autenticar
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extrae los datos del token
        String email  = jwtUtil.extractEmail(token);
        String rol    = jwtUtil.extractRol(token);
        Long userId   = jwtUtil.extractUserId(token);

        // 6. Crea el objeto de autenticación de Spring Security
        //    - principal    = email   (String)
        //    - credentials  = userId  (Long) ← así lo leen los controllers
        //    - authorities  = ["ROLE_CLIENTE"] o ["ROLE_TRANSPORTISTA"]
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        var auth = new UsernamePasswordAuthenticationToken(email, userId, authorities);

        // 7. Inyecta la autenticación en el contexto de la petición actual
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
```

### Cómo los controllers leen el userId

En los controllers, Spring inyecta el objeto `Authentication` que rellenó el filtro:

```java
@GetMapping
public ResponseEntity<List<ViajeDTO>> getMisViajes(Authentication auth) {
    Long userId = (Long) auth.getCredentials();  // ← aquí está el userId del token
    return ResponseEntity.ok(viajeService.getMisViajes(userId));
}
```

---

## 6. Ciclo de vida de un token

```
  T=0          T=1s          T=86400s (24h)      T=86401s
   │             │                │                  │
   │  login      │  peticiones    │  token caduca    │  token inválido
   │  → token    │  con token     │                  │  → 401/403
   │             │                │                  │
```

### Qué pasa cuando caduca

El token lleva `exp = iat + 86400000ms`. Cuando `exp < ahora`:

```java
// En JwtUtil.isTokenValid():
extractClaims(token).getExpiration().before(new Date())  →  true  →  devuelve false
```

El filtro no inyecta la autenticación → Spring Security ve la petición como anónima → devuelve 403.

### No hay refresh token

En la implementación actual de PisPax no hay refresh token. Cuando el token caduca, el usuario tiene que hacer login de nuevo. Para el TFG esto es suficiente.

### BCrypt y las contraseñas

Las contraseñas no se guardan en texto plano. Se usa BCrypt:

```
password: "miPassword123"
hash:     "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKr.VJsgG"
```

BCrypt incluye automáticamente una "sal" aleatoria en el hash, por lo que el mismo password siempre genera un hash diferente. Para verificar, `BCrypt.matches(rawPassword, storedHash)` compara sin necesitar la sal por separado.

---

# PARTE 2 — Endpoints y Pruebas con Postman

---

## 7. Configuración inicial de Postman

### Crear una colección

1. Abrir Postman
2. Click en "Collections" (panel izquierdo)
3. "+" → "Blank Collection"
4. Nombrarla: **PisPax API**

### Configurar el Content-Type global

Para no repetirlo en cada petición:
1. Click en la colección → pestaña "Pre-request Script" (o Headers a nivel colección)
2. En cada petición que envíe body, añadir header: `Content-Type: application/json`

---

## 8. Variables de entorno en Postman

Las variables te permiten cambiar la URL base y reutilizar el token automáticamente.

### Crear un entorno

1. Icono de entornos (rueda dentada arriba a la derecha) → "Add"
2. Nombre: **PisPax Local**
3. Añadir estas variables:

| Variable | Valor inicial | Descripción |
|---|---|---|
| `base_url` | `http://localhost:8080` | URL base del backend |
| `token_cliente` | *(vacío)* | Se rellena automáticamente al hacer login |
| `token_transportista` | *(vacío)* | Se rellena automáticamente al hacer login |
| `viaje_id` | *(vacío)* | ID del viaje creado, para usarlo en siguientes peticiones |
| `vehiculo_id` | *(vacío)* | ID del vehículo creado |

4. Click "Save"
5. Seleccionar el entorno en el desplegable arriba a la derecha

### Script para guardar el token automáticamente

En la pestaña **"Tests"** del endpoint de login, pega este script:

```javascript
// Para el login del CLIENTE
if (pm.response.code === 200) {
    var body = pm.response.json();
    pm.environment.set("token_cliente", body.token);
    console.log("Token cliente guardado: " + body.token.substring(0, 20) + "...");
}
```

```javascript
// Para el login del TRANSPORTISTA
if (pm.response.code === 200) {
    var body = pm.response.json();
    pm.environment.set("token_transportista", body.token);
    console.log("Token transportista guardado: " + body.token.substring(0, 20) + "...");
}
```

Así, después de hacer login, el token se guarda solo y todas las peticiones siguientes lo usan con `{{token_cliente}}` o `{{token_transportista}}`.

---

## 9. Endpoint 1 — Registro de cliente

### Configuración en Postman

| Campo | Valor |
|---|---|
| Método | **POST** |
| URL | `{{base_url}}/api/auth/registro` |
| Autenticación | Ninguna |
| Content-Type | `application/json` |

### Body (JSON):

```json
{
    "nombre": "María",
    "apellidos": "García López",
    "email": "maria.cliente@pispax.com",
    "password": "Password123",
    "telefono": "600111222",
    "rol": "CLIENTE"
}
```

### Respuesta esperada — 201 Created:

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

Observa que `password` **no aparece** en la respuesta. El DTO filtra los campos sensibles.

### Validaciones que el servidor comprueba

| Campo | Regla | Error si falla |
|---|---|---|
| `nombre` | Obligatorio, máx 100 chars | `"nombre: El nombre es obligatorio"` |
| `apellidos` | Obligatorio, máx 150 chars | `"apellidos: Los apellidos son obligatorios"` |
| `email` | Obligatorio, formato válido | `"email: Email con formato inválido"` |
| `password` | Obligatorio, mínimo 8 caracteres | `"password: La contraseña debe tener al menos 8 caracteres"` |
| `telefono` | Opcional, solo dígitos/+/espacios | `"telefono: Teléfono inválido"` |
| `rol` | Obligatorio, debe ser `CLIENTE` o `TRANSPORTISTA` | `"rol: El rol es obligatorio"` |

### Posibles errores

**400 — Email duplicado:**
```json
{
    "mensaje": "Ya existe un usuario con ese email",
    "codigo": 400
}
```

**400 — Campos inválidos:**
```json
{
    "mensaje": "email: Email con formato inválido, password: La contraseña debe tener al menos 8 caracteres",
    "codigo": 400
}
```

---

## 10. Endpoint 2 — Registro de transportista

### Configuración en Postman

| Campo | Valor |
|---|---|
| Método | **POST** |
| URL | `{{base_url}}/api/auth/registro` |
| Autenticación | Ninguna |

### Body (JSON):

```json
{
    "nombre": "Carlos",
    "apellidos": "Martínez Ruiz",
    "email": "carlos.transportista@pispax.com",
    "password": "Transport456",
    "telefono": "677888999",
    "rol": "TRANSPORTISTA"
}
```

### Respuesta esperada — 201 Created:

```json
{
    "id": 2,
    "nombre": "Carlos",
    "apellidos": "Martínez Ruiz",
    "email": "carlos.transportista@pispax.com",
    "telefono": "677888999",
    "rol": "TRANSPORTISTA"
}
```

---

## 11. Endpoint 3 — Login

Este es el endpoint más importante porque devuelve el **token JWT**.

### 11.1 Login como CLIENTE

| Campo | Valor |
|---|---|
| Método | **POST** |
| URL | `{{base_url}}/api/auth/login` |
| Autenticación | Ninguna |

### Body:

```json
{
    "email": "maria.cliente@pispax.com",
    "password": "Password123"
}
```

### Respuesta esperada — 200 OK:

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtYXJpYS5jbGllbnRlQHBpc3BheC5jb20iLCJ1c2VySWQiOjEsInJvbCI6IkNMSUVOVEUiLCJpYXQiOjE3NDg0NTY3ODksImV4cCI6MTc0ODU0MzE4OX0.firma_aqui",
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

### Pestaña Tests — Script para guardar token:

```javascript
if (pm.response.code === 200) {
    pm.environment.set("token_cliente", pm.response.json().token);
}
```

### 11.2 Login como TRANSPORTISTA

Misma configuración, diferente body:

```json
{
    "email": "carlos.transportista@pispax.com",
    "password": "Transport456"
}
```

Pestaña Tests:

```javascript
if (pm.response.code === 200) {
    pm.environment.set("token_transportista", pm.response.json().token);
}
```

### Cómo usar el token en Postman

En cada petición que requiera autenticación:

1. Pestaña **"Authorization"**
2. Type: **"Bearer Token"**
3. Token: `{{token_cliente}}` o `{{token_transportista}}`

O manualmente en la pestaña **"Headers"**:

| Key | Value |
|---|---|
| `Authorization` | `Bearer {{token_cliente}}` |

### Descodificar el token para inspeccionarlo

Copia el token y pégalo en `https://jwt.io`. Verás algo así:

```
HEADER:
{
  "alg": "HS256"
}

PAYLOAD:
{
  "sub": "maria.cliente@pispax.com",
  "userId": 1,
  "rol": "CLIENTE",
  "iat": 1748456789,
  "exp": 1748543189
}
```

Los timestamps (`iat`, `exp`) son segundos Unix. Para convertirlos a fecha legible:

```javascript
// En la consola del navegador:
new Date(1748456789 * 1000).toLocaleString()
// → "28/5/2026, 20:46:29"
```

### Posibles errores en login

**400 — Credenciales incorrectas (email no existe):**
```json
{
    "mensaje": "Credenciales incorrectas",
    "codigo": 400
}
```

**400 — Credenciales incorrectas (password mal):**
```json
{
    "mensaje": "Credenciales incorrectas",
    "codigo": 400
}
```

El servidor devuelve el mismo mensaje en ambos casos a propósito: no queremos revelar si el email existe o no (seguridad por oscuridad).

---

## 12. Endpoint 4 — Tipos de mercancía (público)

Este endpoint **no requiere token**. Es el catálogo que el cliente usa para seleccionar el tipo de carga.

| Campo | Valor |
|---|---|
| Método | **GET** |
| URL | `{{base_url}}/api/tipo-mercancia` |
| Autenticación | Ninguna |

### Respuesta esperada — 200 OK:

```json
[
    { "id": 1,  "nombre": "Paquetería general",         "descripcion": "Paquetes, cajas, envíos estándar" },
    { "id": 2,  "nombre": "Electrónica frágil",         "descripcion": "Dispositivos frágiles, equipos electrónicos" },
    { "id": 3,  "nombre": "Alimentos frescos",          "descripcion": "Productos perecederos (0–8°C), requiere frío" },
    { "id": 4,  "nombre": "Alimentos congelados",       "descripcion": "Productos congelados (< -18°C)" },
    { "id": 5,  "nombre": "Materiales de construcción", "descripcion": "Cemento, acero, materiales pesados" },
    { "id": 6,  "nombre": "Mobiliario",                 "descripcion": "Muebles, sofás, piezas grandes" },
    { "id": 7,  "nombre": "Mercancía peligrosa (ADR)",  "descripcion": "Sustancias químicas, inflamables, tóxicas" },
    { "id": 8,  "nombre": "Productos farmacéuticos",    "descripcion": "Medicinas, biologics — GDP recomendado" },
    { "id": 9,  "nombre": "Textil y moda",              "descripcion": "Ropa, telas, calzado" },
    { "id": 10, "nombre": "Maquinaria industrial",      "descripcion": "Máquinas, equipos pesados" },
    { "id": 11, "nombre": "Documentación y archivo",    "descripcion": "Documentos, libros, archivos" },
    { "id": 12, "nombre": "Vehículos y automoción",     "descripcion": "Coches, motos, piezas vehiculares" }
]
```

Guarda los `id` de los tipos que quieras usar al crear un viaje.

---

## 13. Endpoint 5 — Registrar vehículo

Solo los TRANSPORTISTAS pueden registrar vehículos.

| Campo | Valor |
|---|---|
| Método | **POST** |
| URL | `{{base_url}}/api/vehiculos` |
| Authorization | Bearer `{{token_transportista}}` |

### Body (JSON):

```json
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

### Valores válidos para los campos ENUM

**`tipoVehiculo`** — valores exactos que acepta el servidor:
```
FURGONETA
FURGON_GRANDE
FRIGORIFICO
CAMION_LIGERO
CAMION_PESADO
CAMION_ARTICULADO
PLATAFORMA
```

**`carnetRequerido`** — valores exactos:
```
B
C1
C
C_E
```

### Respuesta esperada — 201 Created:

```json
{
    "id": 1,
    "matricula": "1234ABC",
    "marca": "Mercedes",
    "modelo": "Sprinter 316",
    "tipoVehiculo": "FURGONETA",
    "subtipo": "Refrigerada",
    "taraKg": 2100.00,
    "capacidadKg": 1500.00,
    "mmaKg": 3500.00,
    "carnetRequerido": "B",
    "transportistaId": 2
}
```

### Pestaña Tests — Guardar el ID del vehículo:

```javascript
if (pm.response.code === 201) {
    pm.environment.set("vehiculo_id", pm.response.json().id);
}
```

### Posibles errores

**403 — Si usas el token de CLIENTE:**
```
403 Forbidden (sin body JSON, Spring Security lo bloquea antes de llegar al controller)
```

**400 — Matrícula duplicada:**
```json
{
    "mensaje": "...",
    "codigo": 400
}
```

---

## 14. Endpoint 6 — Mis vehículos

| Campo | Valor |
|---|---|
| Método | **GET** |
| URL | `{{base_url}}/api/vehiculos/mis-vehiculos` |
| Authorization | Bearer `{{token_transportista}}` |

No lleva body.

### Respuesta esperada — 200 OK:

```json
[
    {
        "id": 1,
        "matricula": "1234ABC",
        "marca": "Mercedes",
        "modelo": "Sprinter 316",
        "tipoVehiculo": "FURGONETA",
        "capacidadKg": 1500.00,
        "carnetRequerido": "B",
        "transportistaId": 2
    }
]
```

---

## 15. Endpoint 7 — Crear viaje

Solo los CLIENTES pueden crear viajes.

| Campo | Valor |
|---|---|
| Método | **POST** |
| URL | `{{base_url}}/api/viajes` |
| Authorization | Bearer `{{token_cliente}}` |

### Body (JSON):

```json
{
    "tipoMercanciaId": 1,
    "descripcionMercancia": "Caja grande con ropa de temporada",
    "direccionRecogida": "Calle Mayor 15, 28013 Madrid",
    "direccionEntrega": "Avenida Diagonal 200, 08018 Barcelona",
    "pesoKg": 50.5
}
```

### Respuesta esperada — 201 Created:

```json
{
    "id": 1,
    "clienteId": 1,
    "clienteNombre": "María García López",
    "transportistaId": null,
    "transportistaNombre": null,
    "vehiculoId": null,
    "vehiculoMatricula": null,
    "tipoMercanciaId": 1,
    "tipoMercanciaNombre": "Paquetería general",
    "descripcionMercancia": "Caja grande con ropa de temporada",
    "direccionRecogida": "Calle Mayor 15, 28013 Madrid",
    "direccionEntrega": "Avenida Diagonal 200, 08018 Barcelona",
    "pesoKg": 50.50,
    "estado": "PENDIENTE",
    "fechaSolicitud": "2026-05-28T20:46:29",
    "fechaInicio": null,
    "fechaFin": null
}
```

Los campos `transportistaId`, `vehiculoId`, `fechaInicio` y `fechaFin` son `null` porque el viaje acaba de crearse y está PENDIENTE.

### Pestaña Tests — Guardar el ID del viaje:

```javascript
if (pm.response.code === 201) {
    pm.environment.set("viaje_id", pm.response.json().id);
}
```

---

## 16. Endpoint 8 — Ver mis viajes

- Si el token es de CLIENTE → devuelve los viajes que creó ese cliente
- Si el token es de TRANSPORTISTA → devuelve los viajes que aceptó ese transportista

| Campo | Valor |
|---|---|
| Método | **GET** |
| URL | `{{base_url}}/api/viajes` |
| Authorization | Bearer `{{token_cliente}}` o `{{token_transportista}}` |

### Respuesta esperada — 200 OK:

```json
[
    {
        "id": 1,
        "clienteId": 1,
        "clienteNombre": "María García López",
        "transportistaId": null,
        "estado": "PENDIENTE",
        "pesoKg": 50.50,
        "direccionRecogida": "Calle Mayor 15, 28013 Madrid",
        "direccionEntrega": "Avenida Diagonal 200, 08018 Barcelona",
        ...
    }
]
```

---

## 17. Endpoint 9 — Ver detalle de un viaje

| Campo | Valor |
|---|---|
| Método | **GET** |
| URL | `{{base_url}}/api/viajes/{{viaje_id}}` |
| Authorization | Bearer `{{token_cliente}}` o `{{token_transportista}}` |

### Respuesta — 200 OK:

Igual que el objeto ViajeDTO completo mostrado en el Endpoint 7.

### Posibles errores

**404 — Viaje no existe:**
```json
{
    "mensaje": "Viaje no encontrado",
    "codigo": 404
}
```

---

## 18. Endpoint 10 — Viajes disponibles para transportista

Devuelve todos los viajes en estado PENDIENTE que el transportista puede aceptar. Solo accesible con token de TRANSPORTISTA.

| Campo | Valor |
|---|---|
| Método | **GET** |
| URL | `{{base_url}}/api/viajes/disponibles` |
| Authorization | Bearer `{{token_transportista}}` |

### Respuesta — 200 OK:

```json
[
    {
        "id": 1,
        "clienteId": 1,
        "clienteNombre": "María García López",
        "transportistaId": null,
        "estado": "PENDIENTE",
        "tipoMercanciaNombre": "Paquetería general",
        "pesoKg": 50.50,
        "direccionRecogida": "Calle Mayor 15, 28013 Madrid",
        "direccionEntrega": "Avenida Diagonal 200, 08018 Barcelona",
        "fechaSolicitud": "2026-05-28T20:46:29",
        ...
    }
]
```

### Posibles errores

**403 — Si usas el token de CLIENTE:**
```
403 Forbidden
```

---

## 19. Endpoint 11 — Aceptar un viaje

El transportista acepta un viaje PENDIENTE y asigna su vehículo.

| Campo | Valor |
|---|---|
| Método | **PUT** |
| URL | `{{base_url}}/api/viajes/{{viaje_id}}/aceptar` |
| Authorization | Bearer `{{token_transportista}}` |

### Body (JSON):

```json
{
    "vehiculoId": {{vehiculo_id}}
}
```

O con el valor directo:

```json
{
    "vehiculoId": 1
}
```

### Respuesta esperada — 200 OK:

```json
{
    "id": 1,
    "clienteId": 1,
    "clienteNombre": "María García López",
    "transportistaId": 2,
    "transportistaNombre": "Carlos Martínez Ruiz",
    "vehiculoId": 1,
    "vehiculoMatricula": "1234ABC",
    "estado": "ACEPTADO",
    "fechaInicio": "2026-05-28T21:00:00",
    "fechaFin": null,
    ...
}
```

Fíjate: el `estado` cambió a `ACEPTADO` y `fechaInicio` ya tiene valor.

### Posibles errores

**409 — El viaje no está PENDIENTE:**
```json
{
    "mensaje": "Solo se pueden aceptar viajes en estado PENDIENTE",
    "codigo": 409
}
```

**400 — El peso excede la capacidad del vehículo:**
```json
{
    "mensaje": "El peso del viaje excede la capacidad del vehículo",
    "codigo": 400
}
```

**400 — El vehículo no es tuyo:**
```json
{
    "mensaje": "El vehículo no pertenece a este transportista",
    "codigo": 400
}
```

---

## 20. Endpoint 12 — Actualizar estado del viaje

El transportista va actualizando el estado a medida que avanza el trayecto.

| Campo | Valor |
|---|---|
| Método | **PUT** |
| URL | `{{base_url}}/api/viajes/{{viaje_id}}/estado` |
| Authorization | Bearer `{{token_transportista}}` |

### Cuerpo genérico:

```json
{
    "nuevoEstado": "SALIDA_RECOGIDA"
}
```

### Secuencia de llamadas en orden correcto:

**Paso 1 — Salida hacia el punto de recogida:**
```json
{ "nuevoEstado": "SALIDA_RECOGIDA" }
→ 200 OK: estado = "SALIDA_RECOGIDA"
```

**Paso 2 — Llegada al punto de recogida:**
```json
{ "nuevoEstado": "LLEGADA_RECOGIDA" }
→ 200 OK: estado = "LLEGADA_RECOGIDA"
```

**Paso 3 — Mercancía cargada en el vehículo:**
```json
{ "nuevoEstado": "MERCANCIA_RECOGIDA" }
→ 200 OK: estado = "MERCANCIA_RECOGIDA"
```

**Paso 4 — Salida hacia el destino:**
```json
{ "nuevoEstado": "SALIDA_ENTREGA" }
→ 200 OK: estado = "SALIDA_ENTREGA"
```

**Paso 5 — Llegada al destino:**
```json
{ "nuevoEstado": "LLEGADA_ENTREGA" }
→ 200 OK: estado = "LLEGADA_ENTREGA"
```

**Paso 6 — Entrega completada:**
```json
{ "nuevoEstado": "COMPLETADO" }
→ 200 OK: estado = "COMPLETADO", fechaFin = "2026-05-28T23:00:00"
```

**Cancelar (desde cualquier estado excepto COMPLETADO/CANCELADO):**
```json
{ "nuevoEstado": "CANCELADO" }
→ 200 OK: estado = "CANCELADO", fechaFin = "2026-05-28T21:30:00"
```

### Posibles errores

**409 — Transición inválida (saltar un estado):**
```json
{
    "mensaje": "Transición inválida: ACEPTADO → MERCANCIA_RECOGIDA",
    "codigo": 409
}
```

**409 — Intentar cancelar un viaje ya terminado:**
```json
{
    "mensaje": "No se puede cancelar un viaje ya finalizado",
    "codigo": 409
}
```

**403 — El transportista no es el asignado al viaje:**
```json
{
    "mensaje": "No tienes permiso para actualizar este viaje",
    "codigo": 403
}
```

---

## 21. Flujo completo de prueba de extremo a extremo

Este es el guión completo para demostrar en la presentación del TFG que todo el sistema funciona:

```
PRERREQUISITO: Servidor arrancado (mvn spring-boot:run)

═══════════════════════════════════════════════
BLOQUE 1 — SETUP (sin token)
═══════════════════════════════════════════════

1. GET  /api/tipo-mercancia
   → Comprobar que devuelve los 12 tipos

2. POST /api/auth/registro (CLIENTE: maria.cliente@pispax.com)
   → 201 Created, id=1

3. POST /api/auth/registro (TRANSPORTISTA: carlos.transportista@pispax.com)
   → 201 Created, id=2

═══════════════════════════════════════════════
BLOQUE 2 — AUTENTICACIÓN
═══════════════════════════════════════════════

4. POST /api/auth/login (CLIENTE)
   → 200 OK, token guardado en {{token_cliente}}

5. POST /api/auth/login (TRANSPORTISTA)
   → 200 OK, token guardado en {{token_transportista}}

═══════════════════════════════════════════════
BLOQUE 3 — TRANSPORTISTA prepara su vehículo
═══════════════════════════════════════════════

6. POST /api/vehiculos (con token transportista)
   Body: { matricula: "1234ABC", capacidadKg: 1500, ... }
   → 201 Created, vehiculo.id = 1, guardado en {{vehiculo_id}}

7. GET /api/vehiculos/mis-vehiculos (con token transportista)
   → Lista con el vehículo recién creado

═══════════════════════════════════════════════
BLOQUE 4 — CLIENTE crea un viaje
═══════════════════════════════════════════════

8. POST /api/viajes (con token cliente)
   Body: { tipoMercanciaId: 1, pesoKg: 50.5, ... }
   → 201 Created, estado = "PENDIENTE", viaje.id = 1, guardado en {{viaje_id}}

9. GET /api/viajes (con token cliente)
   → Lista con el viaje en estado PENDIENTE

═══════════════════════════════════════════════
BLOQUE 5 — TRANSPORTISTA acepta y ejecuta el viaje
═══════════════════════════════════════════════

10. GET  /api/viajes/disponibles (con token transportista)
    → Lista con el viaje de María

11. PUT  /api/viajes/{{viaje_id}}/aceptar (con token transportista)
    Body: { vehiculoId: 1 }
    → 200 OK, estado = "ACEPTADO"

12. PUT  /api/viajes/{{viaje_id}}/estado (con token transportista)
    Body: { nuevoEstado: "SALIDA_RECOGIDA" }
    → 200 OK, estado = "SALIDA_RECOGIDA"

13. PUT  /api/viajes/{{viaje_id}}/estado
    Body: { nuevoEstado: "LLEGADA_RECOGIDA" }
    → 200 OK, estado = "LLEGADA_RECOGIDA"

14. PUT  /api/viajes/{{viaje_id}}/estado
    Body: { nuevoEstado: "MERCANCIA_RECOGIDA" }
    → 200 OK

15. PUT  /api/viajes/{{viaje_id}}/estado
    Body: { nuevoEstado: "SALIDA_ENTREGA" }
    → 200 OK

16. PUT  /api/viajes/{{viaje_id}}/estado
    Body: { nuevoEstado: "LLEGADA_ENTREGA" }
    → 200 OK

17. PUT  /api/viajes/{{viaje_id}}/estado
    Body: { nuevoEstado: "COMPLETADO" }
    → 200 OK, estado = "COMPLETADO", fechaFin ≠ null

═══════════════════════════════════════════════
BLOQUE 6 — Verificación final
═══════════════════════════════════════════════

18. GET /api/viajes/{{viaje_id}} (con cualquier token)
    → Viaje con estado COMPLETADO, fechaInicio y fechaFin rellenos

19. GET /api/viajes (con token cliente)
    → Muestra el viaje completado

20. GET /api/viajes (con token transportista)
    → Muestra el viaje completado (como transportista asignado)
```

---

## 22. Pruebas de error — qué debe fallar y por qué

Estas pruebas son igualmente importantes en el TFG. Demuestran que el sistema rechaza correctamente las peticiones inválidas.

### Errores de autenticación

**A. Petición sin token a endpoint protegido:**
```
GET /api/viajes (sin header Authorization)
→ 403 Forbidden
```
Por qué: Spring Security rechaza toda petición a rutas protegidas sin autenticación.

**B. Token caducado o modificado:**
```
GET /api/viajes
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.MANIPULADO.firma
→ 403 Forbidden
```
Por qué: JwtAuthFilter detecta que la firma no coincide y no inyecta la autenticación.

### Errores de autorización por rol

**C. Cliente intentando ver viajes disponibles (solo transportistas):**
```
GET /api/viajes/disponibles
Authorization: Bearer {{token_cliente}}
→ 403 Forbidden
```
Por qué: `@PreAuthorize("hasRole('TRANSPORTISTA')")` en el controller.

**D. Cliente intentando crear un vehículo:**
```
POST /api/vehiculos
Authorization: Bearer {{token_cliente}}
→ 403 Forbidden
```
Por qué: el controller `VehiculoController` tiene `@PreAuthorize("hasRole('TRANSPORTISTA')")` a nivel de clase.

**E. Transportista intentando crear un viaje:**
```
POST /api/viajes
Authorization: Bearer {{token_transportista}}
→ 403 Forbidden
```
Por qué: `@PreAuthorize("hasRole('CLIENTE')")` en `ViajeController.crearViaje()`.

### Errores de validación de datos

**F. Registro con email duplicado:**
```json
POST /api/auth/registro
{ "email": "maria.cliente@pispax.com", ... }
→ 400 Bad Request: "Ya existe un usuario con ese email"
```

**G. Login con contraseña incorrecta:**
```json
POST /api/auth/login
{ "email": "maria.cliente@pispax.com", "password": "contraseñaMal" }
→ 400 Bad Request: "Credenciales incorrectas"
```

**H. Crear viaje con peso negativo:**
```json
POST /api/viajes
{ "pesoKg": -10.0, ... }
→ 400 Bad Request: "pesoKg: El peso debe ser mayor que 0"
```

**I. Crear viaje sin dirección de entrega:**
```json
POST /api/viajes
{ "direccionEntrega": "", ... }
→ 400 Bad Request: "direccionEntrega: La dirección de entrega es obligatoria"
```

### Errores de lógica de negocio

**J. Aceptar un viaje que ya está ACEPTADO:**
```
PUT /api/viajes/1/aceptar
→ 409 Conflict: "Solo se pueden aceptar viajes en estado PENDIENTE"
```

**K. Saltar un estado del viaje:**
```json
PUT /api/viajes/1/estado
{ "nuevoEstado": "COMPLETADO" }  ← cuando el estado es ACEPTADO
→ 409 Conflict: "Transición inválida: ACEPTADO → COMPLETADO"
```

**L. Asignar un vehículo que no es tuyo:**
```json
PUT /api/viajes/1/aceptar
{ "vehiculoId": 99 }  ← vehículo de otro transportista
→ 400 Bad Request: "El vehículo no pertenece a este transportista"
```

**M. Peso del viaje supera la capacidad del vehículo:**
```json
PUT /api/viajes/1/aceptar
{ "vehiculoId": 1 }  ← vehiculo capacidad 1500kg, viaje pesa 2000kg
→ 400 Bad Request: "El peso del viaje excede la capacidad del vehículo"
```

**N. Cancelar un viaje ya completado:**
```json
PUT /api/viajes/1/estado
{ "nuevoEstado": "CANCELADO" }  ← viaje ya en COMPLETADO
→ 409 Conflict: "No se puede cancelar un viaje ya finalizado"
```

**O. Actualizar estado de un viaje que no es tuyo:**
```
PUT /api/viajes/1/estado (con token del transportista 3, pero el viaje lo aceptó el transportista 2)
→ 403 Forbidden: "No tienes permiso para actualizar este viaje"
```

---

*Documento generado el 28/05/2026 — PisPax TFG DAM 2025/2026 — Adrián Salazar*
