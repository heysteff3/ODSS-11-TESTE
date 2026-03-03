package com.sustentafome.sustentafome.auth;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailTokenService {

    private record Token(String code, Instant expiresAt) {}

    private final Map<String, Token> tokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private static final Duration TTL = Duration.ofMinutes(15);

    public String generateToken(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        tokens.put(email.toLowerCase(), new Token(code, Instant.now().plus(TTL)));
        return code;
    }

    public boolean validate(String email, String code) {
        Token token = tokens.get(email.toLowerCase());
        if (token == null) return false;
        if (Instant.now().isAfter(token.expiresAt)) {
            tokens.remove(email.toLowerCase());
            return false;
        }
        return token.code().equals(code);
    }

    public void consume(String email) {
        tokens.remove(email.toLowerCase());
    }
}
