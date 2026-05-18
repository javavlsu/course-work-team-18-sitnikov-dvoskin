-- =====================================================================
-- MovieHub — дополнительные индексы поверх schema-postgres.sql
-- Включает GIN-индекс полнотекстового поиска (русский словарь).
-- Применяется автоматически Spring SQL init после schema-postgres.sql.
-- =====================================================================

-- Полнотекстовый поиск по контенту (title + originalTitle + description)
-- Используем словарь 'russian' с приведением NULL -> ''
CREATE INDEX IF NOT EXISTS idx_content_fts
    ON content
    USING GIN (
        to_tsvector(
            'russian',
            COALESCE(title, '')
            || ' ' || COALESCE(original_title, '')
            || ' ' || COALESCE(description, '')
        )
    );

-- Композитный индекс для частых выборок «опубликованный фильм/сериал по году»
CREATE INDEX IF NOT EXISTS idx_content_status_type_year
    ON content (status, content_type, release_year DESC);

-- Индекс для top-rated сортировки
CREATE INDEX IF NOT EXISTS idx_content_status_rating
    ON content (status, average_rating DESC NULLS LAST);

-- Для be-recs / similar — частая выборка опубликованных c JOIN tags
CREATE INDEX IF NOT EXISTS idx_content_tags_tag_id ON content_tags (tag_id);
CREATE INDEX IF NOT EXISTS idx_content_tags_content_id ON content_tags (content_id);
