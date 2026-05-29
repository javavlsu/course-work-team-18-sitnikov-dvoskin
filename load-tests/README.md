# JMeter Load Tests — MovieHub

Apache JMeter сценарии нагрузочного тестирования REST API курсовой `cinema-service`.

## Установка

```bash
brew install jmeter            # macOS
# либо: https://jmeter.apache.org/download_jmeter.cgi
jmeter --version               # проверка
```

JMeter поставляется с встроенными HTML-генератором отчётов (`-e -o`),
поэтому никаких дополнительных плагинов не требуется.

## Структура

```
load-tests/
├── plans/                       # .jmx тест-планы
│   ├── catalog.jmx              # GET /api/v1/content (50 vu, 90s)
│   ├── search.jmx               # GET /api/v1/search?q=... (30 vu, 90s, CSV)
│   ├── auth.jmx                 # POST /register + /login (10 vu, 30s, BCrypt)
│   ├── review-create.jmx        # setUp(login) → POST /reviews (10 vu, 60s, Bearer)
│   ├── spike.jmx                # 1 → 200 vu за 30s, hold 60s
│   └── stress.jmx               # 0 → 100 vu за 5 мин, hold 60s
├── data/
│   └── search-queries.csv       # 30 query'ев для CSVDataSet (RU + EN)
├── results/                     # сюда падают .jtl + HTML отчёты
└── run-all.sh                   # последовательный прогон + HTML-репорт
```

## Запуск

### Один сценарий

```bash
cd load-tests
jmeter -n \
       -t plans/catalog.jmx \
       -l results/catalog.jtl \
       -e -o results/catalog-html
open results/catalog-html/index.html
```

### Все сценарии последовательно

```bash
bash load-tests/run-all.sh
# → results/<TS>/{catalog,search,auth,review-create}.jtl + HTML
```

### Параметризация

Все планы принимают `-J` параметры:

| Параметр | Дефолт | Что делает |
|---|---|---|
| `host` | `localhost` | хост бэкенда |
| `port` | `8080` | порт бэкенда |
| `threads` | план-специфично | число виртуальных пользователей |
| `rampup` | план-специфично | секунды на разгон (linear ramp-up) |
| `duration` | план-специфично | секунды активной нагрузки |
| `queries` | `data/search-queries.csv` | путь к CSV для search.jmx |

Примеры:

```bash
# Сжатый smoke catalog: 10 vu, 30s
jmeter -n -t plans/catalog.jmx -l results/smoke.jtl -e -o results/smoke-html \
       -Jthreads=10 -Jrampup=10 -Jduration=30

# Прогон против стенда
HOST=stage.example.com PORT=80 bash run-all.sh

# Запустить только catalog + spike
PLANS="catalog spike" bash run-all.sh
```

## Сценарии

| Файл | Vu | Длительность | Описание |
|---|---|---|---|
| `plans/catalog.jmx` | 50 | 30s ramp + 60s hold = 90s | GET `/api/v1/content?page={0..5}&size=20` |
| `plans/search.jmx` | 30 | 30s ramp + 60s hold = 90s | GET `/api/v1/search?q={query}` (CSV) |
| `plans/auth.jmx` | 10 | 15s ramp + 15s hold = 30s | POST `/register` + `/login` (BCrypt тяжёлый) |
| `plans/review-create.jmx` | 10 | 20s ramp + 40s hold = 60s | setUp: register+login → авторизованный POST `/reviews` |
| `plans/spike.jmx` | 1→200 | 30s ramp + 60s hold + 30s rampdown = 2m | resilience-тест |
| `plans/stress.jmx` | 0→100 | 5m ramp + 60s hold = 6m | прогрессивный стресс |

### Assertions (thresholds)

В каждом плане прицеплено:

* **ResponseAssertion** на ожидаемый HTTP статус (`200/201/404/409` для разных endpoints)
* **DurationAssertion** p95-friendly: `< 500 ms` для catalog/search,
  `< 1500 ms` для auth (BCrypt медленный — это норма),
  `< 800 ms` для review-create

Если сэмпл нарушает ассерт — он помечается как `success=false` и попадает в Errors%.

## Метрики первого прогона (smoke, 03 May 2026)

Прогон против `localhost:8080`, БД пустая (нет seed):

| Сценарий | Vu | Длит | Запросов | Throughput | Avg | p95 | Errors% |
|---|---:|---:|---:|---:|---:|---:|---:|
| `catalog` | 10 | 30s | 545 | **18.2 req/s** | 5.4 ms | 10.7 ms | 0.0 % |
| `search` (24 разных q) | 5 | 20s | 134 | 7.1 req/s | 6.6 ms | 10.0 ms | 0.0 % |
| `auth` (register+login) | 5 | 15s | 63 | 4.4 req/s | **79.8 ms** | 95.0 ms | 0.0 % |

**Выводы:**

* **catalog/search** — латентность отличная (5–8 ms). Бэк не нагружен,
  потому что таблица `content` пустая — Hibernate возвращает пустой Page
  по индексу `idx_content_status`.
* **auth** — `/register` и `/login` дают ~80 ms каждый: это полностью BCrypt
  (`spring.security.bcrypt.strength=10` по умолчанию). На M5 Max это нижняя граница;
  на CI-машине ожидаем 150–250 ms.
* Ошибок в auth ноль после фикса коллизий username (используем
  `JSR223 PreProcessor` с `nanoTime + Random.nextInt`).

Сырьё лежит в `results/smoke-{catalog,search,auth}.jtl` и HTML-дашборды
в `results/smoke-{catalog,search,auth}-html/index.html`.

## Просмотр HTML-отчёта

```bash
open load-tests/results/smoke-catalog-html/index.html
```

Дашборд содержит: Statistics summary, Response times over time,
Active threads over time, Throughput vs threads, error breakdown,
APDEX по transactions.

## Известные ограничения / TODO

* **БД пустая.** Catalog/search/content отдают пустые списки — это завышает
  RPS и занижает латентность по сравнению с production-ситуацией.
  Перед серьёзным прогоном засеять каталог через `/api/v1/admin/content`
  (минимум 50 контентов + 200 ratings).
* **review-create** при пустой БД вернёт 404 на `contentId=1` —
  это OK (мы измеряем латентность роута, ассерт допускает 404).
  После seed — поправить body, чтобы `contentId` существовал.
* **spike + stress** не входят в `run-all.sh` по умолчанию (длинные:
  2m и 6m). Запускать вручную: `jmeter -n -t plans/spike.jmx ...`.
* **Distributed mode (master/slaves)** не настроен — для локального
  стенда не нужен. Если бэкенд будет на удалённом VPS, может потребоваться
  master-slave для генерации >500 vu.

## Почему JMeter, а не k6 / wrk / Gatling

* **Академический стандарт** — Apache JMeter упоминается в большинстве
  методичек по тестированию ПО (включая нашу).
* **GUI для просмотра .jmx** — преподавательница может открыть план
  через `jmeter -t plans/catalog.jmx` (без `-n`) и посмотреть конфигурацию
  визуально, без чтения XML.
* **HTML-отчёты из коробки** (`-e -o`) — не нужно собирать графики Grafana,
  как для k6.
* **CLI режим (`-n`)** даёт reproducible runs для CI, выходные .jtl
  можно скормить любому JMeter parser'у.
