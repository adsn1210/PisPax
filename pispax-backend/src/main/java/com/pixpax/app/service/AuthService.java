package com.pixpax.app.service;

// Servicio de autenticación: gestiona el registro y el login de usuarios.
// Es la única parte de la aplicación que crea tokens JWT y encripta contraseñas.

import com.pixpax.app.dto.UsuarioDTO;
import com.pixpax.app.dto.request.LoginRequest;
import com.pixpax.app.dto.request.RegistroRequest;
import com.pixpax.app.entity.Usuario;
import com.pixpax.app.repository.UsuarioRepository;
import com.pixpax.app.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pixpax.app.dto.LoginResponse;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // BCrypt: encripta y verifica contraseñas
    private final JwtUtil jwtUtil;                 // genera y valida tokens JWT

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // @Transactional = si algo falla a mitad, la BD hace rollback automáticamente
    @Transactional
    public UsuarioDTO registro(RegistroRequest request) {
        // No se permite tener dos usuarios con el mismo email
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        // Construimos el usuario con el Builder de Lombok (patron creacional)
        // La contraseña se guarda encriptada con BCrypt, nunca en texto plano
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(request.getRol())
                .build();

        // Guardamos en BD y devolvemos un DTO (sin contraseña)
        return new UsuarioDTO(usuarioRepository.save(usuario));
    }

    // readOnly=true = Hibernate sabe que solo va a leer, lo optimiza internamente
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // Si no existe el email, lanzamos 401 (mismo mensaje que si la contraseña es mala,
        // para no dar pistas de si el email existe o no)
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        // BCrypt compara la contraseña en texto plano con el hash almacenado
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        // Generamos el token JWT con el email, ID y rol del usuario
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getRol().name());
        return new LoginResponse(token, new UsuarioDTO(usuario));
    }
}
