-- =====================================================
-- Скрипт создания базы данных Cinema Service
-- СУБД: PostgreSQL / H2 (совместимый синтаксис)
-- =====================================================

-- 1. Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 2. Таблица контента (базовая)
CREATE TABLE IF NOT EXISTS content (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    original_title  VARCHAR(255),
    description     TEXT,
    release_year    INT,
    poster_url      VARCHAR(500),
    average_rating  DECIMAL(3, 2)   DEFAULT 0.00,
    content_type    VARCHAR(20)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    country         VARCHAR(100),
    language        VARCHAR(50),
    imdb_id         VARCHAR(20),
    kinopoisk_id    VARCHAR(20),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 3. Таблица фильмов (наследование от content)
CREATE TABLE IF NOT EXISTS movies (
    content_id      BIGINT PRIMARY KEY,
    duration        INT,
    budget          DECIMAL(15, 2),
    box_office      DECIMAL(15, 2),
    CONSTRAINT fk_movies_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- 4. Таблица сериалов (наследование от content)
CREATE TABLE IF NOT EXISTS series (
    content_id      BIGINT PRIMARY KEY,
    total_seasons   INT,
    total_episodes  INT,
    is_finished     BOOLEAN,
    CONSTRAINT fk_series_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- 5. Таблица рецензий
CREATE TABLE IF NOT EXISTS reviews (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    text            TEXT            NOT NULL,
    rating_value    INT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    view_count      INT             NOT NULL DEFAULT 0,
    like_count      INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- 6. Таблица комментариев
CREATE TABLE IF NOT EXISTS comments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    text            TEXT            NOT NULL,
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at       TIMESTAMP,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- 7. Таблица оценок
CREATE TABLE IF NOT EXISTS ratings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    "value"         INT             NOT NULL CHECK ("value" >= 1 AND "value" <= 10),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT uq_ratings_user_content UNIQUE (user_id, content_id)
);

-- 8. Таблица подборок
CREATE TABLE IF NOT EXISTS playlists (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(100)    NOT NULL,
    description     TEXT,
    cover_image_url VARCHAR(500),
    is_public       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playlists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 9. Таблица связи подборка-контент
CREATE TABLE IF NOT EXISTS playlist_content (
    playlist_id     BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    added_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sort_order      INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (playlist_id, content_id),
    CONSTRAINT fk_pc_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- 10. Таблица тегов
CREATE TABLE IF NOT EXISTS tags (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(255),
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    usage_count     INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 11. Таблица связи контент-теги
CREATE TABLE IF NOT EXISTS content_tags (
    content_id      BIGINT          NOT NULL,
    tag_id          BIGINT          NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (content_id, tag_id),
    CONSTRAINT fk_ct_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT fk_ct_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- =====================================================
-- Индексы для оптимизации запросов
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_content_status ON content(status);
CREATE INDEX IF NOT EXISTS idx_content_type ON content(content_type);
CREATE INDEX IF NOT EXISTS idx_content_release_year ON content(release_year);
CREATE INDEX IF NOT EXISTS idx_content_avg_rating ON content(average_rating);

CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_content ON reviews(content_id);
CREATE INDEX IF NOT EXISTS idx_reviews_status ON reviews(status);

CREATE INDEX IF NOT EXISTS idx_comments_user ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_content ON comments(content_id);

CREATE INDEX IF NOT EXISTS idx_ratings_user ON ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_ratings_content ON ratings(content_id);

CREATE INDEX IF NOT EXISTS idx_playlists_user ON playlists(user_id);
CREATE INDEX IF NOT EXISTS idx_playlists_public ON playlists(is_public);

CREATE INDEX IF NOT EXISTS idx_tags_slug ON tags(slug);
