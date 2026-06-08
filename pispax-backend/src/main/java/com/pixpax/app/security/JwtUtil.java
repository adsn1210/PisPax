package com.pixpax.app.security;

// Utilidad para crear y leer tokens JWT.
// JWT (JSON Web Token) es el mecanismo que usamos para que el cliente demuestre
// quién es en cada petición sin que el servidor tenga que guardar sesiones.
// Un token tiene tres partes: cabecera.payload.firma, separadas por puntos.

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")     // viene de application.properties
    private String secret;

    @Value("${jwt.expiration}") // tiempo en ms que dura el token (86400000 = 24 horas)
    private long expiration;

    private SecretKey key; // clave criptográfica generada a partir del secret

    // @PostConstruct = Spring ejecuta este método justo después de crear el objeto,
    // una sola vez. Aquí convertimos el texto del secret en una clave criptográfica.
    @PostConstruct
    private void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Genera un token JWT con los datos del usuario.
    // El token incluye: email (subject), ID del usuario, rol y fecha de expiración.
    public String generateToken(String email, Long userId, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key) // firma el token con la clave secreta (HMAC-SHA256)
                .compact();    // lo convierte al formato de texto final: xxx.yyy.zzz
    }

    // Extrae y verifica los datos del token. Si el token está manipulado o caducado, lanza excepción.
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // verifica la firma con la misma clave secreta
                .build()
                .parseSignedClaims(token)
                .getPayload(); // devuelve el contenido del token (email, userId, rol, fechas)
    }
}
