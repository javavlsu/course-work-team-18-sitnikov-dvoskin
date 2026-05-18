-- =====================================================
-- Cinema Service — схема для PostgreSQL
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    email           VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS content (
    id              BIGSERIAL PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS movies (
    content_id      BIGINT PRIMARY KEY,
    duration        INT,
    budget          DECIMAL(15, 2),
    box_office      DECIMAL(15, 2),
    CONSTRAINT fk_movies_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS series (
    content_id      BIGINT PRIMARY KEY,
    total_seasons   INT,
    total_episodes  INT,
    is_finished     BOOLEAN,
    CONSTRAINT fk_series_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    text            TEXT            NOT NULL,
    rating_value    INT             CHECK (rating_value IS NULL OR (rating_value >= 1 AND rating_value <= 5)),
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    view_count      INT             NOT NULL DEFAULT 0,
    like_count      INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    text            TEXT            NOT NULL,
    is_edited       BOOLEAN         NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PUBLISHED',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at       TIMESTAMP,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

-- Миграция: добавить status в comments если колонки нет (для уже созданной БД).
ALTER TABLE comments ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED';

-- Миграция: причина модерации рецензии (use-case D, альт. поток 1).
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS moderation_reason VARCHAR(500);

-- ===== Этап 3: Genre =====
CREATE TABLE IF NOT EXISTS genres (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(300)
);
CREATE TABLE IF NOT EXISTS content_genres (
    content_id      BIGINT          NOT NULL,
    genre_id        BIGINT          NOT NULL,
    PRIMARY KEY (content_id, genre_id),
    CONSTRAINT fk_cg_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT fk_cg_genre   FOREIGN KEY (genre_id)   REFERENCES genres(id)  ON DELETE CASCADE
);

-- ===== Этап 3: UserFollow (подписки на других пользователей) =====
CREATE TABLE IF NOT EXISTS user_follows (
    follower_id     BIGINT          NOT NULL,
    following_id    BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT fk_uf_follower  FOREIGN KEY (follower_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uf_following FOREIGN KEY (following_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_uf_self CHECK (follower_id <> following_id)
);

-- ===== Этап 2: Person (актёры / режиссёры) для use-case «поиск по актёрам/режиссёрам» =====
CREATE TABLE IF NOT EXISTS persons (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL UNIQUE,
    photo_url       VARCHAR(500),
    bio             TEXT,
    birth_date      DATE
);
CREATE TABLE IF NOT EXISTS content_persons (
    content_id      BIGINT          NOT NULL,
    person_id       BIGINT          NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    PRIMARY KEY (content_id, person_id, role),
    CONSTRAINT fk_cp_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_person  FOREIGN KEY (person_id)  REFERENCES persons(id) ON DELETE CASCADE,
    CONSTRAINT chk_cp_role CHECK (role IN ('ACTOR','DIRECTOR'))
);

-- ===== Этап 3: CollectionRating (оценка подборки 1–5) =====
CREATE TABLE IF NOT EXISTS playlist_ratings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    playlist_id     BIGINT          NOT NULL,
    "value"         INT             NOT NULL CHECK ("value" >= 1 AND "value" <= 5),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pr_user     FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
    CONSTRAINT fk_pr_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    CONSTRAINT uq_pr_user_playlist UNIQUE (user_id, playlist_id)
);

CREATE TABLE IF NOT EXISTS review_likes (
    id              BIGSERIAL PRIMARY KEY,
    review_id       BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_likes_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT uq_review_likes UNIQUE (review_id, user_id)
);

CREATE TABLE IF NOT EXISTS ratings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    "value"         INT             NOT NULL CHECK ("value" >= 1 AND "value" <= 5),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ratings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT uq_ratings_user_content UNIQUE (user_id, content_id)
);

CREATE TABLE IF NOT EXISTS playlists (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(100)    NOT NULL,
    description     TEXT,
    cover_image_url VARCHAR(500),
    is_public       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_playlists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS playlist_content (
    playlist_id     BIGINT          NOT NULL,
    content_id      BIGINT          NOT NULL,
    added_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sort_order      INT             NOT NULL DEFAULT 0,
    PRIMARY KEY (playlist_id, content_id),
    CONSTRAINT fk_pc_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tags (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(255),
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    usage_count     INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS content_tags (
    content_id      BIGINT          NOT NULL,
    tag_id          BIGINT          NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (content_id, tag_id),
    CONSTRAINT fk_ct_content FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    CONSTRAINT fk_ct_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- =====================================================
-- Индексы
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_content_status ON content(status);
CREATE INDEX IF NOT EXISTS idx_content_type ON content(content_type);
CREATE INDEX IF NOT EXISTS idx_content_release_year ON content(release_year);
CREATE INDEX IF NOT EXISTS idx_content_avg_rating ON content(average_rating);

-- Флаг битой обложки. Клиент репортит через POST /api/v1/content/{id}/report-broken-poster
-- (img onerror), список-эндпоинты потом исключают такие записи из выдачи.
ALTER TABLE content ADD COLUMN IF NOT EXISTS poster_broken BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_content_poster_broken ON content(poster_broken);

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
