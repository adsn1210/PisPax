# Levantamiento del Backend y Base de Datos — PisPax

## Requisitos previos

| Herramienta | Versión | Ruta instalada |
|---|---|---|
| Java (JDK) | 17 | `C:\Dependencias\jdk-17.0.12` |
| Maven | 3.9.x | `C:\Dependencias\apache-maven-3.9.14` |
| MySQL | 8.0 | `C:\Program Files\MySQL\bin\` |
| Servicio Windows | MYSQL80 | — |

---

## 1. Arrancar MySQL

### Primera vez / servidor apagado

Abrir **PowerShell como Administrador** y ejecutar:

```powershell
Start-Service MYSQL80
```

Verificar que está corriendo:

```powershell
(Get-Service MYSQL80).Status
# Debe mostrar: Running
```

> Si el servicio no existe o falla, abrir **MySQL Installer** y verificar que el servidor MySQL 8.0 está instalado y configurado como servicio de Windows.

### Crear la base de datos (solo la primera vez)

```powershell
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS pispax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

Pedirá la contraseña: `nairda04`

Verificar que existe:

```powershell
& "C:\Program Files\MySQL\bin\mysql.exe" -u root -p -e "SHOW DATABASES LIKE 'pispax';"
```

---

## 2. Levantar el backend Spring Boot

### Situarse en el directorio del proyecto

```powershell
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
```

### Compilar (solo si hay cambios en el código)

```powershell
mvn clean package -DskipTests
```

| Parte | Qué hace |
|---|---|
| `clean` | Borra `target/` para partir de cero |
| `package` | Compila y genera el `.jar` en `target/` |
| `-DskipTests` | Salta los tests para mayor velocidad |

### Arrancar el servidor

```powershell
mvn spring-boot:run
```

El servidor queda listo cuando en el log aparece:

```
Started PispaxApplication in X.XXX seconds
```

---

## 3. Verificar que todo funciona

### Página de estado (navegador)

Abrir: **http://localhost:8080**

Debe mostrar la página de estado de PisPax con el badge verde "Servidor activo".

### Smoke test por terminal

```powershell
# Debe devolver 400 con {"mensaje":"Credenciales incorrectas","codigo":400}
Invoke-RestMethod -Uri http://localhost:8080/api/auth/login `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"email":"test@test.com","password":"wrong"}'
```

O con curl (si está disponible):

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"wrong"}'
```

Respuesta esperada: `{"mensaje":"Credenciales incorrectas","codigo":400}`

---

## 4. Qué hace Spring Boot al arrancar

1. **Hibernate** conecta a `localhost:3306/pispax` con usuario `root`
2. `ddl-auto=update` crea o actualiza las tablas automáticamente si no existen
3. `data.sql` se ejecuta **después** de Hibernate (por `spring.jpa.defer-datasource-initialization=true`) e inserta los 12 tipos de mercancía con `INSERT IGNORE` (no duplica si ya existen)
4. El servidor queda escuchando en el **puerto 8080**

### Tablas creadas automáticamente

| Tabla | Descripción |
|---|---|
| `usuario` | Clientes y transportistas (con rol y BCrypt password) |
| `vehiculo` | Vehículos vinculados a transportistas |
| `tipo_mercancia` | Catálogo de 12 tipos (precargado por `data.sql`) |
| `vehiculo_tipo_mercancia` | Compatibilidades vehículo ↔ tipo mercancía |
| `viaje` | Solicitudes de transporte con estado y flujo |

---

## 5. Parar el servidor

Desde la terminal donde está corriendo:

```
Ctrl + C
```

Desde otra terminal (si corre en background), buscar y matar el proceso Java en el puerto 8080:

```powershell
# Obtener el PID
netstat -ano | findstr ":8080"

# Matar el proceso (sustituir XXXX por el PID)
Stop-Process -Id XXXX -Force
```

---

## 6. Configuración relevante (`application.properties`)

```properties
# Conexión MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/pispax?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=nairda04

# Hibernate
spring.jpa.hibernate.ddl-auto=update

# data.sql se ejecuta después de que Hibernate cree las tablas
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# Puerto
server.port=8080

# JWT (expiración: 24h en ms)
jwt.secret=pispax_jwt_secret_key_2026_muy_larga_para_seguridad_hmac_sha256
jwt.expiration=86400000
```

---

## Resumen rápido (arranque del día a día)

```powershell
# 1. PowerShell como Administrador
Start-Service MYSQL80

# 2. Terminal normal
cd C:\Users\adria\Documents\ClaudeIA\Code-Pispax\pispax-backend
mvn spring-boot:run

# 3. Abrir en el navegador
# http://localhost:8080
```
