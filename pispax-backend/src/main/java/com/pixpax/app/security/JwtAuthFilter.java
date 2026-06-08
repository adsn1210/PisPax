package com.pixpax.app.security;

// Filtro de autenticación JWT.
// Spring Security llama a este filtro en CADA petición HTTP antes de que llegue al controller.
// Su trabajo es leer el token del header "Authorization", verificarlo y,
// si es válido, decirle a Spring quién es el usuario y qué rol tiene.
// Si no hay token o es inválido, simplemente deja pasar la petición sin autenticar
// (y luego Spring Security rechazará el acceso si el endpoint lo requiere).

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// OncePerRequestFilter garantiza que el filtro se ejecuta exactamente una vez por petición
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Leemos la cabecera "Authorization" de la petición HTTP
        String authHeader = request.getHeader("Authorization");

        // Si no hay cabecera o no empieza por "Bearer ", dejamos pasar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Quitamos el prefijo "Bearer " (7 caracteres) para quedarnos solo con el token
        String token = authHeader.substring(7);

        // Intentamos extraer los datos del token
        Claims claims;
        try {
            claims = jwtUtil.extractClaims(token);
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, "Token expirado");
            return;
        } catch (Exception e) {
            writeUnauthorized(response, "Token inválido");
            return;
        }

        // Sacamos los datos que metimos al generar el token en JwtUtil.generateToken()
        String email  = claims.getSubject();
        String rol    = claims.get("rol", String.class);
        Long userId   = claims.get("userId", Long.class);

        // Spring Security necesita el rol con el prefijo "ROLE_" (ej: "ROLE_CLIENTE")
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

        // Creamos el objeto de autenticación. El "credentials" lo usamos para guardar el userId
        // y recuperarlo fácilmente en los controllers con AuthUtils.getUserId()
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, userId, authorities);

        // Le decimos a Spring Security que este usuario está autenticado en esta petición
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // Continuamos con la cadena de filtros (siguiente filtro o el controller)
        filterChain.doFilter(request, response);
    }

    // Escribe una respuesta JSON de error 401 directamente, sin pasar por el controller
    private void writeUnauthorized(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\",\"codigo\":401}");
    }
}
