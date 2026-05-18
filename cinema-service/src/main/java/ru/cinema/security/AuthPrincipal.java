package ru.cinema.security;

/**
 * Principal в SecurityContext: компактные данные о юзере без обращения к БД.
 */
public record AuthPrincipal(Long id, String username, String role) {}
