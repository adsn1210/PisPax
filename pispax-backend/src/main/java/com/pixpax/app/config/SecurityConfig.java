package com.pixpax.app.config;

// Configuración de seguridad de la aplicación.
// Aquí se decide qué endpoints necesitan token JWT y cuáles son públicos.
// También se configura el cifrado de contraseñas y se instala el filtro JWT.

import com.pixpax.app.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

// @EnableWebSecurity = activa Spring Security en la aplicación
// @EnableMethodSecurity = activa @PreAuthorize en los controllers (sin esto no funciona el control por rol)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // @Bean = Spring crea este objeto y lo gestiona. "filterChain" define las reglas de seguridad HTTP.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF porque usamos JWT (stateless), no cookies de sesión
            .csrf(AbstractHttpConfigurer::disable)
            // Sin sesiones: cada petición debe traer su token, el servidor no guarda nada
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Reglas de acceso por endpoint:
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html").permitAll()       // página de bienvenida
                .requestMatchers("/api/auth/**").permitAll()            // registro y login, sin token
                .requestMatchers("/api/tipo-mercancia").permitAll()     // catálogo público
                .anyRequest().authenticated()                           // todo lo demás necesita token
            )
            // Si llega una petición sin token a un endpoint protegido, devolvemos 401 en JSON
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"mensaje\":\"Autenticación requerida\",\"codigo\":401}");
                })
            )
            // Instalamos nuestro filtro JWT antes del filtro de login por defecto de Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt es el algoritmo estándar para cifrar contraseñas. Spring lo usa automáticamente
    // en AuthService para encriptar al registrar y verificar al hacer login.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
