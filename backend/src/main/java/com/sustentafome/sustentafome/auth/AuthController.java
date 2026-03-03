package com.sustentafome.sustentafome.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailTokenService emailTokenService;

    private static final Pattern PASSWORD_RULE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,16}$");

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          EmailTokenService emailTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailTokenService = emailTokenService;
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

    @PostMapping("/email-token")
    public ResponseEntity<EmailTokenResponse> sendEmailToken(@RequestBody @Valid EmailTokenRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(409).body(new EmailTokenResponse("E-mail já cadastrado", null));
        }
        String code = emailTokenService.generateToken(request.email());
        // Aqui deveria enviar o código por e-mail; para ambiente de dev retornamos o token para facilitar testes.
        return ResponseEntity.ok(new EmailTokenResponse("Código enviado para o e-mail informado", code));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> fullRegister(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(409).build();
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(409).build();
        }
        if (!request.password().equals(request.confirmPassword())) {
            return ResponseEntity.badRequest().build();
        }
        if (!PASSWORD_RULE.matcher(request.password()).matches()) {
            return ResponseEntity.badRequest().build();
        }
        if (!emailTokenService.validate(request.email(), request.verificationCode())) {
            return ResponseEntity.status(400).build();
        }
        AppUser user = AppUser.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(request.username())
                .email(request.email())
                .phone(request.phone())
                .emailVerified(true)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.VISUALIZADOR)
                .build();
        userRepository.save(user);
        emailTokenService.consume(request.email());

        String token = jwtService.generateToken(user, Map.of("role", user.getRole().name()));
        String refresh = jwtService.generateToken(user, Map.of("role", user.getRole().name(), "refresh", true));
        return ResponseEntity.ok(new AuthResponse(token, refresh, user.getRole().name()));
    }
}
