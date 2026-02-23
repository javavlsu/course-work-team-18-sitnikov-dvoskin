package ru.cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения Cinema Service.
 * Сервис поиска и рекомендаций фильмов и сериалов.
 */
@SpringBootApplication
public class CinemaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaServiceApplication.class, args);
    }
}
