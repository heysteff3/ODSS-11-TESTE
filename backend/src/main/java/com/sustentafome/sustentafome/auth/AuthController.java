package com.sustentafome.sustentafome.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUser user = (AppUser) authentication.getPrincipal();
        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));
        String refresh = jwtService.generateToken(user, Map.of("role", user.getRole().name(), "refresh", true));
        return ResponseEntity.ok(new AuthResponse(token, refresh, user.getRole().name()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.extractUsername(token);
        AppUser user = userRepository.findByUsername(username).orElseThrow();
        String newToken = jwtService.generateToken(user, Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(newToken, token, user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthRequest request) {
        AppUser user = AppUser.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.VISUALIZADOR)
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, null, user.getRole().name()));
    }
}
