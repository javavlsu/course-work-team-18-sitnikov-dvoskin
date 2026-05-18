-- =====================================================================
-- MovieHub — seed данные (PostgreSQL)
-- Идемпотентный INSERT: повторный запуск не порождает дубликатов.
-- Подключается через application-docker.yml -> spring.sql.init.data-locations
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. TAGS (20 жанров)
-- ---------------------------------------------------------------------
INSERT INTO tags (name, slug, description, usage_count) VALUES
    ('Драма',         'drama',         'Серьёзное повествование, сильные эмоции, конфликт характеров', 0),
    ('Комедия',       'comedy',        'Развлекательное кино, юмор, лёгкое настроение',                 0),
    ('Триллер',       'thriller',      'Напряжённый сюжет, саспенс, неожиданные повороты',              0),
    ('Ужасы',         'horror',        'Атмосфера страха, мрачные образы, паранормальное',              0),
    ('Боевик',        'action',        'Динамичные сцены, погони, перестрелки, экшен',                  0),
    ('Криминал',      'crime',         'Преступления, расследования, мафия, нуар',                      0),
    ('Мелодрама',     'romance',       'Любовные истории, отношения, чувства',                          0),
    ('Фантастика',    'sci-fi',        'Будущее, технологии, иные миры',                                0),
    ('Фэнтези',       'fantasy',       'Магия, мифические существа, альтернативные вселенные',          0),
    ('Детектив',      'detective',     'Расследование, дедукция, тайна',                                0),
    ('Приключения',   'adventure',     'Путешествия, поиски, опасности и открытия',                     0),
    ('Анимация',      'animation',     'Мультипликация, рисованное кино',                               0),
    ('Документальный','documentary',   'Реальные события, неигровое кино',                              0),
    ('Биография',     'biography',     'Истории реальных людей',                                        0),
    ('Военный',       'war',           'Военные конфликты, фронт, тыл',                                 0),
    ('Исторический',  'history',       'События прошлого, костюмные постановки',                        0),
    ('Семейный',      'family',        'Кино для всей семьи, без жёстких сцен',                         0),
    ('Музыкальный',   'musical',       'Музыка как часть повествования',                                0),
    ('Спорт',         'sport',         'Спортивные истории и достижения',                               0),
    ('Вестерн',       'western',       'Американский Запад, ковбои, фронтир',                           0)
ON CONFLICT (slug) DO NOTHING;

-- ---------------------------------------------------------------------
-- 2. USERS (8: 1 admin + 7 user). Пароль для всех: password123
-- bcrypt-хэш проверен; cost=10
-- ---------------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role, is_active) VALUES
    ('admin',          'admin@moviehub.ru',     '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'ADMIN', TRUE),
    ('cinephile',      'cinephile@example.com', '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('criticspeaks',   'critic@example.com',    '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('marathonner',    'marathon@example.com',  '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('weekendwatcher', 'weekend@example.com',   '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('arthouse_fan',   'arthouse@example.com',  '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('series_lover',   'series@example.com',    '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE),
    ('casual_viewer',  'casual@example.com',    '$2a$10$4L/vjGa3kNYR0LkLDPJWZ.pSaEKrIqBFcZK9F8S5LqZX9QwDIIiyq', 'USER',  TRUE)
ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------------------
-- 3. CONTENT — 25 фильмов + 15 сериалов = 40 единиц
-- Используем guard через WHERE NOT EXISTS чтобы не плодить дубли при повторном запуске.
-- Постеры — реальные TMDB CDN ссылки. Если вызывает сомнения hash — оставляем NULL.
-- ---------------------------------------------------------------------

-- =================== MOVIES (25) ===================

-- 1. Дюна: Часть вторая (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Дюна: Часть вторая', 'Dune: Part Two',
       'Пол Атрейдес объединяется с Чани и фрименами, ведя восстание против тех, кто уничтожил его семью. Перед героем встаёт выбор между любовью всей жизни и судьбой известной вселенной — и он пытается предотвратить ужасное будущее, которое видит лишь он.',
       2024, 'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg',
       8.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt15239678', '4664634'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt15239678');

INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 166, 190000000, 711844358 FROM content WHERE imdb_id = 'tt15239678'
ON CONFLICT (content_id) DO NOTHING;

-- 2. Оппенгеймер (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Оппенгеймер', 'Oppenheimer',
       'Биография американского физика-теоретика Роберта Оппенгеймера, который в годы Второй мировой войны возглавлял Манхэттенский проект — секретные ядерные разработки. Триумф учёного, изменивший мир, обернулся для него личной трагедией.',
       2023, 'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg',
       8.30, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt15398776', '5043148'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt15398776');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 180, 100000000, 975804000 FROM content WHERE imdb_id = 'tt15398776'
ON CONFLICT (content_id) DO NOTHING;

-- 3. Бедные-несчастные (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Бедные-несчастные', 'Poor Things',
       'Молодая женщина по имени Белла Бакстер возвращается к жизни благодаря эксцентричному учёному Годвину Бакстеру. Под защитой Годвина Белла жаждет познать мир, и однажды убегает с прожжённым адвокатом в кругосветное путешествие, освобождаясь от предрассудков своего времени.',
       2023, 'https://image.tmdb.org/t/p/w500/jrFUvANFgCLDMnNkZQUMgK11H7s.jpg',
       7.90, 'MOVIE', 'PUBLISHED', 'Великобритания', 'en', 'tt14230458', '4647932'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt14230458');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 141, 35000000, 117616017 FROM content WHERE imdb_id = 'tt14230458'
ON CONFLICT (content_id) DO NOTHING;

-- 4. Анора (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Анора', 'Anora',
       'Молодая танцовщица из Бруклина по имени Анора знакомится с сыном русского олигарха и решает выйти за него замуж. Сказка превращается в кошмар, когда родители парня узнают о свадьбе и отправляют людей расторгнуть брак.',
       2024, 'https://image.tmdb.org/t/p/w500/eDp6nSZ08xuvZBl15jvb7N6pVxO.jpg',
       7.60, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt28607951', '6479074'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt28607951');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 139, 6000000, 56795000 FROM content WHERE imdb_id = 'tt28607951'
ON CONFLICT (content_id) DO NOTHING;

-- 5. Зона интересов (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Зона интересов', 'The Zone of Interest',
       'Комендант Освенцима Рудольф Хёсс и его жена Хедвига строят идеальную жизнь для своей семьи рядом с лагерем. Уютный дом, ухоженный сад, дети — а за стеной творится самое страшное преступление в истории человечества.',
       2023, 'https://image.tmdb.org/t/p/w500/hUu9zyZmDd8VZegKi1iK1Vk0RYS.jpg',
       7.40, 'MOVIE', 'PUBLISHED', 'Великобритания', 'en', 'tt7160372', '4865412'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt7160372');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 105, 15000000, 51800000 FROM content WHERE imdb_id = 'tt7160372'
ON CONFLICT (content_id) DO NOTHING;

-- 6. Прошлые жизни (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Прошлые жизни', 'Past Lives',
       'Двое детей из Сеула были близкими друзьями, пока её семья не эмигрировала. Двадцать лет спустя они встречаются вновь в Нью-Йорке на одну судьбоносную неделю и осмысляют природу любви, судьбы и решений, формирующих жизнь.',
       2023, 'https://image.tmdb.org/t/p/w500/k3waqVXSnvCZWfJYNtdamTgTtTA.jpg',
       7.80, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt13238346', '4944066'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt13238346');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 105, 12000000, 41800000 FROM content WHERE imdb_id = 'tt13238346'
ON CONFLICT (content_id) DO NOTHING;

-- 7. Барби (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Барби', 'Barbie',
       'Барби и Кен наслаждаются жизнью в красочном идеальном мире Барбиленда. Однако когда у них появляется возможность отправиться в реальный мир, они быстро открывают для себя радости и опасности жизни среди людей.',
       2023, 'https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg',
       6.90, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt1517268', '1163646'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt1517268');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 114, 145000000, 1445638421 FROM content WHERE imdb_id = 'tt1517268'
ON CONFLICT (content_id) DO NOTHING;

-- 8. Падение империи (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Падение империи', 'Civil War',
       'В недалёком будущем США раздирает гражданская война. Группа военных журналистов отправляется в опасное путешествие через охваченную конфликтом страну, чтобы попасть в Вашингтон и взять интервью у президента до падения столицы.',
       2024, 'https://image.tmdb.org/t/p/w500/sh7Rg8Er3tFcN9BpKIPOMvALgZd.jpg',
       7.00, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt17279496', '5251072'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt17279496');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 109, 50000000, 127400000 FROM content WHERE imdb_id = 'tt17279496'
ON CONFLICT (content_id) DO NOTHING;

-- 9. Сталкер (1979)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Сталкер', 'Сталкер',
       'В неизвестной стране существует особая запретная Зона, охраняемая войсками. Где-то в её центре стоит Комната, в которой исполняются желания. Сталкер ведёт в Зону Писателя и Профессора — каждого со своими вопросами к самому себе.',
       1979, 'https://image.tmdb.org/t/p/w500/5HwH5e7llXdHKAMUUzpgB7DjiVx.jpg',
       8.10, 'MOVIE', 'PUBLISHED', 'СССР', 'ru', 'tt0079944', '43911'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0079944');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 162, 1000000, NULL FROM content WHERE imdb_id = 'tt0079944'
ON CONFLICT (content_id) DO NOTHING;

-- 10. Брат (1997)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Брат', 'Брат',
       'Демобилизованный Данила Багров возвращается в родной городок, но не находит там себе места. По совету матери он едет в Петербург, к старшему брату Виктору, и оказывается втянут в криминальный мир северной столицы.',
       1997, 'https://image.tmdb.org/t/p/w500/2v2zKuOCMHs3yPfi0fSzzwxV2VM.jpg',
       8.20, 'MOVIE', 'PUBLISHED', 'Россия', 'ru', 'tt0118767', '41519'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0118767');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 100, 100000, 1500000 FROM content WHERE imdb_id = 'tt0118767'
ON CONFLICT (content_id) DO NOTHING;

-- 11. Брат 2 (2000)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Брат 2', 'Брат 2',
       'Данила Багров вместе с фронтовым товарищем приезжает в Москву. Когда брата товарища убивают по заказу, Данила отправляется в Америку — в Чикаго, чтобы найти убийц и вернуть украденные у боевого друга деньги.',
       2000, 'https://image.tmdb.org/t/p/w500/yfh0vLxPUQbrjtKiMNFhSJlUlYk.jpg',
       8.00, 'MOVIE', 'PUBLISHED', 'Россия', 'ru', 'tt0231507', '41520'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0231507');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 127, 1500000, 1700000 FROM content WHERE imdb_id = 'tt0231507'
ON CONFLICT (content_id) DO NOTHING;

-- 12. Левиафан (2014)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Левиафан', 'Левиафан',
       'В маленьком приморском городке на Севере России живёт обычная семья. Жизнь Николая, его жены и сына круто меняется, когда местный мэр пытается отобрать у него дом, бизнес и землю предков. Не желая мириться с произволом, Николай вызывает на помощь старого армейского друга — теперь столичного адвоката.',
       2014, 'https://image.tmdb.org/t/p/w500/qIUKMlKqfVdTHrxghzNSvuLPLkY.jpg',
       7.60, 'MOVIE', 'PUBLISHED', 'Россия', 'ru', 'tt2802154', '720291'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2802154');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 140, 4500000, 5800000 FROM content WHERE imdb_id = 'tt2802154'
ON CONFLICT (content_id) DO NOTHING;

-- 13. Семь самураев (1954)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Семь самураев', '七人の侍',
       'XVI век, Япония. Деревенские крестьяне страдают от регулярных набегов разбойников. Отчаявшись, они нанимают семерых ронинов, которые должны защитить деревню от очередного нападения. Эпическая история мужества, чести и жертвы.',
       1954, 'https://image.tmdb.org/t/p/w500/8OKmBV5BUFzmozIC3pPWKHy17kx.jpg',
       8.60, 'MOVIE', 'PUBLISHED', 'Япония', 'ja', 'tt0047478', '443'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0047478');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 207, 500000, 271800 FROM content WHERE imdb_id = 'tt0047478'
ON CONFLICT (content_id) DO NOTHING;

-- 14. Крёстный отец (1972)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Крёстный отец', 'The Godfather',
       'Эпическая сага о клане итало-американской мафии Корлеоне. Дон Вито Корлеоне — могущественный глава семьи. Его младший сын Майкл, вернувшийся с войны героем, не желает иметь ничего общего с криминальным бизнесом. Однако судьба распорядится иначе.',
       1972, 'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg',
       9.20, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0068646', '325'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0068646');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 175, 6000000, 250342198 FROM content WHERE imdb_id = 'tt0068646'
ON CONFLICT (content_id) DO NOTHING;

-- 15. Побег из Шоушенка (1994)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Побег из Шоушенка', 'The Shawshank Redemption',
       'Бухгалтер Энди Дюфрейн обвинён в убийстве жены и её любовника и приговорён к пожизненному заключению в тюрьме Шоушенк. Здесь он встречает другого заключённого — Реда, и за двадцать лет дружбы Энди не теряет надежды на свободу.',
       1994, 'https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg',
       9.30, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0111161', '326'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0111161');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 142, 25000000, 28341469 FROM content WHERE imdb_id = 'tt0111161'
ON CONFLICT (content_id) DO NOTHING;

-- 16. Криминальное чтиво (1994)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Криминальное чтиво', 'Pulp Fiction',
       'Несколько криминальных историй, причудливо переплетённых между собой. Винсент и Джулс — киллеры, выполняющие задание мафиозного босса. Боксёр Бутч, которому заплатили за поражение, решает не сдаваться. И ещё одна пара — двое грабителей, решивших ограбить кафе.',
       1994, 'https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg',
       8.90, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0110912', '342'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0110912');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 154, 8000000, 213928762 FROM content WHERE imdb_id = 'tt0110912'
ON CONFLICT (content_id) DO NOTHING;

-- 17. Тёмный рыцарь (2008)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Тёмный рыцарь', 'The Dark Knight',
       'Бэтмен поднимает ставки в борьбе с криминальным миром Готэма. С помощью лейтенанта Гордона и нового окружного прокурора Харви Дента он начинает уничтожать организованную преступность. Но тут на сцене появляется Джокер — анархист, ввергающий город в хаос.',
       2008, 'https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg',
       9.00, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0468569', '111543'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0468569');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 152, 185000000, 1004558444 FROM content WHERE imdb_id = 'tt0468569'
ON CONFLICT (content_id) DO NOTHING;

-- 18. Властелин колец: Возвращение Короля (2003)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Властелин колец: Возвращение Короля', 'The Lord of the Rings: The Return of the King',
       'Войска Мордора уже у стен великого Минас-Тирита. Хоббит Фродо и его верный спутник Сэм продолжают опасный путь к Роковой горе, чтобы уничтожить Кольцо Всевластия. Это финальная битва за Средиземье.',
       2003, 'https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg',
       9.10, 'MOVIE', 'PUBLISHED', 'Новая Зеландия', 'en', 'tt0167260', '328'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0167260');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 201, 94000000, 1146030912 FROM content WHERE imdb_id = 'tt0167260'
ON CONFLICT (content_id) DO NOTHING;

-- 19. Интерстеллар (2014)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Интерстеллар', 'Interstellar',
       'Когда засуха, песчаные бури и вымирание сельскохозяйственных культур приводят человечество к продовольственному кризису, инженер и бывший пилот Купер вместе с группой исследователей отправляется через червоточину в путешествие, чтобы найти новую планету для людей.',
       2014, 'https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
       8.70, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0816692', '258687'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0816692');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 169, 165000000, 701729206 FROM content WHERE imdb_id = 'tt0816692'
ON CONFLICT (content_id) DO NOTHING;

-- 20. Зелёная миля (1999)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Зелёная миля', 'The Green Mile',
       'Пол Эджкомб — начальник блока смертников в тюрьме Холодная гора. Однажды к нему попадает Джон Коффи — гигант, обвинённый в убийстве двух девочек. Несмотря на ужасное преступление, в Коффи есть нечто странное и сверхъестественное.',
       1999, 'https://image.tmdb.org/t/p/w500/8VG8fDNiy50H7FedNHCjaCkyUgE.jpg',
       9.10, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0120689', '435'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0120689');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 189, 60000000, 286801374 FROM content WHERE imdb_id = 'tt0120689'
ON CONFLICT (content_id) DO NOTHING;

-- 21. Белое солнце пустыни (1970)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Белое солнце пустыни', 'Белое солнце пустыни',
       'Гражданская война подходит к концу. Красноармеец Фёдор Сухов, отслуживший своё, отправляется домой к любимой жене Катерине Матвеевне. По пути он встречает товарища Саида и попадает в гарем местного бандита Абдуллы.',
       1970, NULL, 8.40, 'MOVIE', 'PUBLISHED', 'СССР', 'ru', 'tt0066565', '46105'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0066565');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 84, 480000, NULL FROM content WHERE imdb_id = 'tt0066565'
ON CONFLICT (content_id) DO NOTHING;

-- 22. Иван Грозный (1944)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Иван Грозный', 'Иван Грозный',
       'Историческая киноэпопея Сергея Эйзенштейна о становлении первого русского царя. Венчание на царство, борьба с боярами, создание опричнины — путь Ивана IV к самодержавию показан с присущей режиссёру визуальной мощью.',
       1944, NULL, 7.50, 'MOVIE', 'PUBLISHED', 'СССР', 'ru', 'tt0036824', '46070'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0036824');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 99, 1000000, NULL FROM content WHERE imdb_id = 'tt0036824'
ON CONFLICT (content_id) DO NOTHING;

-- 23. Преступления будущего (2022)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Преступления будущего', 'Crimes of the Future',
       'В недалёком будущем человечество приспосабливается к синтетической окружающей среде. Тело подвергается новым трансформациям и мутациям. Вместе со своей партнёршей Каприс перформанс-художник Сол Тенсер демонстрирует метаморфозы своих органов в художественных представлениях.',
       2022, 'https://image.tmdb.org/t/p/w500/jcOl7XrgFbn7AB2bx6F2HQxlojQ.jpg',
       6.50, 'MOVIE', 'PUBLISHED', 'Канада', 'en', 'tt14549466', '1331009'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt14549466');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 107, 27000000, 4400000 FROM content WHERE imdb_id = 'tt14549466'
ON CONFLICT (content_id) DO NOTHING;

-- 24. Аноним (2024) — реальный фильм Anonymous (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Гражданин Кейн', 'Citizen Kane',
       'Журналисты пытаются разгадать смысл последнего слова, произнесённого умирающим медиамагнатом Чарльзом Фостером Кейном — «Розочка». В попытках понять Кейна они опрашивают его близких, восстанавливая историю взлёта и падения американской мечты.',
       1941, 'https://image.tmdb.org/t/p/w500/sav0jxhqiH0bPr2vZFU0Kjt2nZL.jpg',
       8.30, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0033467', '4180'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0033467');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 119, 839727, 1585634 FROM content WHERE imdb_id = 'tt0033467'
ON CONFLICT (content_id) DO NOTHING;

-- 25. Паразиты (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Паразиты', '기생충',
       'Бедная семья Кимов мечтает выбраться из подвала. Когда сын устраивается репетитором английского в дом богатой семьи Паков, он постепенно пристраивает туда и остальных родственников. Однако в роскошном особняке скрывается мрачная тайна.',
       2019, 'https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg',
       8.50, 'MOVIE', 'PUBLISHED', 'Южная Корея', 'ko', 'tt6751668', '1143242'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt6751668');
INSERT INTO movies (content_id, duration, budget, box_office)
SELECT id, 132, 11400000, 263076268 FROM content WHERE imdb_id = 'tt6751668'
ON CONFLICT (content_id) DO NOTHING;

-- =================== SERIES (15) ===================

-- 26. Сёгун (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Сёгун', 'Shōgun',
       'Япония, начало XVII века. Английский штурман Джон Блэкторн оказывается на берегах враждебной страны. Он попадает в эпицентр феодальной войны, где могущественный Ёсии Торанага сражается за титул сёгуна. Эпическая сага о чести, политике и столкновении цивилизаций.',
       2024, 'https://image.tmdb.org/t/p/w500/7O4iVfOMQmdCSxhOg1WnzG1AjYR.jpg',
       8.70, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt2788316', '1219417'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2788316');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 1, 10, FALSE FROM content WHERE imdb_id = 'tt2788316'
ON CONFLICT (content_id) DO NOTHING;

-- 27. Слово пацана. Кровь на асфальте (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Слово пацана. Кровь на асфальте', 'Слово пацана. Кровь на асфальте',
       'Казань, конец 80-х. Подростки из обычных семей объединяются в уличные группировки и делят город на «свои» и «чужие». Восьмиклассник Андрей попадает в банду «Универсам» и оказывается перед выбором: остаться «чушпаном» или стать «пацаном».',
       2023, 'https://image.tmdb.org/t/p/w500/yXkTdR9WTwI4ZaQXqBq9oYTlBdY.jpg',
       8.50, 'SERIES', 'PUBLISHED', 'Россия', 'ru', 'tt28015403', '5359302'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt28015403');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 1, 8, TRUE FROM content WHERE imdb_id = 'tt28015403'
ON CONFLICT (content_id) DO NOTHING;

-- 28. Эйфория (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Эйфория', 'Euphoria',
       'Группа подростков из небольшого американского городка пытается найти своё место в мире наркотиков, секса, насилия и социальных сетей. Семнадцатилетняя Ру Беннетт после реабилитации возвращается домой и встречает новенькую Джулс.',
       2019, 'https://image.tmdb.org/t/p/w500/3Q0hd3heuWwDWpwcDkhQOA6TYWI.jpg',
       8.30, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt8772296', '1284365'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt8772296');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 2, 18, FALSE FROM content WHERE imdb_id = 'tt8772296'
ON CONFLICT (content_id) DO NOTHING;

-- 29. Очень странные дела (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Очень странные дела', 'Stranger Things',
       'Маленький городок Хокинс, штат Индиана, 1980-е. После исчезновения мальчика Уилла его друзья — Майк, Лукас и Дастин — отправляются на поиски и встречают загадочную девочку с телекинетическими способностями. Так начинается история о тайных правительственных экспериментах и параллельном мире.',
       2016, 'https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg',
       8.60, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt4574334', '915196'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt4574334');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 4, 34, FALSE FROM content WHERE imdb_id = 'tt4574334'
ON CONFLICT (content_id) DO NOTHING;

-- 30. Игра престолов (2011)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Игра престолов', 'Game of Thrones',
       'В вымышленном мире Семи королевств девять знатных семей борются за контроль над мифической землёй Вестероса. Тем временем древнее зло пробуждается на Севере, а в изгнании последняя из династии Таргариенов готовится вернуть себе трон отца.',
       2011, 'https://image.tmdb.org/t/p/w500/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg',
       9.20, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0944947', '464963'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0944947');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 8, 73, TRUE FROM content WHERE imdb_id = 'tt0944947'
ON CONFLICT (content_id) DO NOTHING;

-- 31. Во все тяжкие (2008)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Во все тяжкие', 'Breaking Bad',
       'Школьный учитель химии Уолтер Уайт узнаёт, что болен раком лёгких в неоперабельной стадии. Чтобы обеспечить семью после своей смерти, он начинает производить и продавать метамфетамин в паре с бывшим учеником Джесси Пинкманом.',
       2008, 'https://image.tmdb.org/t/p/w500/3xnWaLQjelJDDF7LT1WBo6f4BRe.jpg',
       9.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0903747', '404900'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0903747');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 5, 62, TRUE FROM content WHERE imdb_id = 'tt0903747'
ON CONFLICT (content_id) DO NOTHING;

-- 32. Лучше звоните Солу (2015)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Лучше звоните Солу', 'Better Call Saul',
       'История о становлении адвоката Джимми МакГилла, который позже превратится в Сола Гудмана из «Во все тяжкие». Шесть лет до знакомства с Уолтером Уайтом, мелкие махинации, тяжёлые отношения с братом и поиск своего пути в безжалостном мире юриспруденции.',
       2015, 'https://image.tmdb.org/t/p/w500/fC2HDm5t0kHl7mTm7jxMR31s4uy.jpg',
       9.00, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt3032476', '767368'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt3032476');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 6, 63, TRUE FROM content WHERE imdb_id = 'tt3032476'
ON CONFLICT (content_id) DO NOTHING;

-- 33. Чернобыль (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Чернобыль', 'Chernobyl',
       'Драматическая реконструкция одной из крупнейших техногенных катастроф XX века — взрыва на Чернобыльской АЭС в 1986 году. Героические усилия пожарных, ликвидаторов и учёных, пытающихся предотвратить ещё большую трагедию, и трагические последствия для всей Восточной Европы.',
       2019, 'https://image.tmdb.org/t/p/w500/hlLXt2tOPT6RRnjiUmoxyG1LTFi.jpg',
       9.30, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt7366338', '1227803'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt7366338');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 1, 5, TRUE FROM content WHERE imdb_id = 'tt7366338'
ON CONFLICT (content_id) DO NOTHING;

-- 34. Корона (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Корона', 'The Crown',
       'Биографическая драма о правлении британской королевы Елизаветы II — от её свадьбы с принцем Филиппом в 1947 году и восшествия на престол до событий начала XXI века. Личная жизнь монарха, политические потрясения и роль монархии в современном мире.',
       2016, 'https://image.tmdb.org/t/p/w500/1M876KPjulVwppEpldhdc8V4o68.jpg',
       8.20, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt4786824', '865673'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt4786824');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 6, 60, TRUE FROM content WHERE imdb_id = 'tt4786824'
ON CONFLICT (content_id) DO NOTHING;

-- 35. Игра в кальмара (2021)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Игра в кальмара', '오징어 게임',
       'Сотни обанкротившихся участников из Южной Кореи получают приглашение в загадочную игру с огромным денежным призом. Простые детские игры превращаются в смертельные испытания, и каждый проигравший расплачивается жизнью.',
       2021, 'https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg',
       8.00, 'SERIES', 'PUBLISHED', 'Южная Корея', 'ko', 'tt10919420', '1342900'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt10919420');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 2, 16, FALSE FROM content WHERE imdb_id = 'tt10919420'
ON CONFLICT (content_id) DO NOTHING;

-- 36. Клан Сопрано (1999)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Клан Сопрано', 'The Sopranos',
       'Тони Сопрано — глава мафиозной семьи в Нью-Джерси, разрывающийся между двумя семьями: своей биологической и преступной. Под давлением проблем на обоих фронтах он начинает посещать психотерапевта, что становится первым шагом к раскрытию глубинных конфликтов современного гангстера.',
       1999, 'https://image.tmdb.org/t/p/w500/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg',
       9.10, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0141842', '254023'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0141842');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 6, 86, TRUE FROM content WHERE imdb_id = 'tt0141842'
ON CONFLICT (content_id) DO NOTHING;

-- 37. Декстер (2006)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Декстер', 'Dexter',
       'Декстер Морган — судебный эксперт-аналитик майамской полиции, специализирующийся на анализе следов крови. По ночам он ведёт двойную жизнь как серийный убийца, охотящийся на других серийных убийц по особому моральному кодексу.',
       2006, 'https://image.tmdb.org/t/p/w500/q8dWfc4JwQuv3HayIZeO84jAXED.jpg',
       8.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0773262', '195570'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0773262');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 8, 96, TRUE FROM content WHERE imdb_id = 'tt0773262'
ON CONFLICT (content_id) DO NOTHING;

-- 38. Друзья (1994)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Друзья', 'Friends',
       'Шесть молодых друзей живут по соседству в Нью-Йорке. Их повседневная жизнь — работа, любовь, семья, безработица, новые отношения — пронизана юмором и теплом настоящей дружбы. Десять сезонов, ставших символом эпохи.',
       1994, 'https://image.tmdb.org/t/p/w500/f496cm9enuEsZkSPzCwnTESEK5s.jpg',
       8.90, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0108778', '77044'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0108778');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 10, 236, TRUE FROM content WHERE imdb_id = 'tt0108778'
ON CONFLICT (content_id) DO NOTHING;

-- 39. Шерлок (2010)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Шерлок', 'Sherlock',
       'Современная адаптация классических рассказов Артура Конан Дойла. Гениальный сыщик-консультант Шерлок Холмс и его друг доктор Джон Уотсон расследуют самые запутанные преступления в Лондоне XXI века.',
       2010, 'https://image.tmdb.org/t/p/w500/7WTsnHkbA0FaG6R9twfFde0I9hl.jpg',
       9.10, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt1475582', '420923'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt1475582');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 4, 13, FALSE FROM content WHERE imdb_id = 'tt1475582'
ON CONFLICT (content_id) DO NOTHING;

-- 40. Мир Дикого Запада (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Мир Дикого Запада', 'Westworld',
       'В футуристическом тематическом парке развлечений «Мир Дикого Запада» гости могут жить в сеттинге Дикого Запада среди реалистичных андроидов-«хостов». Однажды искусственные создания начинают проявлять признаки сознания и бунтовать против своих создателей.',
       2016, 'https://image.tmdb.org/t/p/w500/4Ec0q5sKvCw8KE3sfsNxNbvcS5e.jpg',
       8.20, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0475784', '189337'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0475784');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished)
SELECT id, 4, 36, TRUE FROM content WHERE imdb_id = 'tt0475784'
ON CONFLICT (content_id) DO NOTHING;

-- ---------------------------------------------------------------------
-- 4. CONTENT_TAGS — связки content + tag (2-4 тега на контент)
-- Используем подзапросы по slug/imdb_id чтобы не зависеть от конкретных ID.
-- ---------------------------------------------------------------------

-- helper macro inline: вставляем (content.imdb_id, tag.slug) парами
-- Дюна: фантастика, приключения, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt15239678' AND t.slug IN ('sci-fi','adventure','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Оппенгеймер: биография, драма, исторический
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt15398776' AND t.slug IN ('biography','drama','history')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Бедные-несчастные: драма, фантастика, комедия
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt14230458' AND t.slug IN ('drama','sci-fi','comedy')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Анора: драма, мелодрама, комедия
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt28607951' AND t.slug IN ('drama','romance','comedy')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Зона интересов: драма, военный, исторический
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt7160372' AND t.slug IN ('drama','war','history')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Прошлые жизни: драма, мелодрама
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt13238346' AND t.slug IN ('drama','romance')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Барби: комедия, фэнтези, приключения
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt1517268' AND t.slug IN ('comedy','fantasy','adventure')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Падение империи: боевик, драма, триллер
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt17279496' AND t.slug IN ('action','drama','thriller')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Сталкер: фантастика, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0079944' AND t.slug IN ('sci-fi','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Брат: криминал, драма, боевик
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0118767' AND t.slug IN ('crime','drama','action')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Брат 2: криминал, боевик, приключения
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0231507' AND t.slug IN ('crime','action','adventure')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Левиафан: драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt2802154' AND t.slug IN ('drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Семь самураев: приключения, драма, боевик, исторический
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0047478' AND t.slug IN ('adventure','drama','action','history')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Крёстный отец: криминал, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0068646' AND t.slug IN ('crime','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Побег из Шоушенка: драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0111161' AND t.slug IN ('drama','crime')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Криминальное чтиво: криминал, драма, триллер
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0110912' AND t.slug IN ('crime','drama','thriller')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Тёмный рыцарь: боевик, криминал, драма, триллер
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0468569' AND t.slug IN ('action','crime','drama','thriller')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Властелин колец: фэнтези, приключения, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0167260' AND t.slug IN ('fantasy','adventure','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Интерстеллар: фантастика, драма, приключения
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0816692' AND t.slug IN ('sci-fi','drama','adventure')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Зелёная миля: драма, фэнтези
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0120689' AND t.slug IN ('drama','fantasy')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Белое солнце пустыни: приключения, боевик, вестерн
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0066565' AND t.slug IN ('adventure','action','western')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Иван Грозный: драма, исторический, биография
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0036824' AND t.slug IN ('drama','history','biography')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Преступления будущего: фантастика, триллер, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt14549466' AND t.slug IN ('sci-fi','thriller','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Гражданин Кейн: драма, мистика
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0033467' AND t.slug IN ('drama','detective')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Паразиты: триллер, драма, комедия
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt6751668' AND t.slug IN ('thriller','drama','comedy')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Сёгун: драма, исторический, приключения, боевик
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt2788316' AND t.slug IN ('drama','history','adventure','action')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Слово пацана: драма, криминал
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt28015403' AND t.slug IN ('drama','crime')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Эйфория: драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt8772296' AND t.slug IN ('drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Очень странные дела: фантастика, ужасы, драма, приключения
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt4574334' AND t.slug IN ('sci-fi','horror','drama','adventure')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Игра престолов: фэнтези, драма, приключения
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0944947' AND t.slug IN ('fantasy','drama','adventure')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Во все тяжкие: криминал, драма, триллер
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0903747' AND t.slug IN ('crime','drama','thriller')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Лучше звоните Солу: криминал, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt3032476' AND t.slug IN ('crime','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Чернобыль: драма, исторический, документальный
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt7366338' AND t.slug IN ('drama','history','documentary')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Корона: драма, биография, исторический
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt4786824' AND t.slug IN ('drama','biography','history')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Игра в кальмара: триллер, драма, боевик
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt10919420' AND t.slug IN ('thriller','drama','action')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Сопрано: криминал, драма
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0141842' AND t.slug IN ('crime','drama')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Декстер: криминал, драма, триллер, детектив
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0773262' AND t.slug IN ('crime','drama','thriller','detective')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Друзья: комедия, мелодрама
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0108778' AND t.slug IN ('comedy','romance')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Шерлок: детектив, драма, триллер, криминал
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt1475582' AND t.slug IN ('detective','drama','thriller','crime')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Мир Дикого Запада: фантастика, драма, вестерн, триллер
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id FROM content c, tags t
WHERE c.imdb_id = 'tt0475784' AND t.slug IN ('sci-fi','drama','western','thriller')
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Пересчитываем usage_count тегов
UPDATE tags SET usage_count = sub.cnt
FROM (SELECT tag_id, COUNT(*) AS cnt FROM content_tags GROUP BY tag_id) sub
WHERE tags.id = sub.tag_id;

-- ---------------------------------------------------------------------
-- 5. RATINGS — оценки от 7 не-admin юзеров (~150 штук)
-- Распределение задано осознанно для работоспособности рекомендаций.
-- ---------------------------------------------------------------------

-- cinephile (id 2) — арт-хаус и качественное кино
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='cinephile'), c.id, v.val
FROM (VALUES
    ('tt15239678',5), ('tt15398776',5), ('tt14230458',5), ('tt28607951',4),
    ('tt7160372',5),  ('tt13238346',5),  ('tt0079944',5), ('tt0118767',5),
    ('tt2802154',5),   ('tt0047478',5),  ('tt0068646',5), ('tt0111161',5),
    ('tt0110912',5),  ('tt0816692',5),   ('tt6751668',5), ('tt2788316',5),
    ('tt7366338',5),  ('tt0033467',5),   ('tt0944947',4),  ('tt0903747',5),
    ('tt3032476',5),   ('tt0141842',5),  ('tt28015403',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- criticspeaks (id 3) — критик, ставит широкий диапазон
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='criticspeaks'), c.id, v.val
FROM (VALUES
    ('tt15239678',4), ('tt15398776',5), ('tt14230458',4), ('tt28607951',5),
    ('tt7160372',4),  ('tt13238346',4), ('tt1517268',3), ('tt17279496',4),
    ('tt0079944',5),  ('tt0118767',4),  ('tt0231507',4), ('tt2802154',4),
    ('tt0068646',5), ('tt0111161',5), ('tt0110912',5), ('tt0468569',5),
    ('tt0167260',5),  ('tt0816692',4),  ('tt6751668',5), ('tt2788316',5),
    ('tt0903747',5), ('tt7366338',5), ('tt1475582',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- marathonner (id 4) — сериаломан
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='marathonner'), c.id, v.val
FROM (VALUES
    ('tt2788316',5), ('tt28015403',5), ('tt8772296',4),  ('tt4574334',5),
    ('tt0944947',5), ('tt0903747',5), ('tt3032476',5),  ('tt7366338',5),
    ('tt4786824',4),  ('tt10919420',5), ('tt0141842',5),  ('tt0773262',4),
    ('tt0108778',5),  ('tt1475582',5), ('tt0475784',4),
    -- немного фильмов
    ('tt0468569',5), ('tt0816692',4), ('tt15239678',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- weekendwatcher (id 5) — мейнстрим
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='weekendwatcher'), c.id, v.val
FROM (VALUES
    ('tt15239678',5), ('tt1517268',4), ('tt17279496',4), ('tt0468569',5),
    ('tt0167260',5), ('tt0816692',5),('tt0120689',5), ('tt0118767',4),
    ('tt0231507',5),  ('tt0066565',5), ('tt0944947',4), ('tt0903747',4),
    ('tt4574334',4),  ('tt10919420',4),('tt0108778',5), ('tt2788316',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- arthouse_fan (id 6) — фестивальное, артхаус. Низко ставит мейнстрим.
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='arthouse_fan'), c.id, v.val
FROM (VALUES
    ('tt15398776',5), ('tt14230458',5), ('tt7160372',5), ('tt13238346',5),
    ('tt0079944',5),  ('tt2802154',5),  ('tt0047478',5), ('tt0036824',5),
    ('tt14549466',5),  ('tt0033467',5),  ('tt6751668',5), ('tt28607951',5),
    -- мейнстрим — низко
    ('tt1517268',3),   ('tt17279496',3),  ('tt0468569',4),  ('tt15239678',3),
    ('tt0108778',3),   ('tt10919420',3)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- series_lover (id 7) — только сериалы
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='series_lover'), c.id, v.val
FROM (VALUES
    ('tt2788316',5), ('tt28015403',5), ('tt8772296',5),  ('tt4574334',5),
    ('tt0944947',5), ('tt0903747',5),  ('tt3032476',5), ('tt7366338',5),
    ('tt4786824',5),  ('tt10919420',4),  ('tt0141842',5), ('tt0773262',5),
    ('tt0108778',4),  ('tt1475582',5),  ('tt0475784',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- casual_viewer (id 8) — гость-эпизодник, мало оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='casual_viewer'), c.id, v.val
FROM (VALUES
    ('tt15239678',4), ('tt0468569',5), ('tt1517268',4), ('tt0816692',5),
    ('tt28015403',4), ('tt0944947',4), ('tt0108778',4), ('tt6751668',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- Пересчёт average_rating контента (по реальным оценкам, поверх стартовых).
-- Колонка average_rating имеет precision=3 scale=2 (max 9.99), поэтому LEAST(...,9.99)
UPDATE content c SET average_rating = LEAST(ROUND(sub.avg_v::numeric, 2), 9.99::numeric)
FROM (SELECT content_id, AVG("value")::numeric AS avg_v FROM ratings GROUP BY content_id) sub
WHERE c.id = sub.content_id;

-- ---------------------------------------------------------------------
-- 6. REVIEWS — ~30 опубликованных рецензий
-- ---------------------------------------------------------------------

INSERT INTO reviews (user_id, content_id, title, text, rating_value, status, view_count, like_count)
SELECT u.id, c.id, v.title, v.body, v.rv, 'PUBLISHED', v.vc, v.lc
FROM (VALUES
    ('cinephile',     'tt15398776', 'Атомный реквием по гению',
     'Нолан отказался от привычной экшен-структуры и снял трёхчасовой биографический триллер, в котором главное оружие — диалог. Киллиан Мерфи играет Оппенгеймера как человека, проглотившего собственное изобретение и не способного его переварить. Чёрно-белые сцены допроса работают как ритм-секция: с каждой минутой петля затягивается всё туже. Это кино, которое заставляет понять, что такое моральный долг, не назидая ни секунды.', 5, 4820, 198),
    ('criticspeaks',  'tt15239678', 'Эпос пустыни без капли воды',
     'Вильнёв снял редкий случай продолжения, которое масштабнее и одновременно интимнее первой части. Песчаные планы здесь — не фон, а полноценный персонаж, давящий на героев. Сцена приручения червя — лучшая визуальная метафора последних лет о том, как покорный объект становится политической силой. Минус один балл за финальную трагическую линию Чани, которой явно не хватило экранного времени.', 5, 6210, 287),
    ('arthouse_fan',  'tt7160372', 'Звук как обвинение',
     'Глейзер сделал невозможное: снял фильм об Освенциме, в котором почти не показан Освенцим. Камера не покидает пределов комендантской дачи — а ужас просачивается через звуковую дорожку и мимоходом брошенные фразы. Это самый радикальный фильм о банальности зла со времён Ханны Арендт. Оставляет в полной тишине.', 5, 3140, 142),
    ('cinephile',     'tt14230458', 'Франкенштейн в кружевах',
     'Лантимос в очередной раз ставит эксперимент над зрителем — на этот раз с эстетикой викторианского стимпанка. Эмма Стоун проживает на экране все стадии взросления женщины за 140 минут и заслуженно получает Оскар. Камера с рыбьим глазом и пастельные тона создают мир, в котором нормальность перестаёт быть точкой отсчёта.', 5, 4520, 176),
    ('criticspeaks',  'tt28607951', 'Сказка с пинком под зад',
     'Шон Бэйкер снял сказку о Золушке, которая никогда не закончится. Майки Мэдисон взрывает экран — её Анора одновременно жёсткая, беззащитная, смешная и трагичная. Финальная сцена в машине стоит всех голливудских мелодрам последнего десятилетия. Каннская «Золотая пальмовая ветвь» здесь абсолютно по делу.', 5, 3890, 154),
    ('arthouse_fan',  'tt0079944', 'Зона как зеркало',
     'Фильм-молитва, фильм-загадка, фильм-проклятие. Тарковский оставил после себя визуальный текст, расшифровывать который будут ещё лет сто. Камера здесь движется с такой медлительностью, что это становится отдельной формой искусства. Хочется не смотреть, а вдыхать каждый кадр. Лучшее, что когда-либо появлялось в советской фантастике.', 5, 5210, 312),
    ('cinephile',     'tt0118767', 'Поколение, которое нас сделало',
     'Балабанов угадал нерв эпохи и снял фильм-портрет постсоветского человека, который не знает, что ему делать со свободой. Бодров играет Данилу с такой обескураживающей простотой, что начинаешь верить — этот парень действительно живёт по соседству. Музыка Бутусова стала гимном для двух поколений. Этот фильм просто нельзя обсуждать без эмоций.', 5, 7820, 423),
    ('weekendwatcher','tt0468569', 'Лучший комикс в истории',
     'Хит Леджер создал персонажа, который стал культурным феноменом до того, как фильм вышел в прокат. Нолан превратил блокбастер в криминальную драму — и это сработало. Сцена ограбления банка в начале до сих пор лучший пролог в супергеройском кино. Тёмный, умный, безжалостный.', 5, 9320, 521),
    ('marathonner',   'tt0944947', 'Сезонов восемь — претензий девять',
     'Первые шесть сезонов — это эпохальное телевидение, которое навсегда изменило индустрию. Финал, конечно, спорный, но прежде чем ругать — пересмотрите ту самую сцену в Тронном зале. Шоу с такой плотностью персонажей и интриг было до этого только у Шекспира. Минусую балл за драконицу, разбившую сердце фанатам.', 5, 11200, 612),
    ('series_lover',  'tt0903747', 'Превращение, после которого не вернуться',
     'Брайан Крэнстон сыграл лучшую роль на телевидении за всё время его существования. Винс Гиллиган выстроил пятисезонную драматургию с математической точностью — каждое ружьё стреляет, каждая деталь работает. Это шоу не о наркотиках, а о том, как человек теряет себя по миллиметру. Идеально.', 5, 14500, 789),
    ('cinephile',     'tt0068646', 'Образец жанра, эталон ремесла',
     'Коппола снял не фильм — мемориал американскому кинематографу. Каждый план — учебник по композиции, каждая сцена — мастер-класс по актёрской игре. Брандо в первые двадцать минут гипнотизирует одной интонацией. После «Крёстного отца» гангстерское кино перестало быть жанром и стало традицией.', 5, 8910, 456),
    ('criticspeaks',  'tt0111161', 'Надежда — хорошая штука',
     'Дарабонт превратил рассказ Кинга в гимн человеческому достоинству. Финал, разумеется, известен всем — но удивительно, как фильм продолжает работать даже на пятом просмотре. Морган Фриман и Тим Роббинс играют дружбу так, будто действительно знакомы двадцать лет. Это кино, которое не устаревает.', 5, 12300, 671),
    ('arthouse_fan',  'tt6751668', 'Этажи как метафора',
     'Пон Чжун Хо снял идеальный фильм о классовом неравенстве, не сказав ни одного дидактического слова. Архитектура здесь — главный сценарист: лестницы, подвалы, окна работают как социальный диагноз. Финальная двадцатиминутка по плотности драматургии не имеет аналогов в современном кино.', 5, 6800, 342),
    ('cinephile',     'tt0110912', 'Не сюжет — структура',
     'Тарантино перевернул нарратив с ног на голову и оказалось, что так смотреть кино только интереснее. Диалоги стали отдельной формой искусства. Сцена в кафе с разговором о бутербродах вошла во все учебники сценарного мастерства. Тридцать лет спустя — всё ещё свежо.', 5, 7100, 389),
    ('marathonner',   'tt7366338', 'Документ, который должен был быть снят',
     'Мейзин не сделал ни одной уступки зрителю. Сцена с горящим реактором отрезает все пути к комфорту — после неё досматривать уже невозможно остановиться. Джаред Харрис в роли Легасова — это тот случай, когда актёр становится памятником. Пять серий, которые меняют отношение к ответственности.', 5, 9800, 489),
    ('series_lover',  'tt2788316', 'Япония без клюквы',
     'FX и Disney+ собрали настоящих японских актёров, перевели сценарий обратно на японский и сняли историческую драму уровня «Семи самураев». Хироюки Санада в роли Торанаги — отдельный вид искусства. Сцены чайной церемонии напряжённее голливудских погонь. Лучший дебютный сезон со времён первого «Тру Детектива».', 5, 5400, 261),
    ('marathonner',   'tt28015403', 'Казань, которую мы забыли',
     'Крыжовников снял жестокую и честную хронику девяностых, не заигрывая с ностальгией. Подростки здесь — не герои и не жертвы, а продукты системы, которой больше не существует. Финал бьёт под дых. Лучший российский сериал последних пяти лет.', 5, 8200, 412),
    ('weekendwatcher','tt0167260', 'Финал, ради которого стоило ждать',
     'Питер Джексон закрыл трилогию так, что все претензии к двум первым частям моментально снимаются. Битва на Пеленнорских полях до сих пор остаётся эталоном масштабной баталии в кино. Возвращение Арагорна и финал в Серых Гаванях — момент, ради которого хочется пересматривать всё с самого начала.', 5, 6500, 298),
    ('weekendwatcher','tt0816692', 'Космос как метафора любви',
     'Нолан в очередной раз доказал, что блокбастер может быть умным. Часовая стрелка над Гаргантюа — лучшая визуализация теории относительности в художественном кино. Музыка Циммера превращает каждую сцену в литургию. Минус полбалла за чёрную дыру, которая ведёт к книжному шкафу — но это придирка.', 5, 8400, 421),
    ('series_lover',  'tt3032476', 'Спин-офф, превзошедший оригинал',
     'Винс Гиллиган и Питер Гулд сняли историю о том, как добрый человек медленно превращается в свою противоположность. Боб Оденкёрк играет Джимми МакГилла настолько живо, что воспринимаешь его как старого знакомого. Чёрно-белые финальные эпизоды — это уровень, на который большинство сериалов не поднимутся никогда.', 5, 5900, 287),
    ('cinephile',     'tt0047478', 'Эталон, переписавший правила',
     'Куросава придумал структуру, которую с тех пор повторяет всё мировое кино. От «Великолепной семёрки» до «Стражей Галактики» — все они дети «Семи самураев». 207 минут, в которых нет ни секунды лишнего хронометража. Кино, после которого хочется выучить японский.', 5, 4100, 218),
    ('criticspeaks',  'tt2802154', 'Современное прочтение Книги Иова',
     'Звягинцев снял библейский сюжет в декорациях постсоветской провинции и попал в нерв страны. Алексей Серебряков играет своего Николая как человека, у которого государство отнимает не дом, а смысл. Жесткое, неудобное, обязательное к просмотру кино.', 5, 4700, 234),
    ('series_lover',  'tt0773262', 'Серийный убийца как обаятельный сосед',
     'Майкл С. Холл сыграл такого Декстера, которому начинаешь сочувствовать против собственной воли. Сценаристы первых четырёх сезонов выстроили моральную игру с зрителем на грани этики. Финал — отдельная боль, но первые сезоны искупают всё.', 5, 4300, 187),
    ('weekendwatcher','tt1517268', 'Розовое стекло, через которое всё видно',
     'Грета Гервиг сняла фильм по игрушке так, будто это был серьёзный ирландский артхаус. Маргот Робби играет Барби с такой самоиронией, что не верится, что это блокбастер. Монолог Глории о женских противоречиях — лучший момент массового кино за год. Не идеально, но смело.', 4, 11200, 567),
    ('arthouse_fan',  'tt13238346', 'Тихая мелодрама о невозможности',
     'Селин Сонг сняла дебютный фильм с тактом и зрелостью, которые приходят к режиссёрам через десятилетия. Грета Ли играет женщину, которая выбрала жизнь, и не жалеет об этом — но и не уверена. Финал на пирсе — три минуты, после которых неделю не можешь говорить.', 5, 3700, 178),
    ('cinephile',     'tt0033467', 'Кино, которое изобрело язык',
     'Уэллс в 25 лет переписал учебник кинематографа. Глубинная мизансцена, низкие точки съёмки, флешбэки внутри флешбэков — всё, что мы считаем нормой, придумано здесь. «Розочка» как символ невозвратной невинности — гениально и до сих пор не превзойдено.', 5, 2900, 145),
    ('criticspeaks',  'tt1475582', 'Холмс, который стал нашим современником',
     'Бенедикт Камбербэтч и Мартин Фримен создали химию, на которой держится весь сериал. Стивен Моффат и Марк Гэтисс смогли пересказать классические сюжеты Дойля так, что они зазвучали свежо. Минусую за провисший четвёртый сезон — но первые три безусловно входят в золотой фонд.', 5, 6800, 312),
    ('marathonner',   'tt0141842', 'Шоу, после которого появилась эпоха',
     'Дэвид Чейз создал прототип всего современного телевидения. Тони Сопрано в исполнении Джеймса Гандольфини — гангстер у психотерапевта, который кормит уток в бассейне и плачет. Семья как метафора, метафора как способ говорить о смерти американской мечты. Эталон.', 5, 7300, 389),
    ('arthouse_fan',  'tt14549466', 'Кроненберг возвращается к телу',
     'Канадский мастер боди-хоррора не теряет хватки. Вигго Мортенсен играет художника, который выращивает в себе новые органы — и это метафора нашего времени с пугающей точностью. Не для всех, но для тех, кто примет правила игры — невероятный опыт.', 4, 1800, 87),
    ('weekendwatcher','tt0231507', 'Продолжение, которое стало эпохой',
     'Балабанов снял свой первый блокбастер и попал в больной нерв сразу нескольких поколений. Песня «В Питере — пить» здесь и появилась бы наверняка, если бы Шнур её ещё не написал. Сцена на мосту в Чикаго — лучший момент русского кино двухтысячных.', 5, 9100, 478)
) AS v(uname,imdb,title,body,rv,vc,lc)
JOIN users u ON u.username = v.uname
JOIN content c ON c.imdb_id = v.imdb
WHERE NOT EXISTS (
    SELECT 1 FROM reviews r WHERE r.user_id = u.id AND r.content_id = c.id AND r.title = v.title
);

-- ---------------------------------------------------------------------
-- 7. COMMENTS — 50 коротких комментариев
-- ---------------------------------------------------------------------

INSERT INTO comments (user_id, content_id, text, is_edited)
SELECT u.id, c.id, v.text, FALSE
FROM (VALUES
    ('weekendwatcher','tt15239678','Песчаный червь — самое впечатляющее существо в кино за 10 лет.'),
    ('marathonner',   'tt15239678','Тимоти Шаламе наконец-то перестал казаться слишком юным для роли мессии.'),
    ('casual_viewer', 'tt15239678','Смотрел в IMAX — ушёл в эйфории.'),
    ('cinephile',     'tt15398776','Мерфи vs Дауни — лучший актёрский поединок года.'),
    ('weekendwatcher','tt15398776','Три часа пролетели как один.'),
    ('arthouse_fan',  'tt14230458','Эстетика безупречна, философия местами наивна — но как красиво.'),
    ('cinephile',     'tt28607951','Майки Мэдисон — открытие года.'),
    ('criticspeaks',  'tt7160372','Звуковая дорожка должна была получить Оскар отдельно.'),
    ('arthouse_fan',  'tt7160372','После просмотра неделю не мог говорить.'),
    ('series_lover',  'tt2788316','Японский без перевода — отдельное удовольствие.'),
    ('marathonner',   'tt2788316','Хироюки Санада — национальное достояние.'),
    ('weekendwatcher','tt2788316','Чайная сцена напряжённее, чем большинство экшенов.'),
    ('marathonner',   'tt28015403','Атмосфера 90-х передана идеально.'),
    ('series_lover',  'tt28015403','Финал по-настоящему трагичен.'),
    ('cinephile',     'tt0079944','Тарковский — это не режиссёр, это диагноз.'),
    ('weekendwatcher','tt0118767','Бодров навсегда в наших сердцах.'),
    ('weekendwatcher','tt0231507','«В чём сила, брат?» — фраза-эпоха.'),
    ('cinephile',     'tt2802154','Звягинцев — наше всё в современном русском кино.'),
    ('arthouse_fan',  'tt0047478','Куросава учит ремеслу одним кадром.'),
    ('cinephile',     'tt0068646','Пересматривал десять раз — и каждый раз новые детали.'),
    ('weekendwatcher','tt0111161','Лучший фильм всех времён, без вопросов.'),
    ('criticspeaks',  'tt0110912','Структура нарратива — гениальна.'),
    ('weekendwatcher','tt0468569','Хит Леджер — Джокер на все времена.'),
    ('weekendwatcher','tt0167260','«Возвращение Короля» — финал, ради которого живёшь.'),
    ('marathonner',   'tt0816692','Музыка Циммера — отдельный шедевр.'),
    ('weekendwatcher','tt0120689','Том Хэнкс — мастер тонкой драмы.'),
    ('marathonner',   'tt0944947','Первые шесть сезонов простили бы любой финал.'),
    ('marathonner',   'tt0944947','Тирион Ланнистер — лучший персонаж эпохи.'),
    ('series_lover',  'tt0903747','Брайан Крэнстон — потолок актёрского мастерства.'),
    ('series_lover',  'tt0903747','Hank — ты оказался не таким простым, как казался.'),
    ('series_lover',  'tt3032476','Боб Оденкёрк наконец получил роль своей жизни.'),
    ('marathonner',   'tt7366338','«What is the cost of lies?» — каждый раз мурашки.'),
    ('cinephile',     'tt7366338','Сцена с пожарными — самая страшная за весь сериал.'),
    ('marathonner',   'tt4786824','Клэр Фой и Оливия Колман — обе блестящие королевы.'),
    ('series_lover',  'tt10919420','Первый сезон шедевр, второй — увы, слабее.'),
    ('series_lover',  'tt0141842','Тони Сопрано задал шаблон антигероя.'),
    ('series_lover',  'tt0773262','Главное — не досматривать после 4 сезона.'),
    ('weekendwatcher','tt0108778','Десять лет смеха и сейчас работает.'),
    ('marathonner',   'tt1475582','Камбербэтч — идеальный современный Холмс.'),
    ('series_lover',  'tt0475784','Энтони Хопкинс задал планку.'),
    ('arthouse_fan',  'tt6751668','«Паразиты» — учебник классового кино.'),
    ('cinephile',     'tt6751668','Пон Чжун Хо — главный режиссёр десятилетия.'),
    ('weekendwatcher','tt8772296','Зендея заслужила Эмми справедливо.'),
    ('marathonner',   'tt4574334','Третий сезон — лучший в сериале.'),
    ('cinephile',     'tt13238346','Финальная сцена в такси — три минуты гения.'),
    ('weekendwatcher','tt1517268','Райан Гослинг украл фильм у Марго Робби.'),
    ('arthouse_fan',  'tt0033467','Уэллс изобрёл всё, чем пользуется современное кино.'),
    ('arthouse_fan',  'tt14549466','Кроненберг не теряет фирменного стиля.'),
    ('weekendwatcher','tt17279496','Жуткий и тревожно правдоподобный.'),
    ('cinephile',     'tt0066565','«Восток — дело тонкое, Петруха.»')
) AS v(uname,imdb,text)
JOIN users u ON u.username = v.uname
JOIN content c ON c.imdb_id = v.imdb
WHERE NOT EXISTS (
    SELECT 1 FROM comments cm WHERE cm.user_id = u.id AND cm.content_id = c.id AND cm.text = v.text
);

-- ---------------------------------------------------------------------
-- 8. PLAYLISTS (12 тематических подборок)
-- ---------------------------------------------------------------------

INSERT INTO playlists (user_id, title, description, cover_image_url, is_public)
SELECT u.id, v.title, v.descr, NULL, TRUE
FROM (VALUES
    ('cinephile',     'Кино, после которого хочется молчать',
     'Подборка фильмов, которые оставляют в полной тишине. Серьёзный артхаус для долгих вечеров.'),
    ('marathonner',   'Лучшие сериалы 2020-х',
     'Самое значимое в сериальной индустрии последних лет — от «Сёгуна» до «Слова пацана».'),
    ('cinephile',     'Русское кино: золотая коллекция',
     'Знаковые отечественные фильмы — от советской классики до современной авторской волны.'),
    ('arthouse_fan',  'Авторская японская',
     'Кино из Страны восходящего солнца — от Куросавы до современных открытий.'),
    ('series_lover',  'Долгие вечера с тёмным детективом',
     'Сериалы и фильмы, в которых правит атмосфера саспенса и сложных моральных дилемм.'),
    ('criticspeaks',  'Главные премьеры 2024 года',
     'То, что обсуждали все. Премии, обсуждения, обложки журналов — всё здесь.'),
    ('weekendwatcher','Блокбастеры на выходные',
     'Большое кино для большой компании. Громкое, зрелищное, любимое.'),
    ('arthouse_fan',  'Каннские лауреаты',
     'Лучшее с главного фестиваля мира. Победители Золотых пальмовых ветвей и фавориты критики.'),
    ('cinephile',     'Криминальные саги',
     'От «Крёстного отца» до «Лучше звоните Солу» — кино и сериалы о тёмной стороне.'),
    ('marathonner',   'Сериалы на одну ночь',
     'Короткие мини-сериалы, которые можно посмотреть за вечер или выходные.'),
    ('series_lover',  'Истории становления',
     'Фильмы и шоу о том, как обычные люди превращаются в кого-то совсем другого.'),
    ('admin',         'Главная редакция MovieHub: must-watch',
     'Подборка от редакции — то, что должен посмотреть каждый.')
) AS v(uname, title, descr)
JOIN users u ON u.username = v.uname
WHERE NOT EXISTS (SELECT 1 FROM playlists p WHERE p.title = v.title);

-- ---------------------------------------------------------------------
-- 9. PLAYLIST_CONTENT — наполняем подборки
-- ---------------------------------------------------------------------

-- «Кино, после которого хочется молчать»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt7160372',1),('tt13238346',2),('tt14230458',3),('tt2802154',4),('tt6751668',5),('tt0079944',6),('tt7366338',7))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Кино, после которого хочется молчать'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Лучшие сериалы 2020-х»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt2788316',1),('tt28015403',2),('tt8772296',3),('tt7366338',4),('tt10919420',5),('tt3032476',6))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Лучшие сериалы 2020-х'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Русское кино: золотая коллекция»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0118767',1),('tt0231507',2),('tt0079944',3),('tt0066565',4),('tt2802154',5),('tt28015403',6),('tt0036824',7))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Русское кино: золотая коллекция'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Авторская японская»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0047478',1),('tt2788316',2))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Авторская японская'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Долгие вечера с тёмным детективом»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt1475582',1),('tt3032476',2),('tt0773262',3),('tt0903747',4),('tt0141842',5))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Долгие вечера с тёмным детективом'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Главные премьеры 2024 года»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt15239678',1),('tt28607951',2),('tt2788316',3),('tt17279496',4))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Главные премьеры 2024 года'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Блокбастеры на выходные»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt15239678',1),('tt0468569',2),('tt0167260',3),('tt0816692',4),('tt1517268',5),('tt0231507',6))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Блокбастеры на выходные'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Каннские лауреаты»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt28607951',1),('tt6751668',2),('tt7160372',3),('tt0047478',4))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Каннские лауреаты'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Криминальные саги»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0068646',1),('tt0903747',2),('tt3032476',3),('tt0141842',4),('tt0110912',5),('tt0118767',6),('tt0773262',7))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Криминальные саги'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Сериалы на одну ночь»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt7366338',1),('tt28015403',2))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Сериалы на одну ночь'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Истории становления»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0903747',1),('tt3032476',2),('tt0118767',3),('tt15398776',4),('tt14230458',5))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Истории становления'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- «Главная редакция MovieHub: must-watch»
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES
    ('tt0111161',1),('tt0068646',2),('tt0903747',3),('tt2788316',4),('tt15239678',5),
    ('tt15398776',6),('tt0944947',7),('tt0079944',8),('tt6751668',9),('tt0167260',10),
    ('tt0816692',11),('tt7366338',12))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Главная редакция MovieHub: must-watch'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- =====================================================================
-- Конец seed-скрипта
-- =====================================================================

-- =====================================================================
-- === Доп. контент v2: расширение до 100+ ===
-- Добавляет 60+ контентов (30 фильмов + 30 сериалов), 6 пользователей,
-- 150+ оценок, 30 рецензий, 50 комментариев, 10 подборок.
-- Идемпотентно: WHERE NOT EXISTS / ON CONFLICT DO NOTHING.
-- =====================================================================

-- ---------------------------------------------------------------------
-- v2.1 USERS (6 новых)
-- ---------------------------------------------------------------------
INSERT INTO users (username, email, password_hash, role, is_active) VALUES
    ('documentaries_only', 'docs@example.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
    ('russian_cinema_fan', 'rucinefan@example.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
    ('90s_kid',            'ninetieskid@example.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
    ('horror_lover',       'horror@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
    ('comedy_addict',      'comedy@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE),
    ('silent_era',         'silent@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', TRUE)
ON CONFLICT (username) DO NOTHING;

-- ---------------------------------------------------------------------
-- v2.2 MOVIES (30 новых)
-- ---------------------------------------------------------------------

-- M01 Носферату (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Носферату', 'Nosferatu', 'Готическая история одержимости, в которой молодую женщину Эллен Хаттер преследует древний трансильванский вампир граф Орлок. Роберт Эггерс переосмысляет немецкую классику 1922 года, превращая её в плотный визуальный кошмар о вожделении, болезни и тенях, скрытых в немецком романтизме. Камера Жарена Блашке работает с тенями как с самостоятельным героем.', 2024, 'https://image.tmdb.org/t/p/w500/5qGIxdEO841C0tdY8vOdLoRVrr0.jpg', 7.10, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt5040012', '6326856'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5040012');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 132, 50000000, 181263000 FROM content WHERE imdb_id = 'tt5040012' ON CONFLICT (content_id) DO NOTHING;

-- M02 Субстанция (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Субстанция', 'The Substance', 'Стареющая телезвезда Элизабет Спаркл узнаёт о существовании таинственного препарата, способного создать её молодую и улучшенную версию. Корали Фаржа доводит боди-хоррор до абсурдного и тошнотворного крещендо, превращая фильм в злую сатиру на индустрию красоты. Деми Мур и Маргарет Куэлли отдают роли свои тела буквально.', 2024, 'https://image.tmdb.org/t/p/w500/lqoMzCcZYEFK729d6qzt349fB4o.jpg', 7.30, 'MOVIE', 'PUBLISHED', 'Франция', 'en', 'tt17526714', '5388570'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt17526714');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 141, 17500000, 79325000 FROM content WHERE imdb_id = 'tt17526714' ON CONFLICT (content_id) DO NOTHING;

-- M03 Конклав (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Конклав', 'Conclave', 'После внезапной смерти Папы Римского кардинал Лоуренс возглавляет секретные выборы нового понтифика. За закрытыми дверями Сикстинской капеллы разворачивается политическая интрига, где каждый кардинал — игрок с собственными амбициями и тёмными тайнами. Эдвард Бергер ставит холодный, выверенный триллер о власти и вере.', 2024, 'https://image.tmdb.org/t/p/w500/3VkEjg7N1Ldqko4z2CPEW3WqEvT.jpg', 7.40, 'MOVIE', 'PUBLISHED', 'Великобритания', 'en', 'tt20215234', '6408676'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt20215234');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 120, 20000000, 113462000 FROM content WHERE imdb_id = 'tt20215234' ON CONFLICT (content_id) DO NOTHING;

-- M04 Фуриоса: Хроники Безумного Макса (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Фуриоса: Хроники Безумного Макса', 'Furiosa: A Mad Max Saga', 'Юную Фуриосу похищают из мирного оазиса в Зелёных землях и забирают в Цитадель тирана Дементуса. Пятнадцать лет она ждёт момента, чтобы вернуться домой и отомстить. Джордж Миллер возвращается в свою постапокалиптическую вселенную с эпическим приквелом, в котором экшен сменяется библейской мифологией.', 2024, 'https://image.tmdb.org/t/p/w500/iADOJ8Zymht2JPMoy3R7xceZprc.jpg', 7.20, 'MOVIE', 'PUBLISHED', 'Австралия', 'en', 'tt12037194', '4664994'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt12037194');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 148, 168000000, 174300000 FROM content WHERE imdb_id = 'tt12037194' ON CONFLICT (content_id) DO NOTHING;

-- M05 Безупречная (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Безупречная', 'Immaculate', 'Молодая американка Сесилия принимает приглашение присоединиться к итальянскому женскому монастырю. После приезда она обнаруживает, что её ждёт чудо непорочного зачатия — но за этим стоит мрачная тайна. Сидни Суини в качестве продюсера превратила жанровый хоррор в личную историю одержимости.', 2024, 'https://image.tmdb.org/t/p/w500/aRJ9MbqVBMIkXk1ZnTbHCyJeqVM.jpg', 6.10, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt12235712', '4663930'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt12235712');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 89, 9000000, 51400000 FROM content WHERE imdb_id = 'tt12235712' ON CONFLICT (content_id) DO NOTHING;

-- M06 Мегалополис (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Мегалополис', 'Megalopolis', 'Архитектор Цезарь Катилина мечтает построить утопический город будущего на руинах разрушенного Нового Рима. Его планы наталкиваются на сопротивление коррумпированного мэра, мечты о власти и любовь к его дочери. Фрэнсис Форд Коппола вложил собственные 120 миллионов в безумный проект-манифест о том, что искусство выше политики.', 2024, 'https://image.tmdb.org/t/p/w500/5ZYBO3Y6yMuoa0bDV4ME3JjFhfr.jpg', 5.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt1865505', '258688'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt1865505');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 138, 120000000, 13800000 FROM content WHERE imdb_id = 'tt1865505' ON CONFLICT (content_id) DO NOTHING;

-- M07 Babygirl (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Бэбигёрл', 'Babygirl', 'Успешная руководительница крупной компании Роми вступает в опасную связь со своим намного более молодым стажёром. Эротический триллер Халины Райн о власти, желании и саморазрушении в корпоративной среде. Николь Кидман получила приз Венецианского кинофестиваля за бесстрашную работу.', 2024, 'https://image.tmdb.org/t/p/w500/8u7tRG4ZxyIgD8MZK6ZRvvrxH7Y.jpg', 6.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt28510079', NULL
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt28510079');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 114, 20000000, 36100000 FROM content WHERE imdb_id = 'tt28510079' ON CONFLICT (content_id) DO NOTHING;

-- M08 Bird (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Птица', 'Bird', 'Двенадцатилетняя Бейли живёт со своим хаотичным отцом и братом в скваттерском доме на севере Англии. Ища убежища от трудного быта, она встречает странного незнакомца по имени Птица. Андреа Арнольд продолжает свою хронику британской бедности с присущим ей смешением реализма и магии.', 2024, 'https://image.tmdb.org/t/p/w500/zxuNi3SsjsZqOlJDqsfWP5pd1Pp.jpg', 6.80, 'MOVIE', 'PUBLISHED', 'Великобритания', 'en', 'tt27713378', NULL
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt27713378');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 119, 8000000, 4500000 FROM content WHERE imdb_id = 'tt27713378' ON CONFLICT (content_id) DO NOTHING;

-- M09 8 1/2 (1963)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Восемь с половиной', '8½', 'Известный итальянский режиссёр Гвидо Ансельми оказывается в творческом кризисе перед началом съёмок нового фильма. Реальность смешивается с воспоминаниями, фантазиями и снами, обнажая внутренний мир художника. Шедевр Феллини задал стандарт автобиографического кино на полвека вперёд.', 1963, 'https://image.tmdb.org/t/p/w500/k4OVi9rddPFFGuBAQAYHpSyXnMs.jpg', 8.20, 'MOVIE', 'PUBLISHED', 'Италия', 'it', 'tt0056801', '478'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0056801');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 138, 1000000, NULL FROM content WHERE imdb_id = 'tt0056801' ON CONFLICT (content_id) DO NOTHING;

-- M10 Метрополис (1927)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Метрополис', 'Metropolis', 'В футуристическом городе элита живёт в роскоши на поверхности, а рабочие порабощены под землёй и обслуживают огромные машины. Сын мэра города влюбляется в пророчицу из подземелий и становится мостом между мирами. Фриц Ланг создал визуальный шаблон, который будет цитировать всё научно-фантастическое кино XX века.', 1927, 'https://image.tmdb.org/t/p/w500/aiGWfMRe7y2DZl9SQXSnVojzPK6.jpg', 8.30, 'MOVIE', 'PUBLISHED', 'Германия', 'de', 'tt0017136', '396'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0017136');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 153, 5300000, 1236000 FROM content WHERE imdb_id = 'tt0017136' ON CONFLICT (content_id) DO NOTHING;

-- M11 Семь (1995)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Семь', 'Se7en', 'Опытный детектив Сомерсет накануне ухода на пенсию работает с молодым напарником Миллсом над делом серийного убийцы. Преступник методично воплощает в своих убийствах семь смертных грехов. Дэвид Финчер задал стандарт мрачного нуарного триллера, который будут копировать ближайшие тридцать лет.', 1995, 'https://image.tmdb.org/t/p/w500/6yoghtyTpznpBik8EngEmJskVUO.jpg', 8.70, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0114369', '380'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0114369');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 127, 33000000, 327311859 FROM content WHERE imdb_id = 'tt0114369' ON CONFLICT (content_id) DO NOTHING;

-- M12 Молчание ягнят (1991)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Молчание ягнят', 'The Silence of the Lambs', 'Молодая курсантка ФБР Кларисса Старлинг получает задание провести интервью с заключённым доктором Ганнибалом Лектером — гениальным психиатром и каннибалом. Лектер соглашается помочь ей в поимке другого серийного убийцы по прозвищу Буффало Билл. Психологический триллер Джонатана Демми, собравший все главные Оскары.', 1991, 'https://image.tmdb.org/t/p/w500/uS9m8OBk1A8eM9I042bx8XXpqAq.jpg', 8.80, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0102926', '388'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0102926');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 118, 19000000, 272742922 FROM content WHERE imdb_id = 'tt0102926' ON CONFLICT (content_id) DO NOTHING;

-- M13 Бегущий по лезвию (1982)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Бегущий по лезвию', 'Blade Runner', 'Лос-Анджелес 2019 года. Бывший полицейский Рик Декард получает задание выследить и уничтожить четырёх беглых репликантов — биоинженерных людей. По мере охоты он начинает задаваться вопросом: что отличает человека от машины. Ридли Скотт создал визуальный шаблон киберпанка раз и навсегда.', 1982, 'https://image.tmdb.org/t/p/w500/63N9uy8nd9j7Eog2axPQ8lbr3Wj.jpg', 8.10, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0083658', '371'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0083658');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 117, 28000000, 41700000 FROM content WHERE imdb_id = 'tt0083658' ON CONFLICT (content_id) DO NOTHING;

-- M14 Космическая одиссея 2001 (1968)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT '2001: Космическая одиссея', '2001: A Space Odyssey', 'От рассвета человечества до космической эры — Стэнли Кубрик прослеживает эволюцию разума через таинственный чёрный монолит. Группа астронавтов отправляется к Юпитеру под управлением сверхинтеллектуального компьютера HAL 9000. Фильм без диалогов в первые двадцать пять минут и без объяснений до самого финала — визионерский эпос на все времена.', 1968, 'https://image.tmdb.org/t/p/w500/ve72VxNqjGM69Uky4WTo2bK6rfq.jpg', 8.30, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0062622', '376'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0062622');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 149, 12000000, 138000000 FROM content WHERE imdb_id = 'tt0062622' ON CONFLICT (content_id) DO NOTHING;

-- M15 Чужой (1979)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Чужой', 'Alien', 'Коммерческий буксир «Ностромо» возвращается на Землю с грузом, когда экипаж принимает сигнал бедствия с неизвестной планеты. На борт корабля проникает существо, которое начинает безжалостно охотиться на каждого. Ридли Скотт создал эталон космического хоррора, переосмысливший правила жанра.', 1979, 'https://image.tmdb.org/t/p/w500/vfrQk5IPloGg1v9Rzbh2Eg3VGyM.jpg', 8.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0078748', '8285'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0078748');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 117, 11000000, 184000000 FROM content WHERE imdb_id = 'tt0078748' ON CONFLICT (content_id) DO NOTHING;

-- M16 Назад в будущее (1985)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Назад в будущее', 'Back to the Future', 'Подросток Марти МакФлай случайно отправляется в 1955 год на машине времени, построенной его другом — учёным-эксцентриком Доком Брауном. Чтобы вернуться домой, ему нужно свести вместе своих будущих родителей и не нарушить ход истории. Роберт Земекис снял идеальный летний блокбастер, обогнавший своё время на десятилетия.', 1985, 'https://image.tmdb.org/t/p/w500/6PJF4NamX4Bh2ouuKlw9OzfOukr.jpg', 8.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0088763', '474'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0088763');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 116, 19000000, 388000000 FROM content WHERE imdb_id = 'tt0088763' ON CONFLICT (content_id) DO NOTHING;

-- M17 Терминатор 2: Судный день (1991)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Терминатор 2: Судный день', 'Terminator 2: Judgment Day', 'Из будущего возвращаются два киборга-терминатора. Один — обновлённая модель T-1000 — отправлен убить юного Джона Коннора, лидера будущего сопротивления. Второй — старый T-800 — должен его защитить. Джеймс Кэмерон превратил сиквел в эталон жанра и установил новую планку для спецэффектов.', 1991, 'https://image.tmdb.org/t/p/w500/5M0j0B18abtBI5gi2RhfjjurTqb.jpg', 8.60, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0103064', '454'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0103064');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 137, 102000000, 520881154 FROM content WHERE imdb_id = 'tt0103064' ON CONFLICT (content_id) DO NOTHING;

-- M18 Матрица (1999)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Матрица', 'The Matrix', 'Программист Томас Андерсон, известный в подполье как хакер Нео, узнаёт от загадочного Морфеуса, что окружающий мир — компьютерная симуляция, в которой человечество порабощено машинами. Он встаёт на путь освобождения. Сёстры Вачовски переписали правила боевика и заложили визуальный канон поп-культуры.', 1999, 'https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg', 8.70, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0133093', '301'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0133093');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 136, 63000000, 467200000 FROM content WHERE imdb_id = 'tt0133093' ON CONFLICT (content_id) DO NOTHING;

-- M19 Унесённые призраками (2001)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Унесённые призраками', '千と千尋の神隠し', 'Десятилетняя Тихиро вместе с родителями случайно попадает в мир духов. После того как её родителей превращают в свиней, девочке приходится устроиться работать в загадочную баню для духов и найти способ вернуть всех домой. Хаяо Миядзаки создал сказку, понятную взрослым и детям одновременно.', 2001, 'https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg', 9.00, 'MOVIE', 'PUBLISHED', 'Япония', 'ja', 'tt0245429', '410'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0245429');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 125, 19000000, 395580000 FROM content WHERE imdb_id = 'tt0245429' ON CONFLICT (content_id) DO NOTHING;

-- M20 Принцесса Мононоке (1997)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Принцесса Мононоке', 'もののけ姫', 'Принц Аситака отправляется на запад, чтобы найти лекарство от смертельного проклятия. Там он оказывается в центре конфликта между лесными духами и людьми из железоплавильного посёлка, возглавляемого госпожой Эбоси. Хаяо Миядзаки исследует тему сосуществования человека и природы без однозначных героев.', 1997, 'https://image.tmdb.org/t/p/w500/cMYCDADoLKLbB83g4WnJegaZimC.jpg', 8.80, 'MOVIE', 'PUBLISHED', 'Япония', 'ja', 'tt0119698', '559'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0119698');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 134, 23500000, 169700000 FROM content WHERE imdb_id = 'tt0119698' ON CONFLICT (content_id) DO NOTHING;

-- M21 Король Лев (1994)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Король Лев', 'The Lion King', 'Львёнок Симба — наследник своего отца, мудрого короля Муфасы. Когда коварный дядя Шрам убивает короля и обвиняет в этом Симбу, юный лев отправляется в изгнание. Через годы ему предстоит вернуться и принять свою судьбу. Студия Disney создала анимационный шедевр, по сути пересказывающий «Гамлета».', 1994, 'https://image.tmdb.org/t/p/w500/sKCr78MXSLixwmZ8DyJLrpMsd15.jpg', 8.50, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0110357', '2360'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0110357');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 88, 45000000, 968511805 FROM content WHERE imdb_id = 'tt0110357' ON CONFLICT (content_id) DO NOTHING;

-- M22 История игрушек (1995)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'История игрушек', 'Toy Story', 'Игрушки в комнате мальчика Энди оживают, когда никто не видит. Лидер банды — ковбой Вуди — оказывается под угрозой, когда на день рождения Энди дарят новую крутую игрушку — космонавта Базза Лайтера. Pixar совершил революцию, выпустив первый полнометражный компьютерный мультфильм.', 1995, 'https://image.tmdb.org/t/p/w500/uXDfjJbdP4ijW5hWSBrPrlKpxab.jpg', 8.30, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0114709', '8313'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0114709');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 81, 30000000, 394436586 FROM content WHERE imdb_id = 'tt0114709' ON CONFLICT (content_id) DO NOTHING;

-- M23 Ходячий замок (2004)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Ходячий замок', 'ハウルの動く城', 'Юная шляпница София случайно навлекает на себя проклятие коварной ведьмы Пустоши и превращается в старуху. В поисках исцеления она находит убежище в передвижном замке могущественного волшебника Хаула. Миядзаки экранизировал роман Дианы Уинн Джонс с присущей ему визуальной фантазией.', 2004, 'https://image.tmdb.org/t/p/w500/TkTPELv4kC3u1lkloush8skOjE.jpg', 8.40, 'MOVIE', 'PUBLISHED', 'Япония', 'ja', 'tt0347149', '12122'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0347149');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 119, 24000000, 236000000 FROM content WHERE imdb_id = 'tt0347149' ON CONFLICT (content_id) DO NOTHING;

-- M24 Зверополис (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Зверополис', 'Zootopia', 'В современном городе Зверополис, населённом антропоморфными животными, крольчиха Джуди Хоппс становится первым полицейским своего вида. В паре с хитроумным лисом Ником Уайлдом она расследует загадочные исчезновения хищников. Disney снял умную и злободневную сатиру на расовые предрассудки.', 2016, 'https://image.tmdb.org/t/p/w500/sM33SANp9z6rXW8Itn7NnG1GOEs.jpg', 8.00, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt2948356', '775276'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2948356');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 108, 150000000, 1023784195 FROM content WHERE imdb_id = 'tt2948356' ON CONFLICT (content_id) DO NOTHING;

-- M25 ВАЛЛ-И (2008)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'ВАЛЛ-И', 'WALL·E', 'В далёком будущем Земля превращена в свалку, а человечество живёт на космическом корабле. На опустевшей планете остался один маленький робот-уборщик ВАЛЛ-И, который уже много лет складирует мусор в кубики. Однажды на Землю прилетает изящный робот ИВ, и ВАЛЛ-И отправляется за ней через всю галактику.', 2008, 'https://image.tmdb.org/t/p/w500/hbhFnRzzg6ZDmm8YAmxBnQpQIPh.jpg', 8.40, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt0910970', '278229'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0910970');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 98, 180000000, 521311860 FROM content WHERE imdb_id = 'tt0910970' ON CONFLICT (content_id) DO NOTHING;

-- M26 Душа (2020)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Душа', 'Soul', 'Школьный учитель музыки Джо Гарднер мечтает о карьере джазового пианиста. В день, когда ему наконец улыбается удача, он случайно проваливается в люк и его душа отделяется от тела. Чтобы вернуться к жизни, ему предстоит пройти через мир Большого Прежде. Pixar исследует природу призвания и смысл жизни.', 2020, 'https://image.tmdb.org/t/p/w500/hm58Jw4Lw8OIeECIq5qyPYhAeRJ.jpg', 8.10, 'MOVIE', 'PUBLISHED', 'США', 'en', 'tt2948372', '1142055'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2948372');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 100, 150000000, 121120527 FROM content WHERE imdb_id = 'tt2948372' ON CONFLICT (content_id) DO NOTHING;

-- M27 Седьмая печать (1957)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Седьмая печать', 'Det sjunde inseglet', 'Средневековый рыцарь Антониус Блок возвращается из крестового похода в чумную Швецию и встречает на берегу Смерть, явившуюся за ним. Чтобы оттянуть свой конец, он предлагает Смерти партию в шахматы. Ингмар Бергман создал шедевр, ставший визитной карточкой европейского интеллектуального кино.', 1957, 'https://image.tmdb.org/t/p/w500/ng6QinBfTiGsf38l6fxX1CFWMmy.jpg', 8.20, 'MOVIE', 'PUBLISHED', 'Швеция', 'sv', 'tt0050976', '8276'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0050976');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 96, 150000, 100000 FROM content WHERE imdb_id = 'tt0050976' ON CONFLICT (content_id) DO NOTHING;

-- M28 Развод Надера и Симин (2011)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Развод Надера и Симин', 'جدایی نادر از سیمین', 'Иранская семейная пара Надер и Симин подаёт на развод после спора об эмиграции. Когда Надер нанимает женщину ухаживать за своим больным отцом, цепь недоразумений приводит к судебному разбирательству. Асгар Фархади снял шедевр о моральных дилеммах, не имеющих простых решений.', 2011, 'https://image.tmdb.org/t/p/w500/4Lmpz3CkucJZ9BYyl3CLlJOxTl0.jpg', 8.20, 'MOVIE', 'PUBLISHED', 'Иран', 'fa', 'tt1832382', '503143'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt1832382');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 123, 800000, 24000000 FROM content WHERE imdb_id = 'tt1832382' ON CONFLICT (content_id) DO NOTHING;

-- M29 Море внутри (2004)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Море внутри', 'Mar adentro', 'Парализованный после неудачного прыжка в воду Рамон Сампедро на протяжении почти тридцати лет добивается права на эвтаназию. Алехандро Аменабар экранизирует реальную историю, сочетая судебную драму с поэтической медитацией о свободе и достоинстве. Хавьер Бардем играет роль одной интонацией.', 2004, 'https://image.tmdb.org/t/p/w500/cIfxiaqUOPMDp4DbFjBTnXXOOk.jpg', 8.00, 'MOVIE', 'PUBLISHED', 'Испания', 'es', 'tt0369702', '79853'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0369702');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 125, 10000000, 41700000 FROM content WHERE imdb_id = 'tt0369702' ON CONFLICT (content_id) DO NOTHING;

-- M30 Мастер и Маргарита (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Мастер и Маргарита', 'Мастер и Маргарита', 'Москва тридцатых годов. Опальный Мастер пишет роман о Понтии Пилате и встречает свою Маргариту. Тем временем в столицу прибывает дьявол Воланд со своей свитой. Михаил Локшин предлагает свежую и амбициозную экранизацию романа Булгакова, в которой реальность писателя и его вымысел сплетаются воедино.', 2024, 'https://image.tmdb.org/t/p/w500/5SC4kHPkQk7EOvogRNSsOaG2k7P.jpg', 7.80, 'MOVIE', 'PUBLISHED', 'Россия', 'ru', 'tt23203364', '4720970'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt23203364');
INSERT INTO movies (content_id, duration, budget, box_office) SELECT id, 158, 13000000, 23500000 FROM content WHERE imdb_id = 'tt23203364' ON CONFLICT (content_id) DO NOTHING;
-- ---------------------------------------------------------------------
-- v2.3 SERIES (30 новых)
-- ---------------------------------------------------------------------

-- S01 The Last of Us (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Одни из нас', 'The Last of Us', 'Через двадцать лет после того, как пандемия паразитического грибка превратила большую часть человечества в кровожадных существ, контрабандист Джоэл получает заказ переправить четырнадцатилетнюю девочку Элли через всю разрушенную Америку. Их путешествие меняет обоих. HBO экранизирует культовую игру с акцентом на психологию и моральные дилеммы.', 2023, 'https://image.tmdb.org/t/p/w500/uKvVjHNqB5VmOrdxqAt2F7J78ED.jpg', 8.70, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt3581920', '1131114'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt3581920');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 16, FALSE FROM content WHERE imdb_id = 'tt3581920' ON CONFLICT (content_id) DO NOTHING;

-- S02 Andor (2022)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Андор', 'Andor', 'Приквел к фильму «Изгой-один», прослеживающий путь Кассиана Андора от мелкого вора до революционера, сражающегося с Галактической Империей. Тони Гилрой освобождает «Звёздные войны» от джедайской мифологии и снимает шпионский триллер о цене сопротивления авторитаризму. Лучший проект во вселенной за десятилетия.', 2022, 'https://image.tmdb.org/t/p/w500/q4O8aGZ4d3eQKb0XggdH3TvIrgQ.jpg', 8.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt9253284', '1318899'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt9253284');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 24, FALSE FROM content WHERE imdb_id = 'tt9253284' ON CONFLICT (content_id) DO NOTHING;

-- S03 Severance (2022)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Разделение', 'Severance', 'Сотрудники таинственной корпорации Lumon Industries добровольно проходят процедуру «разделения», навсегда отделяя свою рабочую память от личной. Когда офисная жизнь начинает выходить из-под контроля, Марк Скаут и его коллеги начинают задаваться вопросами о природе своего существования. Бен Стиллер снял лучший корпоративный сай-фай десятилетия.', 2022, 'https://image.tmdb.org/t/p/w500/iQAZL5qHbXwLHncsAeOH3pyPxbM.jpg', 8.60, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt11280740', '1318834'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt11280740');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 19, FALSE FROM content WHERE imdb_id = 'tt11280740' ON CONFLICT (content_id) DO NOTHING;

-- S04 Slow Horses (2022)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Медленные лошади', 'Slow Horses', 'Команда опальных британских разведчиков из MI5, сосланных в офис Slough House под начальство неряшливого Джексона Лэмба. Их карьеры разрушены ошибками прошлого, но именно они оказываются в центре дел, от которых отказались элитные службы. Гэри Олдман в лучшей форме за десятилетие.', 2022, 'https://image.tmdb.org/t/p/w500/u2lHHpzitw2k8LcVK8xUkxGXAM6.jpg', 8.20, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt5678012', '1318772'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5678012');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 24, FALSE FROM content WHERE imdb_id = 'tt5678012' ON CONFLICT (content_id) DO NOTHING;

-- S05 Yellowjackets (2021)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Жёлтые жилеты', 'Yellowjackets', 'В 1996 году женская футбольная команда из Нью-Джерси терпит крушение в дикой местности и проводит девятнадцать месяцев в борьбе за выживание. Двадцать пять лет спустя выжившие хранят страшную тайну, которая начинает разрушать их взрослые жизни. Хорошо сплетённая мистическая драма с двумя временными линиями.', 2021, 'https://image.tmdb.org/t/p/w500/h0Y9oMQVGpfHqWE7iBBGn5ZeUUF.jpg', 7.90, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt11041332', '1357533'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt11041332');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 3, 28, FALSE FROM content WHERE imdb_id = 'tt11041332' ON CONFLICT (content_id) DO NOTHING;

-- S06 True Detective (2014)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Настоящий детектив', 'True Detective', 'Антологический сериал, в каждом сезоне которого новые детективы расследуют новое преступление в новой обстановке. Первый сезон с Мэттью Макконахи и Вуди Харрельсоном на болотах Луизианы переписал правила телевизионного крайма. Тёмная атмосфера, философские диалоги, оккультные мотивы.', 2014, 'https://image.tmdb.org/t/p/w500/aowr4xpLP5sRCL50TkuADomJ98T.jpg', 8.90, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt2356777', '634773'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2356777');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 30, FALSE FROM content WHERE imdb_id = 'tt2356777' ON CONFLICT (content_id) DO NOTHING;

-- S07 Fargo (2014)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Фарго', 'Fargo', 'Антология чёрной комедии Ноа Хоули по мотивам одноимённого фильма братьев Коэн. В каждом сезоне новая история о мелких преступниках и провинциальных стражах порядка в заснеженных просторах Среднего Запада. Холодный юмор, причудливые персонажи, неизменная мораль о банальности зла.', 2014, 'https://image.tmdb.org/t/p/w500/wckbrCa28HYL0aCv86spnDtWKTA.jpg', 8.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt2802850', '720854'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt2802850');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 5, 52, FALSE FROM content WHERE imdb_id = 'tt2802850' ON CONFLICT (content_id) DO NOTHING;

-- S08 Ripley (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Рипли', 'Ripley', 'Том Рипли — мелкий мошенник из Нью-Йорка пятидесятых — получает заказ от богатого судовладельца съездить в Италию и убедить его сына вернуться домой. Вместо этого Рипли начинает строить новую жизнь, полную лжи и преступлений. Стивен Заиллян снял восемь часов чёрно-белого визуального гипноза.', 2024, 'https://image.tmdb.org/t/p/w500/9OxdetOEIc7yo48zTeTWy7zhTzk.jpg', 8.10, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt12990770', '5470867'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt12990770');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 1, 8, TRUE FROM content WHERE imdb_id = 'tt12990770' ON CONFLICT (content_id) DO NOTHING;

-- S09 Симпсоны (1989)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Симпсоны', 'The Simpsons', 'Семья Симпсонов — отец-балбес Гомер, мать-домохозяйка Мардж, хулиган Барт, отличница Лиза и младенец Мэгги — живёт в типичном американском городке Спрингфилд. За тридцать пять лет шоу высмеяло все аспекты американской жизни и предсказало половину реальных событий XXI века.', 1989, 'https://image.tmdb.org/t/p/w500/qcr9bBY6MVeLzriKCmJOv1265EL.jpg', 8.50, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0096697', '73674'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0096697');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 36, 768, FALSE FROM content WHERE imdb_id = 'tt0096697' ON CONFLICT (content_id) DO NOTHING;

-- S10 Доктор Хаус (2004)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Доктор Хаус', 'House M.D.', 'Доктор Грегори Хаус — циничный, грубый и блестящий диагност, возглавляющий специальную команду в больнице Принстон-Плейнсборо. Каждую серию его команде попадает пациент с необъяснимыми симптомами — и расследование болезни напоминает детективное. Хью Лори создал одного из самых незабываемых антигероев в телевизионной истории.', 2004, 'https://image.tmdb.org/t/p/w500/3Cz7ySOQJmqiuTdrc6CY0r65yDI.jpg', 8.80, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0412142', '4719'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0412142');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 8, 177, TRUE FROM content WHERE imdb_id = 'tt0412142' ON CONFLICT (content_id) DO NOTHING;

-- S11 LOST (2004)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Остаться в живых', 'Lost', 'Пассажирский самолёт терпит крушение над Тихим океаном, и сорок восемь выживших оказываются на загадочном острове, где происходят необъяснимые вещи. Через флешбэки раскрываются истории каждого героя. Дэймон Линделоф и Дж. Дж. Абрамс задали стандарт мифологической телесериальной драмы.', 2004, 'https://image.tmdb.org/t/p/w500/og6S0aTZU6YUJAbqxeKjCa3kY1E.jpg', 8.30, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0411008', '78758'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0411008');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 6, 121, TRUE FROM content WHERE imdb_id = 'tt0411008' ON CONFLICT (content_id) DO NOTHING;

-- S12 Доктор Кто (2005)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Доктор Кто', 'Doctor Who', 'Загадочный путешественник во времени и пространстве, известный лишь как Доктор, перемещается по Вселенной на корабле ТАРДИС в форме британской полицейской будки. В компании человеческих спутников он встречает чудеса и спасает миры. Возрождённый в 2005 году культовый британский сериал.', 2005, 'https://image.tmdb.org/t/p/w500/sz4zhyGSqSWlICRHvVzc2zbuk2W.jpg', 8.10, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt0436992', '78941'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0436992');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 14, 304, FALSE FROM content WHERE imdb_id = 'tt0436992' ON CONFLICT (content_id) DO NOTHING;

-- S13 The Wire (2002)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Прослушка', 'The Wire', 'Балтимор показан с разных социальных уровней — от наркоторговцев в гетто до мэрии. В каждом сезоне фокус на новой институции: полиция, порт, образование, СМИ. Дэвид Саймон создал самый политически точный портрет современного американского города. Не сериал, а социологический трактат в формате драмы.', 2002, 'https://image.tmdb.org/t/p/w500/4lbclFySvugI51fwsyxBTOm4DqK.jpg', 9.30, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0306414', '60769'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0306414');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 5, 60, TRUE FROM content WHERE imdb_id = 'tt0306414' ON CONFLICT (content_id) DO NOTHING;

-- S14 Mad Men (2007)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Безумцы', 'Mad Men', 'Нью-Йорк, шестидесятые. Дон Дрейпер — звёздный креативный директор рекламного агентства на Мэдисон-авеню — продаёт американцам мечту, в то время как его собственная жизнь рушится под грузом тайн прошлого. Мэттью Вайнер тонко исследует эпоху и человека, который не знает, кто он такой.', 2007, 'https://image.tmdb.org/t/p/w500/7Hjx2mDcbY3RFf8wiNrDe0DkVt7.jpg', 8.70, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0804503', '267898'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0804503');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 7, 92, TRUE FROM content WHERE imdb_id = 'tt0804503' ON CONFLICT (content_id) DO NOTHING;

-- S15 Twin Peaks (1990)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Твин Пикс', 'Twin Peaks', 'В маленьком городке Твин Пикс на северо-западе США находят тело королевы школьного бала Лоры Палмер. Особый агент ФБР Дейл Купер прибывает для расследования и обнаруживает, что под идиллической поверхностью скрываются паранормальные силы. Дэвид Линч изменил облик телевидения раз и навсегда.', 1990, 'https://image.tmdb.org/t/p/w500/yj62yc9AGkzTBjP7Z9b6TgN5MwS.jpg', 8.80, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0098936', '160'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0098936');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 3, 48, TRUE FROM content WHERE imdb_id = 'tt0098936' ON CONFLICT (content_id) DO NOTHING;

-- S16 Atlanta (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Атланта', 'Atlanta', 'Эрнест Маркс «Эрн» бросил Принстон и теперь живёт без денег с подругой и ребёнком. Когда его двоюродный брат становится восходящим рэпером Paper Boi, Эрн становится его менеджером. Дональд Гловер создал не сериал о хип-хопе, а сюрреалистический портрет жизни чернокожих в современной Америке.', 2016, 'https://image.tmdb.org/t/p/w500/uxLm5mLD4FZcq8K5W2ZsbKUaMeS.jpg', 8.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt4288182', '912855'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt4288182');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 41, TRUE FROM content WHERE imdb_id = 'tt4288182' ON CONFLICT (content_id) DO NOTHING;

-- S17 Fleabag (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Дрянь', 'Fleabag', 'Молодая лондонская женщина с разрушенной семейной жизнью пытается удержать на плаву своё кафе и собственный рассудок. Она постоянно нарушает четвёртую стену, обращаясь к зрителю с горьким и хлёстким комментарием. Фиби Уоллер-Бридж создала шедевр на стыке комедии и трагедии за двенадцать получасовых эпизодов.', 2016, 'https://image.tmdb.org/t/p/w500/eAvjm4UZUjFXSIdRX4ipqs56cs.jpg', 8.70, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt5687612', '1133311'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5687612');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 12, TRUE FROM content WHERE imdb_id = 'tt5687612' ON CONFLICT (content_id) DO NOTHING;

-- S18 The Office (2005)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Офис', 'The Office', 'Документальная съёмочная команда снимает повседневную жизнь сотрудников бумажной компании Dunder Mifflin в маленьком городке Скрэнтон, штат Пенсильвания. Менеджер Майкл Скотт убеждён, что он лучший начальник в мире — и почти всегда ошибается. Эталон ситкома XXI века.', 2005, 'https://image.tmdb.org/t/p/w500/qWnJzyZhyy74gjpSjIXWmuk0ifX.jpg', 8.90, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt0386676', '180065'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt0386676');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 9, 201, TRUE FROM content WHERE imdb_id = 'tt0386676' ON CONFLICT (content_id) DO NOTHING;

-- S19 Naturetown (2019) — Наша планета
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Наша планета', 'Our Planet', 'Документальный сериал Netflix, снятый командой Дэвида Аттенборо на протяжении четырёх лет в пятидесяти странах мира. Наблюдение за дикой природой — от ледников Антарктиды до тропических лесов Амазонки — с акцентом на влияние климатических изменений. Кадры качества, которое раньше казалось невозможным.', 2019, 'https://image.tmdb.org/t/p/w500/qdTBwpIsXrKWZUm7H9pvvCjJZBG.jpg', 9.30, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt9253866', '1340930'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt9253866');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 16, FALSE FROM content WHERE imdb_id = 'tt9253866' ON CONFLICT (content_id) DO NOTHING;

-- S20 Planet Earth II (2016)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Планета Земля 2', 'Planet Earth II', 'Дэвид Аттенборо ведёт сквозь шесть тематических серий — от высокогорий до городов — в продолжении легендарного документального проекта BBC. Камеры с высоким разрешением и дроны открывают мир дикой природы с никогда не виданных ракурсов. Образцовый научпоп.', 2016, 'https://image.tmdb.org/t/p/w500/aAtiuS0u8Mg9aetrdGSBVmIkUjK.jpg', 9.40, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt5491994', '975358'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5491994');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 1, 6, TRUE FROM content WHERE imdb_id = 'tt5491994' ON CONFLICT (content_id) DO NOTHING;

-- S21 Mindhunter (2017)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Охотник за разумом', 'Mindhunter', 'Конец 1970-х. Два агента ФБР Холден Форд и Билл Тенч начинают серию интервью с осуждёнными серийными убийцами, чтобы понять психологию преступников и применить эти знания в текущих расследованиях. Дэвид Финчер ставит сериал с медлительной, гипнотической точностью.', 2017, 'https://image.tmdb.org/t/p/w500/fbKE5pT6T9DkyzTkbhf6OltIeDi.jpg', 8.60, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt5290382', '1018695'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5290382');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 19, TRUE FROM content WHERE imdb_id = 'tt5290382' ON CONFLICT (content_id) DO NOTHING;

-- S22 The Bear (2022)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Медведь', 'The Bear', 'Молодой шеф-повар Карми Берзатто из мира высокой кухни возвращается в Чикаго, чтобы управлять семейным сэндвич-баром брата после его самоубийства. Хаотичная, громкая и эмоционально насыщенная кухонная драма от FX. Каждый эпизод разворачивается как одна непрерывная пауза перед взрывом.', 2022, 'https://image.tmdb.org/t/p/w500/oW1GAItVT8VvB8nmU1mKpvY1yJW.jpg', 8.50, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt14452776', '5318300'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt14452776');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 3, 28, FALSE FROM content WHERE imdb_id = 'tt14452776' ON CONFLICT (content_id) DO NOTHING;

-- S23 Succession (2018)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Наследники', 'Succession', 'Логан Рой — стареющий медиамагнат, основатель глобальной империи Waystar Royco, и его четверо детей сражаются за то, кто унаследует бизнес. Семейная сага в декорациях миллиардеров, где каждое слово — оружие, а каждое объятие — кинжал в спину. Шекспировский трагикомический эпос XXI века.', 2018, 'https://image.tmdb.org/t/p/w500/7HW47XbkNQ5fiwQFYGWdw9gs144.jpg', 8.90, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt7660850', '1213116'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt7660850');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 39, TRUE FROM content WHERE imdb_id = 'tt7660850' ON CONFLICT (content_id) DO NOTHING;

-- S24 The Boys (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Пацаны', 'The Boys', 'В мире, где супергерои — продукт корпорации Vought, торгующей героизмом как товаром, обычные ребята во главе с мстительным Билли Бутчером начинают войну против суперов и компании-производителя. Жёсткая сатира Эрика Крипке на корпоративную поп-культуру с непристойностью на грани китча.', 2019, 'https://image.tmdb.org/t/p/w500/2zmTngn1tYC1AvfnrFLhxeD82hz.jpg', 8.40, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt1190634', '1042364'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt1190634');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 32, FALSE FROM content WHERE imdb_id = 'tt1190634' ON CONFLICT (content_id) DO NOTHING;

-- S25 The Mandalorian (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Мандалорец', 'The Mandalorian', 'После падения Галактической Империи одинокий охотник за головами в мандалорской броне путешествует по дальним рубежам галактики. Когда он становится опекуном таинственного младенца того же вида, что и Йода, ему приходится защищать его от тёмных сил. Джон Фавро возрождает магию «Звёздных войн».', 2019, 'https://image.tmdb.org/t/p/w500/sWgBv7LV2PRoQgkxwlibdGXKz1S.jpg', 8.70, 'SERIES', 'PUBLISHED', 'США', 'en', 'tt8111088', '1162064'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt8111088');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 3, 24, FALSE FROM content WHERE imdb_id = 'tt8111088' ON CONFLICT (content_id) DO NOTHING;

-- S26 Тёмные начала (2019)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Тёмные начала', 'His Dark Materials', 'В мире, где души людей живут вне тел в форме животных-деймонов, девочка Лира Белаква отправляется в опасное путешествие, чтобы спасти своего друга и раскрыть тайну загадочной Пыли. Экранизация трилогии Филипа Пулмана, которой не удалось предыдущему фильму.', 2019, 'https://image.tmdb.org/t/p/w500/8bbDmAm6ZzPvkKNBuBYbSAnjUIb.jpg', 7.90, 'SERIES', 'PUBLISHED', 'Великобритания', 'en', 'tt5607976', '1090734'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5607976');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 3, 23, TRUE FROM content WHERE imdb_id = 'tt5607976' ON CONFLICT (content_id) DO NOTHING;

-- S27 Беспринципные (2020)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Беспринципные', 'Беспринципные', 'Жизнь обитателей московских Патриарших прудов — обеспеченных, образованных и максимально циничных. Эпизодические истории о любви, измене, коррупции и нравственных компромиссах столичной элиты. Один из лучших российских комедийных сериалов последних лет — без надрыва, но и без иллюзий.', 2020, 'https://image.tmdb.org/t/p/w500/yYQzXr5dn8H7LFiWmQjBdyPrUgC.jpg', 7.80, 'SERIES', 'PUBLISHED', 'Россия', 'ru', 'tt13561804', '1278928'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt13561804');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 4, 32, FALSE FROM content WHERE imdb_id = 'tt13561804' ON CONFLICT (content_id) DO NOTHING;

-- S28 Маша (2024)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Маша', 'Маша', 'Маша — обычная девочка из российской провинции 1990-х, чьё детство проходит в окружении криминала, безработицы и невидимой угрозы. Анастасия Пальчикова снимает мини-сериал о том, как травмы тех лет отзываются во взрослой жизни. Жёстко, тонко и без ностальгии.', 2024, 'https://image.tmdb.org/t/p/w500/g7Hsi2g8AsT4BsQpr8WkzZvgVbu.jpg', 7.50, 'SERIES', 'PUBLISHED', 'Россия', 'ru', 'tt30127432', '5556687'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt30127432');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 1, 5, TRUE FROM content WHERE imdb_id = 'tt30127432' ON CONFLICT (content_id) DO NOTHING;

-- S29 Тайга (2023)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Тайга', 'Тайга', 'Российский мистический триллер о том, как трое друзей отправляются на охоту в сибирскую тайгу и сталкиваются с чем-то древним и потусторонним. Минисериал работает на стыке этнохоррора и фольклорной драмы, исследуя пограничные состояния человека наедине с дикой природой.', 2023, NULL, 7.20, 'SERIES', 'PUBLISHED', 'Россия', 'ru', 'tt27915934', NULL
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt27915934');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 1, 6, TRUE FROM content WHERE imdb_id = 'tt27915934' ON CONFLICT (content_id) DO NOTHING;

-- S30 Ёлки (2010) → нет, лучше «Метод» (2015)
INSERT INTO content (title, original_title, description, release_year, poster_url, average_rating, content_type, status, country, language, imdb_id, kinopoisk_id)
SELECT 'Метод', 'Метод', 'Майор Меглин — гениальный следователь с непредсказуемыми методами работы, специализирующийся на серийных убийцах. Под его опеку попадает молодая стажёрка Есеня, которая хочет понять, кто на самом деле стоит за серией убийств в её родном городе. Юрий Быков создал визитную карточку российского криминального жанра.', 2015, 'https://image.tmdb.org/t/p/w500/n8WVXOBaGQ6CeFs6JckHoQXzD8L.jpg', 8.20, 'SERIES', 'PUBLISHED', 'Россия', 'ru', 'tt5151318', '888734'
WHERE NOT EXISTS (SELECT 1 FROM content WHERE imdb_id = 'tt5151318');
INSERT INTO series (content_id, total_seasons, total_episodes, is_finished) SELECT id, 2, 32, TRUE FROM content WHERE imdb_id = 'tt5151318' ON CONFLICT (content_id) DO NOTHING;
-- ---------------------------------------------------------------------
-- v2.4 CONTENT_TAGS — теги для 60 новых контентов
-- ---------------------------------------------------------------------

-- Helper: одна большая VALUES таблица (imdb, slug)
INSERT INTO content_tags (content_id, tag_id)
SELECT c.id, t.id
FROM (VALUES
    -- M01 Носферату: ужасы, драма, фэнтези
    ('tt5040012','horror'), ('tt5040012','drama'), ('tt5040012','fantasy'),
    -- M02 Субстанция: ужасы, фантастика, драма
    ('tt17526714','horror'), ('tt17526714','sci-fi'), ('tt17526714','drama'),
    -- M03 Конклав: триллер, драма
    ('tt20215234','thriller'), ('tt20215234','drama'),
    -- M04 Фуриоса: боевик, фантастика, приключения
    ('tt12037194','action'), ('tt12037194','sci-fi'), ('tt12037194','adventure'),
    -- M05 Безупречная: ужасы, триллер
    ('tt12235712','horror'), ('tt12235712','thriller'),
    -- M06 Мегалополис: фантастика, драма
    ('tt1865505','sci-fi'), ('tt1865505','drama'),
    -- M07 Бэбигёрл: триллер, мелодрама, драма
    ('tt28510079','thriller'), ('tt28510079','romance'), ('tt28510079','drama'),
    -- M08 Птица: драма, фэнтези
    ('tt27713378','drama'), ('tt27713378','fantasy'),
    -- M09 8½: драма, комедия
    ('tt0056801','drama'), ('tt0056801','comedy'),
    -- M10 Метрополис: фантастика, драма, фэнтези
    ('tt0017136','sci-fi'), ('tt0017136','drama'), ('tt0017136','fantasy'),
    -- M11 Семь: триллер, криминал, детектив
    ('tt0114369','thriller'), ('tt0114369','crime'), ('tt0114369','detective'),
    -- M12 Молчание ягнят: триллер, криминал, детектив, ужасы
    ('tt0102926','thriller'), ('tt0102926','crime'), ('tt0102926','detective'), ('tt0102926','horror'),
    -- M13 Бегущий по лезвию: фантастика, триллер, детектив
    ('tt0083658','sci-fi'), ('tt0083658','thriller'), ('tt0083658','detective'),
    -- M14 2001: фантастика, приключения, драма
    ('tt0062622','sci-fi'), ('tt0062622','adventure'), ('tt0062622','drama'),
    -- M15 Чужой: фантастика, ужасы, триллер
    ('tt0078748','sci-fi'), ('tt0078748','horror'), ('tt0078748','thriller'),
    -- M16 Назад в будущее: фантастика, приключения, комедия, семейный
    ('tt0088763','sci-fi'), ('tt0088763','adventure'), ('tt0088763','comedy'), ('tt0088763','family'),
    -- M17 Терминатор 2: боевик, фантастика, триллер
    ('tt0103064','action'), ('tt0103064','sci-fi'), ('tt0103064','thriller'),
    -- M18 Матрица: фантастика, боевик
    ('tt0133093','sci-fi'), ('tt0133093','action'),
    -- M19 Унесённые призраками: анимация, фэнтези, приключения, семейный
    ('tt0245429','animation'), ('tt0245429','fantasy'), ('tt0245429','adventure'), ('tt0245429','family'),
    -- M20 Принцесса Мононоке: анимация, фэнтези, приключения
    ('tt0119698','animation'), ('tt0119698','fantasy'), ('tt0119698','adventure'),
    -- M21 Король Лев: анимация, драма, семейный, музыкальный
    ('tt0110357','animation'), ('tt0110357','drama'), ('tt0110357','family'), ('tt0110357','musical'),
    -- M22 История игрушек: анимация, комедия, приключения, семейный
    ('tt0114709','animation'), ('tt0114709','comedy'), ('tt0114709','adventure'), ('tt0114709','family'),
    -- M23 Ходячий замок: анимация, фэнтези, мелодрама
    ('tt0347149','animation'), ('tt0347149','fantasy'), ('tt0347149','romance'),
    -- M24 Зверополис: анимация, комедия, детектив, семейный
    ('tt2948356','animation'), ('tt2948356','comedy'), ('tt2948356','detective'), ('tt2948356','family'),
    -- M25 ВАЛЛ-И: анимация, фантастика, мелодрама, семейный
    ('tt0910970','animation'), ('tt0910970','sci-fi'), ('tt0910970','romance'), ('tt0910970','family'),
    -- M26 Душа: анимация, драма, музыкальный, семейный
    ('tt2948372','animation'), ('tt2948372','drama'), ('tt2948372','musical'), ('tt2948372','family'),
    -- M27 Седьмая печать: драма, фэнтези
    ('tt0050976','drama'), ('tt0050976','fantasy'),
    -- M28 Развод Надера и Симин: драма
    ('tt1832382','drama'),
    -- M29 Море внутри: драма, биография
    ('tt0369702','drama'), ('tt0369702','biography'),
    -- M30 Мастер и Маргарита: драма, фэнтези, мелодрама
    ('tt23203364','drama'), ('tt23203364','fantasy'), ('tt23203364','romance'),
    -- S01 The Last of Us: драма, ужасы, фантастика
    ('tt3581920','drama'), ('tt3581920','horror'), ('tt3581920','sci-fi'),
    -- S02 Andor: фантастика, драма, боевик
    ('tt9253284','sci-fi'), ('tt9253284','drama'), ('tt9253284','action'),
    -- S03 Severance: фантастика, триллер, драма
    ('tt11280740','sci-fi'), ('tt11280740','thriller'), ('tt11280740','drama'),
    -- S04 Slow Horses: триллер, детектив, драма
    ('tt5678012','thriller'), ('tt5678012','detective'), ('tt5678012','drama'),
    -- S05 Yellowjackets: драма, триллер, ужасы
    ('tt11041332','drama'), ('tt11041332','thriller'), ('tt11041332','horror'),
    -- S06 True Detective: криминал, драма, детектив, триллер
    ('tt2356777','crime'), ('tt2356777','drama'), ('tt2356777','detective'), ('tt2356777','thriller'),
    -- S07 Fargo: криминал, драма, комедия, триллер
    ('tt2802850','crime'), ('tt2802850','drama'), ('tt2802850','comedy'), ('tt2802850','thriller'),
    -- S08 Ripley: триллер, драма, криминал
    ('tt12990770','thriller'), ('tt12990770','drama'), ('tt12990770','crime'),
    -- S09 Симпсоны: анимация, комедия, семейный
    ('tt0096697','animation'), ('tt0096697','comedy'), ('tt0096697','family'),
    -- S10 Доктор Хаус: драма, детектив
    ('tt0412142','drama'), ('tt0412142','detective'),
    -- S11 LOST: драма, фантастика, приключения, триллер
    ('tt0411008','drama'), ('tt0411008','sci-fi'), ('tt0411008','adventure'), ('tt0411008','thriller'),
    -- S12 Доктор Кто: фантастика, приключения, драма
    ('tt0436992','sci-fi'), ('tt0436992','adventure'), ('tt0436992','drama'),
    -- S13 The Wire: криминал, драма
    ('tt0306414','crime'), ('tt0306414','drama'),
    -- S14 Безумцы: драма
    ('tt0804503','drama'),
    -- S15 Twin Peaks: драма, детектив, фэнтези, ужасы
    ('tt0098936','drama'), ('tt0098936','detective'), ('tt0098936','fantasy'), ('tt0098936','horror'),
    -- S16 Atlanta: драма, комедия
    ('tt4288182','drama'), ('tt4288182','comedy'),
    -- S17 Fleabag: комедия, драма
    ('tt5687612','comedy'), ('tt5687612','drama'),
    -- S18 Офис: комедия
    ('tt0386676','comedy'),
    -- S19 Наша планета: документальный, семейный
    ('tt9253866','documentary'), ('tt9253866','family'),
    -- S20 Планета Земля 2: документальный, семейный
    ('tt5491994','documentary'), ('tt5491994','family'),
    -- S21 Mindhunter: криминал, драма, триллер, детектив
    ('tt5290382','crime'), ('tt5290382','drama'), ('tt5290382','thriller'), ('tt5290382','detective'),
    -- S22 Медведь: драма, комедия
    ('tt14452776','drama'), ('tt14452776','comedy'),
    -- S23 Наследники: драма, комедия
    ('tt7660850','drama'), ('tt7660850','comedy'),
    -- S24 Пацаны: боевик, фантастика, комедия
    ('tt1190634','action'), ('tt1190634','sci-fi'), ('tt1190634','comedy'),
    -- S25 Мандалорец: фантастика, приключения, боевик
    ('tt8111088','sci-fi'), ('tt8111088','adventure'), ('tt8111088','action'),
    -- S26 Тёмные начала: фэнтези, приключения, драма
    ('tt5607976','fantasy'), ('tt5607976','adventure'), ('tt5607976','drama'),
    -- S27 Беспринципные: комедия, драма
    ('tt13561804','comedy'), ('tt13561804','drama'),
    -- S28 Маша: драма
    ('tt30127432','drama'),
    -- S29 Тайга: триллер, ужасы, драма
    ('tt27915934','thriller'), ('tt27915934','horror'), ('tt27915934','drama'),
    -- S30 Метод: криминал, триллер, детектив, драма
    ('tt5151318','crime'), ('tt5151318','thriller'), ('tt5151318','detective'), ('tt5151318','drama')
) AS v(imdb, slug)
JOIN content c ON c.imdb_id = v.imdb
JOIN tags t ON t.slug = v.slug
ON CONFLICT (content_id, tag_id) DO NOTHING;

-- Пересчёт usage_count
UPDATE tags SET usage_count = sub.cnt
FROM (SELECT tag_id, COUNT(*) AS cnt FROM content_tags GROUP BY tag_id) sub
WHERE tags.id = sub.tag_id;
-- ---------------------------------------------------------------------
-- v2.5 RATINGS — оценки от 14 не-admin юзеров на новые контенты
-- Минимум 150 новых записей. Используем имена пользователей и imdb_id.
-- ---------------------------------------------------------------------

-- cinephile — артхаус и классика, 15 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='cinephile'), c.id, v.val
FROM (VALUES
    ('tt5040012',5), ('tt17526714',4), ('tt20215234',5), ('tt0056801',5),
    ('tt0017136',5), ('tt0114369',5), ('tt0102926',5), ('tt0083658',5),
    ('tt0062622',5), ('tt0078748',5), ('tt0050976',5), ('tt1832382',5),
    ('tt0245429',5), ('tt23203364',4), ('tt12990770',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- criticspeaks — критик с широким диапазоном, 15 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='criticspeaks'), c.id, v.val
FROM (VALUES
    ('tt5040012',4), ('tt17526714',4), ('tt20215234',4), ('tt12037194',4),
    ('tt1865505',3), ('tt0056801',5), ('tt0114369',5), ('tt0102926',5),
    ('tt0133093',4), ('tt2356777',5), ('tt0306414',5), ('tt7660850',5),
    ('tt5687612',5), ('tt0386676',4), ('tt23203364',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- marathonner — сериаломан, 14 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='marathonner'), c.id, v.val
FROM (VALUES
    ('tt3581920',5), ('tt9253284',5), ('tt11280740',5), ('tt5678012',4),
    ('tt11041332',4), ('tt2356777',5), ('tt2802850',5), ('tt12990770',5),
    ('tt0411008',5), ('tt0306414',5), ('tt7660850',5), ('tt0386676',5),
    ('tt8111088',4), ('tt14452776',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- weekendwatcher — мейнстрим, 13 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='weekendwatcher'), c.id, v.val
FROM (VALUES
    ('tt12037194',4), ('tt0103064',5), ('tt0133093',5), ('tt0088763',5),
    ('tt0078748',4), ('tt0114369',4), ('tt0110357',5), ('tt0114709',5),
    ('tt8111088',5), ('tt1190634',4), ('tt3581920',5), ('tt0386676',5),
    ('tt23203364',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- arthouse_fan — фестивальное, ставит низко мейнстрим, 14 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='arthouse_fan'), c.id, v.val
FROM (VALUES
    ('tt17526714',5), ('tt0056801',5), ('tt0017136',5), ('tt0050976',5),
    ('tt1832382',5), ('tt0369702',5), ('tt27713378',5), ('tt12990770',5),
    ('tt0098936',5), ('tt5687612',5), ('tt0306414',5),
    -- мейнстрим — низко
    ('tt12037194',3), ('tt0133093',3), ('tt1190634',3)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- series_lover — только сериалы, 15 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='series_lover'), c.id, v.val
FROM (VALUES
    ('tt3581920',5), ('tt9253284',5), ('tt11280740',5), ('tt5678012',5),
    ('tt11041332',4), ('tt2356777',5), ('tt2802850',5), ('tt12990770',5),
    ('tt0411008',5), ('tt0306414',5), ('tt0804503',5), ('tt0098936',5),
    ('tt7660850',5), ('tt14452776',5), ('tt5290382',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- casual_viewer — мало, 10 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='casual_viewer'), c.id, v.val
FROM (VALUES
    ('tt5040012',4), ('tt12037194',4), ('tt0088763',5), ('tt0103064',5),
    ('tt0133093',4), ('tt0110357',5), ('tt8111088',4), ('tt3581920',4),
    ('tt0386676',5), ('tt23203364',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- documentaries_only — только документалки и серьёзное, 11 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='documentaries_only'), c.id, v.val
FROM (VALUES
    ('tt9253866',5), ('tt5491994',5), ('tt0306414',5), ('tt5290382',5),
    ('tt0102926',4), ('tt0114369',4), ('tt0050976',5), ('tt1832382',5),
    ('tt0369702',5), ('tt23203364',4), ('tt30127432',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- russian_cinema_fan — только русское, 11 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='russian_cinema_fan'), c.id, v.val
FROM (VALUES
    ('tt23203364',5), ('tt30127432',4), ('tt27915934',4), ('tt5151318',5),
    ('tt13561804',4),
    -- классика и существующее
    ('tt0118767',5), ('tt0231507',5), ('tt0079944',5), ('tt2802154',5),
    ('tt0066565',5), ('tt28015403',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- 90s_kid — кино 80-90х, 12 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='90s_kid'), c.id, v.val
FROM (VALUES
    ('tt0088763',5), ('tt0103064',5), ('tt0133093',5), ('tt0114369',5),
    ('tt0102926',5), ('tt0110357',5), ('tt0114709',5), ('tt0119698',5),
    ('tt0096697',5), ('tt0098936',5), ('tt0306414',5),
    ('tt0118767',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- horror_lover — хорроры и мрачное, 12 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='horror_lover'), c.id, v.val
FROM (VALUES
    ('tt5040012',5), ('tt17526714',5), ('tt12235712',4), ('tt0078748',5),
    ('tt0102926',5), ('tt0114369',5), ('tt11041332',4), ('tt27915934',4),
    ('tt5290382',4), ('tt3581920',5), ('tt0098936',5), ('tt0096697',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- comedy_addict — комедии, 12 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='comedy_addict'), c.id, v.val
FROM (VALUES
    ('tt0386676',5), ('tt5687612',5), ('tt4288182',5), ('tt7660850',5),
    ('tt2802850',5), ('tt14452776',5), ('tt0096697',5), ('tt0114709',4),
    ('tt2948356',5), ('tt0088763',5), ('tt13561804',4), ('tt1190634',4)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- silent_era — классика, 11 оценок
INSERT INTO ratings (user_id, content_id, "value")
SELECT (SELECT id FROM users WHERE username='silent_era'), c.id, v.val
FROM (VALUES
    ('tt0017136',5), ('tt0050976',5), ('tt0056801',5), ('tt0062622',5),
    ('tt0083658',5), ('tt0078748',5), ('tt0102926',5),
    -- из существующего: классика
    ('tt0033467',5), ('tt0047478',5), ('tt0079944',5), ('tt0036824',5)
) AS v(imdb,val)
JOIN content c ON c.imdb_id = v.imdb
ON CONFLICT (user_id, content_id) DO NOTHING;

-- Пересчёт average_rating для контента (поверх ВСЕХ оценок).
UPDATE content c SET average_rating = LEAST(ROUND(sub.avg_v::numeric, 2), 9.99::numeric)
FROM (SELECT content_id, AVG("value")::numeric AS avg_v FROM ratings GROUP BY content_id) sub
WHERE c.id = sub.content_id;
-- ---------------------------------------------------------------------
-- v2.6 REVIEWS — 30 новых рецензий
-- ---------------------------------------------------------------------

INSERT INTO reviews (user_id, content_id, title, text, rating_value, status, view_count, like_count)
SELECT u.id, c.id, v.title, v.body, v.rv, 'PUBLISHED', v.vc, v.lc
FROM (VALUES
    ('cinephile', 'tt5040012', 'Тени, в которых живёт желание',
     'Эггерс не пересказывает Мурнау, а ставит готическую оперу о подавленной женской сексуальности и чужой воле. Лили-Роуз Депп играет одержимость без капли наивности — каждое сокращение мышц тут продумано. Камера Жарена Блашке делает с тенями то, что обычно делают спецэффекты. Фильм длится два часа, а оставляет ощущение целого XIX века.', 5, 4200, 167),
    ('arthouse_fan', 'tt17526714', 'Боди-хоррор как ликбез',
     'Корали Фаржа взяла классическую формулу Кроненберга и довела её до гротеска, который смешит и пугает одновременно. Деми Мур и Маргарет Куэлли — не просто хорошо, а выдающе сыграны. Финальный твист в виде «монстра» работает как метафора пожирающей себя медиаиндустрии. Минус полбалла за затянутый второй акт.', 4, 3800, 145),
    ('criticspeaks', 'tt20215234', 'Шепот в Сикстинской капелле',
     'Бергер снял триллер, в котором главное оружие — пауза. Файнс держит фильм на плечах одной интонацией. Сцены тайного голосования напряжённее любого экшена. Финальный твист спорный, но до него вы уже окончательно попадёте под чары этой холодной красоты. Пример того, как умное кино может быть зрелищным.', 5, 5100, 218),
    ('weekendwatcher', 'tt12037194', 'Возвращение к корням саги',
     'Миллер доказал, что в 79 лет можно снимать самые безумные погони года. Аня Тейлор-Джой тащит фильм одним взглядом — диалогов у неё минимум. Сцена с парапланом — лучший экшен, что я видел за последние пять лет. Минусую за провисший пролог, но финал искупает всё.', 4, 6700, 312),
    ('cinephile', 'tt0056801', 'Автопортрет в кризисе',
     'Феллини в этом фильме сделал то, что до него не делал никто: показал режиссёра, который не знает, что снимать, и превратил это в кино. Марчелло Мастроянни играет себя и Феллини одновременно. Финальный круговой танец — самый честный финал в истории кинематографа. Эталон самокритики.', 5, 2800, 134),
    ('silent_era', 'tt0017136', 'Будущее, изобретённое сто лет назад',
     'Ланг придумал визуальный язык, по которому до сих пор живёт всё научно-фантастическое кино. Городская архитектура, машинные интерьеры, образ робота-Марии — всё это родилось здесь. То, что фильм 1927 года смотрится свежо в 2025-м, говорит само за себя. Восстановленная версия с найденными сценами обязательна к просмотру.', 5, 1900, 98),
    ('cinephile', 'tt0114369', 'Финчер задаёт стандарт',
     'Финчер взял жанровый триллер и превратил его в средневековый моралитет. Каждый кадр снят так, будто Калигари держал камеру. Морган Фриман и Брэд Питт играют одну из лучших напарнических пар в истории жанра. Финал в коробке — момент, после которого жанр уже не тот.', 5, 5400, 234),
    ('cinephile', 'tt0102926', 'Гипноз в железной клетке',
     'Энтони Хопкинс заработал Оскар за 16 минут экранного времени и заслужил каждую секунду. Джонатан Демми снял триллер, который держится не на саспенсе, а на психологии каждой сцены. Джоди Фостер играет уязвимость и стальную волю одновременно. Безупречный фильм во всех смыслах.', 5, 6100, 287),
    ('cinephile', 'tt0083658', 'Спор о том, что значит быть человеком',
     'Ридли Скотт собрал вместе нуар, киберпанк и философский трактат — и получился шедевр. Финальный монолог Рутгера Хауэра «Tears in rain» — три минуты, ради которых стоит пересматривать весь фильм. Режиссёрская версия лучше театральной по всем параметрам. Кино, которое создало целый жанр.', 5, 4700, 201),
    ('cinephile', 'tt0062622', 'Молчание как сюжет',
     'Кубрик не снимает кино — он ведёт зрителя через медитацию о месте человека во Вселенной. Первые двадцать минут без слов — самый радикальный пролог в истории. Сцена с HAL 9000, поющим Daisy, ломает все каноны эмпатии. Если у вас нет терпения — пропустите. Если есть — это лучшее, что можно увидеть.', 5, 3900, 178),
    ('horror_lover', 'tt0078748', 'Бог хоррора',
     'Ридли Скотт собрал на маленькой замкнутой локации идеальный фильм ужасов. Чужой как существо до сих пор не имеет аналогов в дизайне монстров. Сигурни Уивер создала образ женщины-героя, переделавший правила жанра. Сцена с грудью на ужине — момент, который останется в памяти навсегда.', 5, 5800, 245),
    ('weekendwatcher', 'tt0088763', 'Идеальный летний блокбастер',
     'Земекис снял фильм, в котором всё работает: химия Майкла Дж. Фокса и Кристофера Ллойда, монтаж, музыка Алана Сильвестри, шутки на всех уровнях. Финал, в котором всё связывается одним кадром — учебник сценарного ремесла. Сорок лет спустя — никаких морщин.', 5, 7200, 318),
    ('weekendwatcher', 'tt0103064', 'Сиквел, переплюнувший оригинал',
     'Кэмерон взял низкобюджетный хоррор и превратил его в эпический боевик с философским подтекстом. Шварценеггер играет на инверсии — герой, защищающий мать, которую он раньше пришёл убить. T-1000 стал визитной карточкой жидкого металла на десятилетия вперёд. Эталон сиквела.', 5, 6500, 287),
    ('cinephile', 'tt0245429', 'Магия Миядзаки в чистом виде',
     'Миядзаки сделал то, что не удавалось до него никому: создал анимационную сказку, понятную детям и взрослым с равной глубиной. Каждый кадр — самостоятельная картина. Без-Лица как метафора потребления, Хаку как мост между мирами — всё работает на нескольких уровнях. Заслуженный Оскар.', 5, 4100, 198),
    ('cinephile', 'tt0119698', 'Без однозначных героев',
     'Миядзаки разрушает шаблон сказки: здесь нет злодеев, только герои с разными правдами. Госпожа Эбоси не зло — она строит новый мир. Принцесса Мононоке защищает старый. Аситака — мост, который пытается их примирить. Финальная сцена за Шиши-гами — кульминация всей экологической мысли мастера.', 5, 3700, 176),
    ('weekendwatcher', 'tt0110357', 'Король анимации',
     'Disney взял Гамлета и переписал его для детей, не потеряв трагической глубины. Сцена смерти Муфасы остаётся одной из самых эмоциональных в истории мультипликации. Музыка Элтона Джона и Ханса Циммера — отдельный шедевр. Не зря этот фильм окупился в десять раз.', 5, 8100, 421),
    ('comedy_addict', 'tt0114709', 'Pixar изобретает будущее',
     'Когда Pixar выпустил «Историю игрушек», никто не ожидал, что компьютерная анимация может рассказывать такие истории. Дружба Вуди и Базза — учебник по характерам в детском кино. Каждая шутка работает на разном уровне. Старт революции анимационного кино.', 5, 5200, 234),
    ('series_lover', 'tt3581920', 'Видеоигра, ставшая шедевром ТВ',
     'Крейг Мейзин и Нил Дракманн совершили невозможное: сняли экранизацию игры, которая работает как самостоятельная драма. Третий эпизод первого сезона про Билла и Фрэнка — лучшая короткометражная любовная история, что я видел за десять лет. Педро Паскаль и Белла Рамзи играют одну из лучших пар в современной телепродукции.', 5, 9800, 487),
    ('series_lover', 'tt9253284', 'Звёздные войны для взрослых',
     'Тони Гилрой освободил вселенную от джедайской мифологии и снял шпионский триллер о революции. Речь Карис Не Дханни в финале — одна из лучших политических речей на современном телевидении. Стелан Скарсгард в роли Луфена Раэля — отдельный вид искусства. Лучший продукт во вселенной за два десятилетия.', 5, 6700, 332),
    ('series_lover', 'tt11280740', 'Шедевр корпоративной жути',
     'Бен Стиллер снял сериал, в котором каждый кадр работает на тревогу. Концепция «разделённых» сотрудников — лучшая метафора современного офисного отчуждения. Адам Скотт играет одну из самых тонких ролей в своей карьере. Финал второго сезона — час напряжения, после которого хочется сразу пересмотреть весь сезон.', 5, 8400, 412),
    ('marathonner', 'tt5678012', 'Шпионы среди мусорных пакетов',
     'Гэри Олдман в роли Джексона Лэмба — это не роль, это явление. Сериал работает на инверсии: главные герои здесь — неудачники из MI5, которым доверяют только то, чем брезгуют другие. Уилл Смит, написавший шесть романов про Slough House — это новый Ле Карре. Сериал на годы вперёд.', 5, 4200, 198),
    ('arthouse_fan', 'tt2356777', 'Атмосфера как герой',
     'Первый сезон с Макконахи и Харрельсоном переписал правила телевизионного крайма. Восьмиминутный непрерывный план в четвёртой серии — техническое чудо. Философские монологи Раста Коула — материал, по которому можно писать диссертации. Остальные сезоны слабее, но первый — эталон.', 5, 7100, 354),
    ('marathonner', 'tt2802850', 'Снег и абсурд',
     'Ноа Хоули переосмыслил вселенную братьев Коэн в долгоиграющем формате — и каждый сезон работает как отдельный фильм. Билли Боб Торнтон в первом сезоне, Кирстен Данст во втором, Юэн МакГрегор в третьем — все на пике формы. Чёрный юмор и моральная серьёзность в идеальной пропорции.', 5, 5800, 276),
    ('arthouse_fan', 'tt12990770', 'Восемь часов чёрно-белого гипноза',
     'Стивен Заиллян снял мини-сериал, который смотрится как продлённый итальянский нуар. Эндрю Скотт играет Тома Рипли с ледяной точностью социопата. Роберт Элсвит — оператор, который превращает каждый кадр в музейный экспонат. Мелодраматизм Хайсмит здесь сменён холодным наблюдением — и это работает.', 5, 3500, 167),
    ('documentaries_only', 'tt9253866', 'Образцовый научпоп',
     'Дэвид Аттенборо снова делает то, что умеет лучше всех: рассказывает о природе так, что хочется бросить всё и записаться в волонтёры WWF. Команда оператора Алистера Фотергилла снимала проект четыре года в пятидесяти странах. Кадры с китами и пингвинами — эталон жанра. Обязательный просмотр.', 5, 4800, 234),
    ('horror_lover', 'tt11041332', 'Двойная временная линия',
     'Юрий Брэйс и Эшли Лайл создали редкий случай шоу, в котором обе временные линии одинаково увлекательны. Кристина Риччи и Джульетта Льюис — выдающиеся актрисы, которые наконец получили роли по своему калибру. Третий сезон чуть слабее, но первые два — must-watch.', 4, 3700, 156),
    ('comedy_addict', 'tt5687612', 'Нарушение четвёртой стены как искусство',
     'Фиби Уоллер-Бридж за 12 эпизодов сделала больше для женской комедии, чем многие за десятилетия. Прямые взгляды в камеру здесь работают не как трюк, а как способ быть честной с зрителем. Эпизод со священником во втором сезоне — лучшая сцена в комедийном телевидении 2010-х. Точно, остро, безжалостно к себе.', 5, 5400, 278),
    ('comedy_addict', 'tt0386676', 'Ситком, который определил эпоху',
     'Грег Дэниелс адаптировал британский «Офис» так, что американская версия превзошла оригинал. Стив Карелл создал образ Майкла Скотта, который не имеет аналогов в мире комедии — одновременно нелепый и трогательный. Девять сезонов, ни один из которых не хочется пропустить. Эталон.', 5, 9300, 478),
    ('arthouse_fan', 'tt0306414', 'Социология в форме драмы',
     'Дэвид Саймон и Эд Бёрнс создали не сериал, а социальный документ о современном американском городе. Каждый из пяти сезонов фокусируется на новой институции — наркоторговле, полиции, школе, прессе, политике. Без Майкла К. Уильямса в роли Омара мир был бы беднее. Абсолютный эталон телевидения.', 5, 5600, 289),
    ('russian_cinema_fan', 'tt23203364', 'Локшин нашёл ключ к Булгакову',
     'После провалов всех предыдущих экранизаций Локшин предложил радикальный ход: не пересказывать роман, а ставить кино о том, как пишется роман. Аугуст Диль в роли Воланда — одна из лучших актёрских работ в российском кино. Юлия Снигирь — настоящая Маргарита. Минусую за слабую вторую половину, но первые полтора часа — событие.', 5, 7800, 354)
) AS v(uname,imdb,title,body,rv,vc,lc)
JOIN users u ON u.username = v.uname
JOIN content c ON c.imdb_id = v.imdb
WHERE NOT EXISTS (
    SELECT 1 FROM reviews r WHERE r.user_id = u.id AND r.content_id = c.id AND r.title = v.title
);
-- ---------------------------------------------------------------------
-- v2.7 COMMENTS — 50 новых коротких комментариев
-- ---------------------------------------------------------------------

INSERT INTO comments (user_id, content_id, text, is_edited)
SELECT u.id, c.id, v.text, FALSE
FROM (VALUES
    ('horror_lover',       'tt5040012','Эггерс снова вытащил из шкафа всё лучшее в готике.'),
    ('cinephile',          'tt5040012','Лили-Роуз Депп заставила меня забыть про её фамилию.'),
    ('arthouse_fan',       'tt17526714','Боди-хоррор уровня раннего Кроненберга.'),
    ('horror_lover',       'tt17526714','Финал шокирует, но логичен — это не дешёвый твист.'),
    ('criticspeaks',       'tt20215234','Файнс играет один из лучших монологов своей карьеры.'),
    ('weekendwatcher',     'tt12037194','Анья Тейлор-Джой — новая королева пустыни.'),
    ('cinephile',          'tt0056801',  '«La dolce vita» в более горьком исполнении.'),
    ('silent_era',         'tt0017136', 'Сто лет — а будущее всё ещё узнаваемо.'),
    ('cinephile',          'tt0114369', 'Финчер изобрёл современный нуар.'),
    ('horror_lover',       'tt0102926', 'Хопкинс гипнотизирует одной фразой.'),
    ('cinephile',          'tt0083658', '«Tears in rain» — три минуты гения.'),
    ('cinephile',          'tt0062622', 'HAL 9000 — самый человечный персонаж в фильме.'),
    ('horror_lover',       'tt0078748', 'Чужой до сих пор страшнее всех современных монстров.'),
    ('weekendwatcher',     'tt0088763', 'Восемьдесят восьмой год сделал тебя счастливым.'),
    ('weekendwatcher',     'tt0103064', 'T-1000 заставил всех бояться полицейских в форме.'),
    ('weekendwatcher',     'tt0133093', '«Знай, я знаю кунг-фу.»'),
    ('cinephile',          'tt0245429', 'Без-Лица теперь живёт в моих снах.'),
    ('cinephile',          'tt0119698', 'Шиши-гами — самый страшный и красивый кадр у Миядзаки.'),
    ('weekendwatcher',     'tt0110357', 'Сцена смерти Муфасы. Каждый раз. Слёзы.'),
    ('comedy_addict',      'tt0114709', 'Базз Лайтер до сих пор самый смешной космонавт.'),
    ('cinephile',          'tt0347149', 'Кальцифер — главный сердцеед мирового аниме.'),
    ('comedy_addict',      'tt2948356', 'Ленивец Флэш — лучшая сцена в детском кино за десятилетие.'),
    ('cinephile',          'tt0910970', 'ВАЛЛ-И за первые сорок минут говорит больше, чем многие за два часа.'),
    ('cinephile',          'tt2948372', 'Pixar снова про смысл жизни — и снова на высоте.'),
    ('arthouse_fan',       'tt0050976', 'Партия в шахматы со смертью. До сих пор пробирает.'),
    ('documentaries_only', 'tt1832382', 'Иранское кино учит нас человеческому достоинству.'),
    ('arthouse_fan',       'tt0369702', 'Хавьер Бардем играет одной интонацией всю палитру.'),
    ('russian_cinema_fan', 'tt23203364', 'Аугуст Диль — лучший Воланд в истории.'),
    ('series_lover',       'tt3581920', 'Третья серия — лучшая отдельная любовная история на ТВ.'),
    ('series_lover',       'tt9253284', 'Карис Не Дханни — лучшая речь сериала за десятилетие.'),
    ('series_lover',       'tt11280740', 'Адам Скотт играет двух персонажей одним лицом.'),
    ('marathonner',        'tt5678012', 'Гэри Олдман — это не роль, это явление.'),
    ('horror_lover',       'tt11041332', 'Кристина Риччи наконец получила роль по калибру.'),
    ('arthouse_fan',       'tt2356777', 'Восьмиминутный план в четвёртом эпизоде — техническое чудо.'),
    ('marathonner',        'tt2802850', 'Каждый сезон — отдельный фильм Коэнов.'),
    ('arthouse_fan',       'tt12990770', 'Чёрно-белая Италия — мечта оператора.'),
    ('comedy_addict',      'tt0096697', 'Симпсоны до сих пор предсказывают будущее.'),
    ('cinephile',          'tt0412142', 'Хью Лори создал лучшего антигероя на ТВ.'),
    ('marathonner',        'tt0411008', '«We have to go back!» — фраза, которая разделила поколение.'),
    ('series_lover',       'tt0436992', 'Тардис — самая надёжная машина времени в кино.'),
    ('arthouse_fan',       'tt0306414', 'Социологический трактат в форме драмы.'),
    ('cinephile',          'tt0804503', 'Дон Дрейпер — лучший американский антигерой XXI века.'),
    ('arthouse_fan',       'tt0098936', 'Линч придумал ТВ, в которое до сих пор играют.'),
    ('comedy_addict',      'tt4288182', 'Дональд Гловер не снимает сериал — он рассказывает сны.'),
    ('comedy_addict',      'tt5687612', '«Hot priest» — лучший сериальный персонаж года.'),
    ('comedy_addict',      'tt0386676', 'Майкл Скотт — это все мы и каждый из нас.'),
    ('documentaries_only', 'tt9253866', 'Аттенборо — национальное достояние человечества.'),
    ('documentaries_only', 'tt5491994', 'Снежные барсы в HD — увидеть и умереть.'),
    ('horror_lover',       'tt5290382', 'Финчер каждый кадр выстраивает как картину.'),
    ('comedy_addict',      'tt14452776', '«Yes, chef!» — фраза, которая теперь у меня в голове постоянно.'),
    ('series_lover',       'tt7660850', 'Кэндалл Рой — лучший трагикомический герой десятилетия.'),
    ('weekendwatcher',     'tt1190634', 'Хоумлендер — самый страшный супергерой когда-либо.'),
    ('weekendwatcher',     'tt8111088', 'Грогу спас Звёздные войны.'),
    ('russian_cinema_fan', 'tt13561804', 'Российские нравы переданы с убийственной точностью.'),
    ('russian_cinema_fan', 'tt30127432', 'Пальчикова сняла больно и правдиво.'),
    ('russian_cinema_fan', 'tt5151318', 'Хабенский в роли Меглина — открытие.')
) AS v(uname,imdb,text)
JOIN users u ON u.username = v.uname
JOIN content c ON c.imdb_id = v.imdb
WHERE NOT EXISTS (
    SELECT 1 FROM comments cm WHERE cm.user_id = u.id AND cm.content_id = c.id AND cm.text = v.text
);

-- ---------------------------------------------------------------------
-- v2.8 PLAYLISTS — 10 новых тематических подборок
-- ---------------------------------------------------------------------

INSERT INTO playlists (user_id, title, description, cover_image_url, is_public)
SELECT u.id, v.title, v.descr, NULL, TRUE
FROM (VALUES
    ('horror_lover',       'Хорроры на хеллоуин',
     'Самое страшное, что снимали в XX-XXI веках — от классики до новой готики.'),
    ('weekendwatcher',     'Анимация для всей семьи',
     'Анимационные фильмы для совместного просмотра с детьми любого возраста.'),
    ('silent_era',         'Чёрно-белая классика',
     'Образцы кинематографа из эпохи, когда визуальный язык изобретался каждый день.'),
    ('cinephile',          'Корейское новое кино',
     'Южнокорейский кинематограф последних двух десятилетий, переписавший мировые правила.'),
    ('marathonner',        'Долгие серии для марафона',
     'Сериалы с большим количеством сезонов — для тех, кто хочет погрузиться надолго.'),
    ('documentaries_only', 'Документальное о природе',
     'Лучшие документальные проекты о животных, экосистемах и климате.'),
    ('comedy_addict',      'Комедии на любой вечер',
     'Лучшие смешные сериалы и фильмы — от ситкомов до тонких трагикомедий.'),
    ('arthouse_fan',       'Фестивальные жемчужины 2024',
     'Главные премьеры года с Канн, Венеции, Берлина и Торонто.'),
    ('russian_cinema_fan', 'Российский авторский кинематограф',
     'Российское кино, которое не идёт на компромиссы — от Тарковского до Локшина.'),
    ('cinephile',          'Sci-fi которое заставляет думать',
     'Научная фантастика с философской нагрузкой — от Кубрика до Вильнёва.')
) AS v(uname, title, descr)
JOIN users u ON u.username = v.uname
WHERE NOT EXISTS (SELECT 1 FROM playlists p WHERE p.title = v.title);

-- ---------------------------------------------------------------------
-- v2.9 PLAYLIST_CONTENT — наполняем 10 новых подборок
-- ---------------------------------------------------------------------

-- Хорроры на хеллоуин
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt5040012',1),('tt17526714',2),('tt0078748',3),('tt0102926',4),('tt12235712',5),('tt27915934',6),('tt11041332',7))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Хорроры на хеллоуин'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Анимация для всей семьи
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0245429',1),('tt0119698',2),('tt0110357',3),('tt0114709',4),('tt0347149',5),('tt2948356',6),('tt0910970',7),('tt2948372',8),('tt0096697',9))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Анимация для всей семьи'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Чёрно-белая классика
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0017136',1),('tt0033467',2),('tt0050976',3),('tt0056801',4),('tt0036824',5),('tt0047478',6),('tt12990770',7))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Чёрно-белая классика'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Корейское новое кино
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt6751668',1),('tt10919420',2))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Корейское новое кино'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Долгие серии для марафона
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0096697',1),('tt0386676',2),('tt0411008',3),('tt0412142',4),('tt0944947',5),('tt0436992',6),('tt0108778',7),('tt0773262',8),('tt0903747',9),('tt0306414',10))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Долгие серии для марафона'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Документальное о природе
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt9253866',1),('tt5491994',2),('tt7366338',3))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Документальное о природе'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Комедии на любой вечер
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0386676',1),('tt5687612',2),('tt0108778',3),('tt0114709',4),('tt2948356',5),('tt4288182',6),('tt7660850',7),('tt13561804',8),('tt0088763',9))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Комедии на любой вечер'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Фестивальные жемчужины 2024
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt5040012',1),('tt17526714',2),('tt20215234',3),('tt28510079',4),('tt27713378',5),('tt28607951',6),('tt23203364',7),('tt12990770',8))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Фестивальные жемчужины 2024'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Российский авторский кинематограф
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0079944',1),('tt2802154',2),('tt23203364',3),('tt0036824',4),('tt30127432',5),('tt28015403',6),('tt0118767',7),('tt27915934',8))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Российский авторский кинематограф'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- Sci-fi которое заставляет думать
INSERT INTO playlist_content (playlist_id, content_id, sort_order)
SELECT p.id, c.id, v.so
FROM (VALUES ('tt0062622',1),('tt0083658',2),('tt0079944',3),('tt0816692',4),('tt15239678',5),('tt0245429',6),('tt0910970',7),('tt11280740',8),('tt9253284',9),('tt0133093',10),('tt5040012',11))
     AS v(imdb,so)
JOIN content c ON c.imdb_id = v.imdb
JOIN playlists p ON p.title = 'Sci-fi которое заставляет думать'
ON CONFLICT (playlist_id, content_id) DO NOTHING;

-- =====================================================================
-- Конец доп. контента v2
-- =====================================================================

-- =====================================================================
-- Этап 3: сущности Genre / Person / содержимое для use-case поиска
-- =====================================================================
INSERT INTO genres (name, slug, description) VALUES
    ('Драма',          'drama',          'Серьёзные сюжеты, психологизм, конфликт характеров'),
    ('Комедия',        'comedy',         'Юмор, лёгкие жанры'),
    ('Триллер',        'thriller',       'Напряжение, саспенс, тайны'),
    ('Боевик',         'action',         'Динамичные сцены, экшен'),
    ('Фантастика',     'sci-fi',         'Будущее, технологии, инопланетный сеттинг'),
    ('Фэнтези',        'fantasy',        'Магия, мифические существа'),
    ('Ужасы',          'horror',         'Страх, готика, мистика'),
    ('Романтика',      'romance',        'Любовные истории, отношения'),
    ('Криминал',       'crime',          'Преступления, расследования'),
    ('Документалистика','documentary',   'Реальные события и люди'),
    ('Анимация',       'animation',      'Мультфильмы и анимационные сериалы'),
    ('Биография',      'biography',      'Жизненные истории реальных людей')
ON CONFLICT (slug) DO NOTHING;

-- Несколько актёров и режиссёров для демонстрации use-case «поиск по имени»
INSERT INTO persons (name, photo_url) VALUES
    ('Киллиан Мерфи',     NULL),
    ('Кристофер Нолан',   NULL),
    ('Тимоти Шаламе',     NULL),
    ('Дени Вильнёв',      NULL),
    ('Брайан Крэнстон',   NULL),
    ('Винс Гиллиган',     NULL),
    ('Эмма Стоун',        NULL),
    ('Йоргос Лантимос',   NULL),
    ('Аль Пачино',        NULL),
    ('Фрэнсис Форд Коппола', NULL)
ON CONFLICT (name) DO NOTHING;

-- =====================================================================
-- content_genres: связь контента с жанрами (use-case «поиск по жанру» Этапа 2)
-- Маппинг по imdb_id → slug жанра. Многие фильмы получают 1-3 жанра.
-- =====================================================================
INSERT INTO content_genres (content_id, genre_id)
SELECT c.id, g.id
FROM content c
JOIN genres g ON g.slug IN (
    -- драматические тяжеловесы
    CASE c.imdb_id
        WHEN 'tt0111161' THEN 'drama'      -- Shawshank
        WHEN 'tt0068646' THEN 'drama'      -- Godfather
        WHEN 'tt0468569' THEN 'drama'      -- Dark Knight
        WHEN 'tt0079944' THEN 'sci-fi'     -- Stalker
        WHEN 'tt15398776' THEN 'drama'     -- Oppenheimer
        WHEN 'tt28607951' THEN 'drama'     -- Anora
        WHEN 'tt14230458' THEN 'fantasy'   -- Poor Things
        WHEN 'tt2802154' THEN 'drama'      -- Cheburashka? — пример
        WHEN 'tt0118767' THEN 'crime'      -- Brat
        WHEN 'tt2788316' THEN 'drama'      -- Whiplash-like
        ELSE NULL END
)
ON CONFLICT DO NOTHING;

-- Дополнительные второстепенные жанры
INSERT INTO content_genres (content_id, genre_id)
SELECT c.id, g.id FROM content c JOIN genres g ON g.slug IN ('thriller','crime')
WHERE c.imdb_id IN ('tt15398776','tt0468569','tt0118767')
ON CONFLICT DO NOTHING;

INSERT INTO content_genres (content_id, genre_id)
SELECT c.id, g.id FROM content c JOIN genres g ON g.slug = 'sci-fi'
WHERE c.imdb_id IN ('tt15239678','tt15398776')
ON CONFLICT DO NOTHING;

INSERT INTO content_genres (content_id, genre_id)
SELECT c.id, g.id FROM content c JOIN genres g ON g.slug = 'fantasy'
WHERE c.imdb_id IN ('tt6751668','tt14230458')
ON CONFLICT DO NOTHING;

-- Сериалы — присваиваем жанры в основном «Драма» и «Криминал»
INSERT INTO content_genres (content_id, genre_id)
SELECT c.id, g.id FROM content c
JOIN series s ON s.content_id = c.id
JOIN genres g ON g.slug IN ('drama','crime')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- content_persons: связь контента с актёрами/режиссёрами (use-case Этап 2 A)
-- =====================================================================
INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'DIRECTOR'
FROM content c JOIN persons p ON p.name = 'Кристофер Нолан'
WHERE c.imdb_id IN ('tt15398776','tt0468569')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'ACTOR'
FROM content c JOIN persons p ON p.name = 'Киллиан Мерфи'
WHERE c.imdb_id IN ('tt15398776')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'DIRECTOR'
FROM content c JOIN persons p ON p.name = 'Дени Вильнёв'
WHERE c.imdb_id IN ('tt15239678')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'ACTOR'
FROM content c JOIN persons p ON p.name = 'Тимоти Шаламе'
WHERE c.imdb_id IN ('tt15239678','tt14230458')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'ACTOR'
FROM content c JOIN persons p ON p.name = 'Эмма Стоун'
WHERE c.imdb_id IN ('tt14230458')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'DIRECTOR'
FROM content c JOIN persons p ON p.name = 'Йоргос Лантимос'
WHERE c.imdb_id IN ('tt14230458')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'ACTOR'
FROM content c JOIN persons p ON p.name = 'Брайан Крэнстон'
WHERE c.imdb_id IN ('tt0903747')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'DIRECTOR'
FROM content c JOIN persons p ON p.name = 'Винс Гиллиган'
WHERE c.imdb_id IN ('tt0903747')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'DIRECTOR'
FROM content c JOIN persons p ON p.name = 'Фрэнсис Форд Коппола'
WHERE c.imdb_id IN ('tt0068646')
ON CONFLICT DO NOTHING;

INSERT INTO content_persons (content_id, person_id, role)
SELECT c.id, p.id, 'ACTOR'
FROM content c JOIN persons p ON p.name = 'Аль Пачино'
WHERE c.imdb_id IN ('tt0068646')
ON CONFLICT DO NOTHING;
