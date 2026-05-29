package ru.cinema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.cinema.model.User;
import ru.cinema.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Фильтр, который вытаскивает JWT из Authorization header и кладёт юзера в SecurityContext.
 * <p>
 * Роль и активность подтягиваются из БД на каждый запрос (а не из JWT claim),
 * чтобы admin-смена роли или ban пользователя действовали моментально —
 * без ожидания истечения текущего access-токена.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwt;
    private final UserRepository userRepo;

    public JwtAuthFilter(JwtService jwt, UserRepository userRepo) {
        this.jwt = jwt;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(PREFIX.length());
            try {
                Claims claims = jwt.requireValid(token, JwtService.TYPE_ACCESS);
                Long userId = Long.parseLong(claims.getSubject());

                // Подтягиваем актуальное состояние из БД: ban / смена роли должны
                // вступать в силу немедленно.
                Optional<User> userOpt = userRepo.findById(userId);
                if (userOpt.isEmpty() || Boolean.FALSE.equals(userOpt.get().getIsActive())) {
                    SecurityContextHolder.clearContext();
                } else {
                    User u = userOpt.get();
                    String role = u.getRole() == null ? "USER" : u.getRole().name();
                    AuthPrincipal principal = new AuthPrincipal(u.getId(), u.getUsername(), role);
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Невалидный JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
