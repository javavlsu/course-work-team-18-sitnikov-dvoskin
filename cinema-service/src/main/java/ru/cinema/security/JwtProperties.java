package ru.cinema.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Конфигурация JWT.
 * Считывается из переменных окружения / application.yml через префикс {@code app.jwt}.
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HMAC-SHA256 секрет (>= 32 байт). Если не задан — генерируется при старте. */
    private String secret;

    /** TTL access-токена в секундах. По умолчанию 15 минут. */
    private long accessTtlSeconds = 15 * 60;

    /** TTL refresh-токена в секундах. По умолчанию 7 дней. */
    private long refreshTtlSeconds = 7 * 24 * 60 * 60;

    /** Issuer для токенов. */
    private String issuer = "cinema-service";

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTtlSeconds() { return accessTtlSeconds; }
    public void setAccessTtlSeconds(long accessTtlSeconds) { this.accessTtlSeconds = accessTtlSeconds; }

    public long getRefreshTtlSeconds() { return refreshTtlSeconds; }
    public void setRefreshTtlSeconds(long refreshTtlSeconds) { this.refreshTtlSeconds = refreshTtlSeconds; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
}
