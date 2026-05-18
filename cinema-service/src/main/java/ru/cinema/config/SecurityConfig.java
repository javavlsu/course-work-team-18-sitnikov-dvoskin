package ru.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.cinema.security.JwtAuthFilter;

import java.util.List;

/**
 * Конфигурация безопасности с JWT-фильтром.
 * <p>
 * Публично доступны (permitAll):
 * - GET /api/v1/content/**, /api/v1/movies/**, /api/v1/series/**, /api/v1/tags/**
 * - GET /api/v1/users/{username}, /api/v1/users/{username}/reviews|playlists
 * - GET /api/v1/playlists, /api/v1/playlists/{id}
 * - GET /api/v1/reviews, /api/v1/reviews/{id}, /api/v1/content/{id}/comments
 * - POST /api/v1/reviews/{id}/view
 * - GET /api/v1/search
 * - /api/v1/auth/** (login, register, refresh)
 * - вся статика и Swagger
 * <p>
 * /api/v1/admin/** — требует ROLE_ADMIN.
 * Всё остальное /api/** — authenticated.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .authorizeHttpRequests(auth -> auth
                        // === Статика и инструменты ===
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**", "/img/**", "/partials/**", "/uploads/**",
                                "/*.html",
                                "/movies", "/movies/**", "/series", "/series/**",
                                "/content/**", "/login", "/register", "/profile",
                                "/users/**", "/me/**", "/reviews", "/reviews/**",
                                "/playlists", "/playlists/**", "/admin", "/admin/**",
                                "/search"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/v3/api-docs",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // === Auth endpoints ===
                        .requestMatchers("/api/v1/auth/register",
                                         "/api/v1/auth/login",
                                         "/api/v1/auth/refresh").permitAll()

                        // === Public GET endpoints ===
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/content/**",
                                "/api/v1/movies/**",
                                "/api/v1/series/**",
                                "/api/v1/tags/**",
                                "/api/v1/genres",
                                "/api/v1/genres/**",
                                "/api/v1/persons",
                                "/api/v1/persons/**",
                                "/api/v1/search",
                                "/api/v1/reviews",
                                "/api/v1/reviews/*",
                                "/api/v1/recommendations/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/*",
                                "/api/v1/users/*/reviews",
                                "/api/v1/users/*/playlists",
                                "/api/v1/users/*/followers",
                                "/api/v1/users/*/following"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/playlists",
                                "/api/v1/playlists/*"
                        ).permitAll()

                        // increment view count is public
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/*/view").permitAll()

                        // report broken poster — публичный (срабатывает из <img onerror>
                        // на любых страницах, включая анонимных пользователей).
                        .requestMatchers(HttpMethod.POST, "/api/v1/content/*/report-broken-poster").permitAll()

                        // === Admin only ===
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // === Всё остальное API — authenticated ===
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().permitAll()
                )
                // Для анонимных запросов на защищённый эндпоинт — 401, не 403.
                // Фронт ловит 401 и редиректит на /login (через api.js silent refresh).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write(
                                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Требуется аутентификация\"}");
                        })
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Разрешаем frame для H2 console (на dev профиле)
        http.headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}
