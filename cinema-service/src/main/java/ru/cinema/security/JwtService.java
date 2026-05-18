package ru.cinema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.cinema.model.User;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Генерация и валидация JWT-токенов (access + refresh).
 * Используется HS256.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties props;
    private SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() {
        String secret = props.getSecret();
        byte[] bytes;
        if (secret == null || secret.isBlank()) {
            log.warn("JWT_SECRET не задан — генерируется случайный ключ. Перезапуск инвалидирует все токены.");
            bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
        } else {
            try {
                bytes = Decoders.BASE64.decode(secret);
            } catch (Exception e) {
                bytes = secret.getBytes();
            }
            if (bytes.length < 32) {
                log.warn("JWT_SECRET короче 32 байт — добиваю до требуемой длины. Не используйте такой секрет в проде.");
                byte[] padded = new byte[32];
                System.arraycopy(bytes, 0, padded, 0, bytes.length);
                bytes = padded;
            }
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        log.info("JwtService готов. accessTtl={}s, refreshTtl={}s", props.getAccessTtlSeconds(), props.getRefreshTtlSeconds());
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(props.getAccessTtlSeconds())))
                .claims(Map.of(
                        CLAIM_USERNAME, user.getUsername(),
                        CLAIM_ROLE, user.getRole().name(),
                        CLAIM_TYPE, TYPE_ACCESS
                ))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(props.getRefreshTtlSeconds())))
                .claims(Map.of(CLAIM_TYPE, TYPE_REFRESH))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Валидирует токен и возвращает claims. Бросает JwtException, если невалиден. */
    public Claims requireValid(String token, String expectedType) {
        Claims c = parse(token);
        Object typ = c.get(CLAIM_TYPE);
        if (typ == null || !expectedType.equals(typ.toString())) {
            throw new JwtException("Неверный тип токена: ожидался " + expectedType);
        }
        return c;
    }

    public long getAccessTtlSeconds() {
        return props.getAccessTtlSeconds();
    }
}
