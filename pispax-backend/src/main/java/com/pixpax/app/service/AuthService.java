package com.pixpax.app.service;

import com.pixpax.app.dto.UsuarioDTO;
import com.pixpax.app.dto.request.LoginRequest;
import com.pixpax.app.dto.request.RegistroRequest;
import com.pixpax.app.entity.Usuario;
import com.pixpax.app.repository.UsuarioRepository;
import com.pixpax.app.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UsuarioDTO registro(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(request.getRol())
                .build();

        return new UsuarioDTO(usuarioRepository.save(usuario));
    }

    public Map<String, Object> login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId(), usuario.getRol().name());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("usuario", new UsuarioDTO(usuario));
        return response;
    }
}
