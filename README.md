# Cinema Service (MovieHub)

Курсовой проект по дисциплине «Распределённые программные системы».
Сервис поиска и рекомендаций фильмов и сериалов.

## Локальный запуск через Docker

Требуется только Docker (например, Colima на macOS):
```bash
brew install colima docker docker-compose
colima start --cpu 2 --memory 4
```

Запуск всего стека (PostgreSQL + Spring Boot):
```bash
docker compose up --build
```

Приложение поднимется на <http://localhost:8080>, PostgreSQL — на `localhost:5432`
(БД `cinemadb`, пользователь `cinema`/`cinema`).

### Hot-reload

Исходники `cinema-service/src` примонтированы в контейнер. Spring Boot DevTools
(подключён в `pom.xml`) перезапускает контекст приложения при пересборке классов.
Чтобы изменения «пролились» — нужно скомпилировать классы внутри контейнера:

```bash
docker compose exec app mvn -B compile
```

Либо просто пересобрать контейнер (медленнее):
```bash
docker compose up --build app
```

### Логи / остановка

```bash
docker compose logs -f app
docker compose down            # сохранит данные Postgres
docker compose down -v         # снесёт том БД
```

## Структура

- `cinema-service/` — Spring Boot 3.2 / Java 17 (бэкенд + статика фронта)
- `Этап 2/`, `Этап 3/`, `Этап 4/` — курсовая документация по этапам
