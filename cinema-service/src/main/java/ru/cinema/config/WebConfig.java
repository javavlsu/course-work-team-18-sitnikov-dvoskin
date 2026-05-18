package ru.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Pretty-URL маршрутизация на статические HTML-страницы.
 * Все ссылки в навигации используют чистые URL ({@code /movies}),
 * а Spring форвардит на соответствующий файл в {@code static/}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Главная
        registry.addViewController("/").setViewName("forward:/index.html");

        // Каталог (единый): /catalog. Старые /movies, /series, /search оставляем на
        // forward к их HTML-файлам — те HTML просто редиректят на /catalog с нужными параметрами.
        registry.addViewController("/catalog").setViewName("forward:/catalog.html");
        registry.addViewController("/movies").setViewName("forward:/movies.html");
        registry.addViewController("/series").setViewName("forward:/series.html");
        registry.addViewController("/search").setViewName("forward:/search.html");

        // Карточка контента: /movies/123 и /series/456 → одна страница content-detail.html
        registry.addViewController("/movies/{id:\\d+}").setViewName("forward:/content-detail.html");
        registry.addViewController("/series/{id:\\d+}").setViewName("forward:/content-detail.html");
        registry.addViewController("/content/{id:\\d+}").setViewName("forward:/content-detail.html");

        // Аутентификация
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/register").setViewName("forward:/register.html");

        // Профиль текущего пользователя и просмотр чужого
        registry.addViewController("/profile").setViewName("forward:/profile.html");
        registry.addViewController("/settings").setViewName("forward:/settings.html");
        registry.addViewController("/users/{username}").setViewName("forward:/user-profile.html");
        registry.addViewController("/me/reviews").setViewName("forward:/my-reviews.html");
        registry.addViewController("/me/playlists").setViewName("forward:/my-playlists.html");

        // Рецензии
        registry.addViewController("/reviews/new").setViewName("forward:/review-create.html");
        registry.addViewController("/reviews/{id:\\d+}").setViewName("forward:/review-detail.html");
        registry.addViewController("/reviews/{id:\\d+}/edit").setViewName("forward:/review-edit.html");

        // Подборки
        registry.addViewController("/playlists/new").setViewName("forward:/playlist-create.html");
        registry.addViewController("/playlists/{id:\\d+}").setViewName("forward:/playlist-detail.html");
        registry.addViewController("/playlists/{id:\\d+}/edit").setViewName("forward:/playlist-edit.html");

        // Админка
        registry.addViewController("/admin").setViewName("forward:/admin.html");
        registry.addViewController("/admin/content").setViewName("forward:/admin-content.html");
        registry.addViewController("/admin/users").setViewName("forward:/admin-users.html");
        registry.addViewController("/admin/reviews").setViewName("forward:/admin-reviews.html");
    }
}
