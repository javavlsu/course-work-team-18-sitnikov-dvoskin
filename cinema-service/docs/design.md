# MovieHub — Design System

Единая точка для UI всего проекта. Все компоненты в `static/css/style.css` и шаблоны страниц ДОЛЖНЫ ссылаться сюда через CSS-переменные и описанные ниже паттерны. Хардкод цветов / радиусов / отступов / типошкалы в компонентах = баг.

Когда меняешь визуал (фон, акцент, бейдж, иконка), правь токены в `:root` (style.css, ~строка 108) — а не в каждом компоненте отдельно. Если нужного токена нет — ДОБАВЬ его в `:root` и используй ровно тот же токен везде.

Источник дизайна — Gladia (gladia.io): дарк-сёрфейс с фиолетовым акцентом #7B5CFF + cyan-подсветка #00D1FF, Inter Tight, pill-кнопки, full-bleed hero, обильные секции с одинаковым ритмом padding'ов. Это уже реализовано на главной (`index.html`) — её и тиражируем на остальные 20 страниц.

> **Префикс `.g-*` = Gladia namespace.** Все новые компоненты, которые соответствуют дизайн-системе, должны жить в этом неймспейсе. Старые классы (`.btn-*`, `.card`, `.hero-cinema`, `.content-detail-header`) остаются для обратной совместимости, но новый код пишется через `.g-*`.

---

## 1. Цвета

### 1.1 Поверхности (background)
| Token | Value | Использование |
|---|---|---|
| `--bg` | `#000000` | body, самый низ; `html { background-color:#000 }` |
| `--bg-elevated` | `#0a0a0c` | hero, фоновые секции, `.g-why-card`, CTA banner |
| `--bg-card` | `#0e0e10` | карточки контента, dropdowns, мега-меню |
| `--bg-card-hover` | `#141418` | hover карточек |
| `--bg-input` | `#111114` | inputs, selects, textarea |
| `--bg-nav` | `rgba(0, 0, 0, 0.65)` | floating nav-pill (с `backdrop-filter: blur(40px)`) |
| `--bg-pill-ghost` | `#252525` | filled-ghost кнопки (вторичный CTA в Gladia-style) |
| `--bg-pill-ghost-hover` | `#2e2e2e` | hover у ghost-pill |
| `--bg-glass` | `rgba(255, 255, 255, 0.03)` | едва заметная подсветка поверх dark surface (mega-item hover) |
| `--surface-tile` | `#1a1a1f` | нейтральный контейнер под иконкой (чуть светлее body) |

### 1.2 Бордеры (4 уровня — больше не заводить)
| Token | Value | Когда |
|---|---|---|
| `--border` | `rgba(255, 255, 255, 0.06)` | дефолт, разделители секций, сетка `.g-flow` |
| `--border-strong` | `rgba(255, 255, 255, 0.10)` | выделяющие границы карточек, инпуты, ghost-кнопки |
| `--border-stronger` | `rgba(255, 255, 255, 0.18)` | hover/focus, акцентные бордеры, стеклянные CTA |
| `--border-nav` | `#1f1f1f` | бордер nav-pill (Gladia-spec) |

Любая `rgba(255,255,255, 0.XX)` в компонентах — должна попадать в один из 4 уровней.

### 1.3 Текст
| Token | Value | Когда |
|---|---|---|
| `--text` | `#f5f5f7` | h1-h6, primary body, hover текст |
| `--text-secondary` | `#b9c0c6` | nav-links дефолт, body lead, footer links |
| `--text-muted` | `#8f969b` | мелкий вторичный (метаданные, captions, eyebrow, stat-label) |
| `--text-faint` | `#5b6066` | едва заметный (timestamps, system info, footer-col-title) |

### 1.4 Акцент (фиолетовый)
| Token | Value | Когда |
|---|---|---|
| `--accent` | `#7B5CFF` | главный акцент: `.g-accent` в hero-title, eyebrow chip dot, focus rings, иконки flow-step number |
| `--accent-hover` | `#8F73FF` | hover на accent-CTA |
| `--accent-deep` | `#5A3BFF` | глубокий tone (gradients, glow, аватары) |
| `--accent-soft` | `rgba(123, 92, 255, 0.14)` | подложки accent-elements (chip bg, CTA banner spotlight) |
| `--accent-strong` | `rgba(123, 92, 255, 0.4)` | акцентные бордеры |
| `--accent-glow` | `rgba(123, 92, 255, 0.45)` | text-shadow / drop-shadow glow на active иконках |

### 1.5 Cyan-подсветка (вторичный)
| Token | Value | Когда |
|---|---|---|
| `--cyan` | `#00D1FF` | редкий вторичный акцент — рейтинговые подложки, `badge-user` |
| `--cyan-soft` | `rgba(0, 209, 255, 0.12)` | подложка под cyan-метку |

**Правило:** purple появляется в 2-3 точках на экране. Если их больше — компоненты теряют акцентность. Иконки в мега-меню / why-cards / flow-steps НЕ должны быть фиолетовыми (это шум). Используй `--surface-tile` (нейтральный) — см. §6.

### 1.6 Семантические (status)
| Token | Value | Когда |
|---|---|---|
| `--success` | `#22c55e` | published, online |
| `--warning` | `#f59e0b` | draft, moderation, spoiler-tag |
| `--danger` | `#ef4444` | rejected, error, btn-outline-danger |
| `--info` | `#3b82f6` | информация |

Бейджи статусов (`.badge-draft/moderation/published/rejected`) используют bg @ 0.14 + border @ 0.35 от семантического цвета.

---

## 2. Типографика

Один шрифт на всё: **Inter Tight** (300-900). Сериф запрещён. Моно — `SF Mono` для tnum-цифр (mono-rating, table id, footer-meta).

```css
--font-sans:    'Inter Tight', 'Inter', -apple-system, sans-serif;
--font-display: 'Inter Tight', 'Inter', -apple-system, sans-serif;  /* alias */
--font-mono:    "SF Mono", ui-monospace, "Roboto Mono", monospace;
```

`body { font-size: 15px; line-height: 1.55; letter-spacing: -0.005em; font-feature-settings: "ss01", "cv11" }` — опентайп ss01/cv11 включены глобально, не надо их добавлять локально.

### 2.1 Шкала размеров

| Уровень | Размер | Weight | Letter-spacing | Line-height | Использование |
|---|---|---|---|---|---|
| `.g-hero-title` | `clamp(44px, 6.4vw, 84px)` | 600 | `-0.035em` | 1.04 | главный H1 на лендинге, max-width 880px, центр |
| `.g-section-head h2` | `clamp(32px, 4vw, 52px)` | 600 | `-0.03em` | 1.04 | заголовки крупных секций (`Сейчас смотрят`, `Топ по оценкам`) |
| H1 (стандарт) | `clamp(36px, 5vw, 56px)` | 700 | `-0.025em` | 1.05 | детальные страницы (контент, плейлист, рецензия) |
| H2 | `clamp(24px, 3vw, 32px)` | 700 | `-0.02em` | 1.1 | sub-секции |
| H3 | 22px | 600 | `-0.015em` | 1.1 | card-title, .g-flow h3 (20px), .g-why-card h3 (19px) |
| H4 | 18px | 600 | — | 1.1 | |
| H5 | 16px | 600 | — | 1.1 | |
| H6 | 14px | 600 | — | 1.1 | |
| `.g-hero-lead` | `clamp(16px, 1.3vw, 19px)` | 400 | — | 1.55 | подзаголовок hero, max-width 620px |
| `.lead` / `.g-section-sub` | `clamp(15px, 1.2vw, 17px)` | 400 | — | 1.55 | подписи к секциям |
| body | 15px | 400 | `-0.005em` | 1.55 | дефолт |
| small / meta | 12-13px | 500 | `0.02em` | 1.4 | meta-line, content-meta |
| eyebrow / label | 11px | 600 | `0.14em` UPPER | 1 | section-eyebrow, form-label, badge |
| micro-label | 10px | 700 | `0.18em` UPPER | 1 | footer-col-title, stat-label, meta-line |

`.g-accent` — inline `<span>` внутри hero-title для фиолетовой подсветки слова/строки. Использовать максимум 1 раз на H1.

### 2.2 Eyebrow-паттерны
- **`.g-eyebrow`** — uppercase tracking-wide, без pill-фона, для маркера секций. `font-size:11px; font-weight:600; letter-spacing:0.14em; text-transform:uppercase; color:var(--text-muted)`.
- **`.g-chip`** — то же, что eyebrow, но с pill-подложкой `--accent-soft` для hero-маркеров. На главной не используется (hero-title идёт без чипа), оставлен для inner-страниц.
- НЕ заводить новые `.xxx-badge` для eyebrow'ов. Только эти 2.

---

## 3. Spacing & sizing

### 3.1 Шкала отступов (4px-base)
```
--s-1: 4px   --s-2: 8px   --s-3: 12px  --s-4: 16px
--s-5: 24px  --s-6: 32px  --s-7: 48px  --s-8: 64px  --s-9: 96px
```

Любой margin/padding/gap в новом коде = один из этих 9 токенов. Между секциями — `var(--s-9)` (96px) или `clamp(56px, 8vw, 112px)` для responsive.

### 3.2 Радиусы (8 значений — не больше)
| Token | Value | Когда |
|---|---|---|
| `--r-sm` | `6px` | мелкие чипы, бейджи статусов |
| `--r` | `8px` | nav-link hover, dropdown items, `g-mega-item-icon` |
| `--r-lg` | `12px` | inputs, posters в гриде, small cards, `g-icon-tile lg` |
| `--r-card` | `14px` | content cards, playlist cards, hero playlist cover |
| `--r-xl` | `16px` | feature tiles, glass-cards, `.g-why-card`, mobile nav |
| `--r-nav` | `20px` | nav-pill, primary/ghost кнопки в Gladia-style |
| `--r-2xl` | `24px` | hero CTAs, `.g-cta-banner` |
| `--r-pill` | `999px` | sparkle-circle бренда, аватары, badge-pill, кнопки |

### 3.3 Контейнеры
```css
.g-container { max-width: 1240px; margin: 0 auto; padding: 0 clamp(20px, 4vw, 40px); }
```
- Дефолт всех секций — `.g-container` (1240px).
- Старые страницы используют bootstrap `.container` с `max-width: 1320px` (см. style.css:439). Постепенно переводить на `.g-container`.
- Variants для длинных текстов: `main.container.container-narrow { max-width: 920px }` (рецензия, профиль), `container-tight { max-width: 560px }` (login/register).

### 3.4 Section rhythm
```css
.g-section { padding: clamp(56px, 8vw, 112px) 0; }
```
Каждая логическая секция = `<section class="g-section g-reveal">` с `<div class="g-container">…</div>` внутри. На детальных страницах нижний padding половинный.

---

## 4. Background — Aurora

Глобальный анимированный фон, инжектится `partials.js` в `<body>` как `<div class="aurora">`. **Должен быть на каждой странице** — без него тёмные сёрфейсы выглядят плоскими.

DOM (генерится автоматически):
```html
<div class="aurora" aria-hidden="true">
  <div class="aurora-orb aurora-orb--1"></div>  <!-- purple, 70vmax -->
  <div class="aurora-orb aurora-orb--2"></div>  <!-- cyan, 65vmax -->
  <div class="aurora-orb aurora-orb--3"></div>  <!-- pink, 50vmax -->
  <div class="aurora-orb aurora-orb--4"></div>  <!-- violet, 55vmax -->
  <div class="aurora-conic"></div>              <!-- rotating prismatic wash, 80s -->
  <div class="aurora-grid"></div>               <!-- 60px grid drift -->
  <div class="aurora-noise"></div>              <!-- SVG fractalNoise grain -->
  <div class="aurora-vignette"></div>           <!-- edge darkening -->
</div>
```

Слои (back→front): orbs → conic → grid → noise → vignette. `position: fixed; inset: 0; z-index: -1; pointer-events: none; opacity: 0.55`.

Все анимации (orb-1..4, conic-rotate, grid-drift) длительностью 22-80s, `cubic-bezier(0.45, 0.05, 0.55, 0.95)`. Под `prefers-reduced-motion: reduce` отключаются автоматически.

**Не дублируй** orb'ы внутри секций — они уже фоном. Локальные glow-effects (hero conic-gradient, CTA radial spotlight) можно, но они скрыты `display:none` в текущей версии — фон делает основную работу.

---

## 5. Layout: страницы

### 5.1 Скелет страницы
```html
<body>
  <header data-include="navbar"></header>     <!-- partials/navbar.html -->
  <main>
    <section class="g-hero">…</section>        <!-- если лендинг -->
    <section class="g-section g-reveal">…</section>
    <section class="g-section g-reveal">…</section>
    …
  </main>
  <footer data-include="footer"></footer>     <!-- partials/footer.html -->
  <script src="/js/auth.js"></script>
  <script src="/js/api.js"></script>
  <script src="/js/ui.js"></script>
  <script src="/js/partials.js"></script>     <!-- инжектит nav/footer/aurora -->
  <script src="/js/page-XXX.js"></script>
</body>
```

`main { padding: 0 }` — main сам не задаёт ширину/паддинги, всё делают секции через `.g-container`.

### 5.2 Цельные responsive-точки
- Mobile: < 768px
- Tablet: 768-991px (`@media (min-width: 768px)` для 4-up grids)
- Desktop: ≥ 992px (`@media (min-width: 992px)` для admin sidebar, mega-menu)
- Wide: ≥ 1200px (катало-грид 6 колонок)

Логотипы в маркизе и стат-строка ломаются на 768px (4 → 2 колонки).

---

## 6. Иконки в плашках (`.g-icon-tile` pattern)

Каждая SVG-иконка в UI обёрнута в квадратную плашку. Дизайн-цель — нейтральная, не фиолетовая, чтобы не конкурировать с акцентом.

```css
.g-icon-tile {
  display: grid;
  place-items: center;
  background: var(--surface-tile);          /* НЕ фиолетовый */
  border: 1px solid var(--border-strong);
  border-radius: var(--r-lg);
  color: var(--text);                       /* line-SVG, currentColor */
}
.g-icon-tile--sm { width: 32px; height: 32px; border-radius: var(--r); }   /* мега-меню */
.g-icon-tile--md { width: 36px; height: 36px; border-radius: var(--r); }   /* flow-steps */
.g-icon-tile--lg { width: 44px; height: 44px; border-radius: var(--r-lg); } /* why-cards */

/* Акцентный вариант — использовать ≤2 раз на странице */
.g-icon-tile--accent {
  background: var(--accent-soft);
  border-color: var(--accent-strong);
  color: var(--accent);
}
```

В index.html этот паттерн применён в `.g-mega-item-icon` (32px), `.g-flow-icon` (36px), `.g-why-icon` (44px). Иконки — line SVG `stroke="currentColor"` `stroke-width="1.5"` `viewBox="0 0 24 24"`, размер 14-16px.

---

## 7. Navbar (Gladia floating pill)

Реализация: `partials/navbar.html` + `.g-nav*` в style.css. Полный состав:

```
[brand-circle MovieHub]   [Каталог▾ · Подборки▾ · Поиск]   [Войти] [Регистрация-pill]
```

- **Контейнер**: `position: sticky; top: 16px; z-index: 1000; max-width: 1240px; padding: 8px 21px 8px 25px`. `background: var(--bg-nav); backdrop-filter: blur(40px) saturate(180%); border: 1px solid var(--border-nav); border-radius: var(--r-nav)`.
- **Brand**: 26px белый кружок (`--r-pill`) + sparkle SVG (4-конечная звезда `M12 1L13.8 9.2…`) + wordmark `MovieHub` 17px font-weight 600. Hover: круг поворачивается на 45°.
- **Mega-menu**: 720px шириной (520px для narrow), 3-колоночная сетка с `g-mega-label` метками + `g-mega-item` (icon-tile + title + desc). Появляется на hover с translateY(-6→0) 200ms + invisible bridge `::before` чтобы курсор не выпадал между линком и панелью.
- **CTA**: `Войти` — `.g-nav-link.g-nav-link--cta` (text-link), `Регистрация` — `.g-btn.g-btn-primary.g-btn-sm` (белая pill).
- Авторизованный юзер: `.g-nav-user` (pill с аватаркой + username) + Bootstrap dropdown `.g-nav-dropdown` (профиль / рецензии / подборки / админ / выйти).
- Mobile (≤991px): toggle-кнопка `.g-nav-toggle` (3 полоски), меню collapse'ится в колонку, mega превращается в плоский список.

Переиспользуй partial as-is. Не клонируй nav на странице — он инжектится автоматически через `<header data-include="navbar"></header>` + `partials.js`.

---

## 8. Кнопки

### 8.1 `.g-btn` (Gladia-style — основная)
| Класс | bg | color | border | radius |
|---|---|---|---|---|
| `.g-btn-primary` | `#fff` | `#0d0d0d` | none | `var(--r-pill)` |
| `.g-btn-ghost` | `var(--bg-pill-ghost)` | `#fff` | `1px solid var(--border-strong)` | `var(--r-pill)` |

Размеры:
- default: `padding: 10px 18px; font-size: 15px; font-weight: 400`
- `.g-btn-sm`: `padding: 6px 14px; font-size: 13px`

**Правила:**
- font-weight: `400` (НЕ bold)
- никаких декоративных стрелок (`→`) внутри кнопок — текст голый
- никакого border-color change на hover — только background
- активное состояние: `transform: scale(0.98)`

### 8.2 Bootstrap `.btn-*` (legacy)
Используется на старых страницах. Сохранён, но выровнен по токенам:
- `.btn-primary` / `.btn-light` — белая pill, `border-radius: var(--r-pill)`, hover `translateY(-1px)` + soft shadow
- `.btn-outline-*` — стеклянная (rgba 0.04 + border-strong + blur 10px)
- `.btn-circle` — 44px круг с blur
- `.btn-outline-gold` (alias `--gold = --accent`) — фиолетовый ghost

При переписывании страницы — заменяй `.btn` на `.g-btn` где можно. На детальных hero (`.detail-actions`) кнопки 48px высотой с `border-radius: 999px`.

---

## 9. Hero (`<section class="g-hero">`)

Композиция как на главной — центрированный титульник + lead + CTA + stat-strip:

```html
<section class="g-hero">
  <div class="g-container">
    <h1 class="g-hero-title g-reveal">
      Найди кино, на которое<br>
      <span class="g-accent">не жалко вечера.</span>
    </h1>
    <p class="g-hero-lead g-reveal">…</p>
    <div class="g-cta-row g-reveal">
      <a href="/search" class="g-btn g-btn-primary">Найти своё кино</a>
      <a href="/movies" class="g-btn g-btn-ghost">Открыть каталог</a>
    </div>
    <ul class="g-stat-row g-reveal">
      <li><span class="g-stat-num">200+</span><span class="g-stat-label">Фильмов</span></li>
      …
    </ul>
  </div>
</section>
```

Спецификация:
- `.g-hero { padding: clamp(80px, 11vw, 160px) 0 clamp(48px, 6vw, 80px); text-align: center; position: relative; isolation: isolate }` — псевдоэлементы `::before/::after` зарезервированы под локальные projector cone + grain (сейчас `display:none`, фон Aurora справляется).
- `.g-hero-title` см. §2.1 (clamp 44-84px, weight 600, ls -0.035, max-width 880px).
- `.g-hero-lead` 16-19px, color secondary, max-width 620px, `margin: 0 auto 36px`.
- `.g-cta-row { display:flex; flex-wrap:wrap; justify-content:center; gap:12px }`.
- `.g-stat-row` — 4-up grid (2-up на mobile), `border-top/bottom: 1px solid var(--border)`, padding 28px 0, max-width 1080px. `.g-stat-num` 28-38px font-weight 600 ls -0.025; `.g-stat-label` 11px upper tracking 0.14em color muted.

Внутренние страницы могут использовать meньший hero — компактный заголовок + lead без CTA-row, но та же типошкала.

---

## 10. Sections

Каждая секция = `<section class="g-section g-reveal">` + `<div class="g-container">`. Заголовок секции через `.g-section-head`:

```html
<section class="g-section g-reveal">
  <div class="g-container">
    <header class="g-section-head">
      <div><h2>Сейчас смотрят</h2></div>
      <a href="/movies" class="g-link">Открыть каталог</a>
    </header>
    <div class="row g-3 g-md-4">…карточки…</div>
  </div>
</section>
```

- `.g-section-head { display:flex; align-items:end; justify-content:space-between; flex-wrap:wrap; gap:16px; margin-bottom: clamp(24px, 3vw, 40px) }`
- `.g-section-head h2` — clamp 32-52px (см. §2.1).
- `.g-section-head--centered` — column flex, для CTA-banner и feature-grids с центрованным текстом + `.g-section-sub` под заголовком.
- `.g-link` — текстовая ссылка `font-size:14px font-weight:500 color:var(--text-secondary) border-bottom:1px solid var(--border-stronger) padding:8px 0`. Hover: color `#fff` + border `#fff`. Без стрелок.

---

## 11. Logos marquee (`.g-logos`)

3 ряда бесконечной ленты студийных лого (на главной — 15 студий: Universal, Paramount, Sony, A24, Disney и т.д.).

```html
<section class="g-section g-logos g-reveal">
  <div class="g-container">
    <span class="g-logos-label">В каталоге &middot; от блокбастеров до фестивального арт-хауса</span>
    <div class="g-logos-marquee">
      <ul class="g-logos-row g-logos-row--1">
        <li class="g-logos-track"><img src="/img/studios/x.svg" alt="…"></li>
        <li class="g-logos-track" aria-hidden="true">…дубликат для seamless loop…</li>
      </ul>
      <ul class="g-logos-row g-logos-row--2">…</ul>
      <ul class="g-logos-row g-logos-row--3">…</ul>
    </div>
  </div>
</section>
```

- `.g-logos-marquee` — `mask-image: linear-gradient(90deg, transparent 0%, #000 8%, #000 92%, transparent 100%)` — края тают.
- `.g-logos-track` — `min-width: 100%; gap: clamp(40px, 5vw, 72px); animation: marquee linear infinite`. Дублируется внутри ряда → `translate3d(0 → -50%)`.
- Скорости: row-1 70s, row-2 90s reverse, row-3 60s.
- `img` — `height: clamp(22px, 2.2vw, 32px); filter: brightness(0) invert(1); opacity: 0.55` (на hover 1).
- `prefers-reduced-motion: reduce` → анимация выключена.

Изображения — локальные SVG в `/img/studios/`. Фолбэков нет, файлы должны существовать.

---

## 12. Карточки

### 12.1 Poster card (`.poster` + `.content-card`)
Для гридов фильмов/сериалов. Соотношение 2:3, `border-radius: var(--r-lg)` (12px).

```html
<a class="content-card" href="/content/123">
  <div class="poster">
    <img class="poster-img" src="…" alt="…">
    <span class="content-type-badge">Фильм</span>
    <span class="rating-badge">8.4</span>
  </div>
  <div class="content-card-body">
    <div class="content-title">Inception</div>
    <div class="content-meta-line">2010 · Триллер · США</div>
  </div>
</a>
```

- Постер: `aspect-ratio: 2 / 3; box-shadow: 0 6px 18px rgba(0,0,0,0.4)`. Hover: `translateY(-4px) + box-shadow с purple-glow rgba(123,92,255,0.22)`.
- Title — ОДНА строка ellipsis (`white-space:nowrap; overflow:hidden; text-overflow:ellipsis; height:1.35em`). Никаких 2-строчных clamp — карточки одинаковой высоты.
- Meta-line — тоже одна строка, 12px muted, цифры с `font-feature-settings:"tnum"`.
- Fallback (нет постера): `.poster-fallback` — gradient + крупная типографика с `mark + title + year` поверх паттерна.
- Размеры: дефолт (грид) | `.poster-row` (80px широкий, для списков плейлиста) | `.poster-lg` (320px, для hero-detail).

### 12.2 Rating badge (`.rating-badge`)
Letterboxd-style: тонкая ★ + цифра в bottom-left угле постера, без чипа/бордера. Подложка — radial-gradient клин с `backdrop-filter: blur(10px) saturate(140%)` через `mask-image` той же радиальной формы.

Размеры: дефолт (13px) | `.rating-lg` (18px, для hero) | `.rating-xl` (28px, для billboard).

Цветовая логика рейтинга (применяется к `.rating-cluster .big` на детальной): high (`#4ade80`, ≥7) / mid (`#fbbf24`, 5-6.9) / low (`#f87171`, <5).

### 12.3 Content type badge
Top-left угол постера: `Фильм` / `Сериал`, 9px upper tracking 0.14em, `bg: rgba(0,0,0,0.7); backdrop-filter: blur(6px); border-radius: var(--r-sm)`.

### 12.4 Playlist card (`.playlist-card`)
16:9 cover + meta-row снизу.

- `.playlist-cover` — gradient-fallback с per-id hue (`--hue: ${(id*47)%360}deg`), на нём radial-gradients из purple/cyan для разнообразия.
- `.playlist-cover-overlay` — title + byline overlay внутри cover, gradient scrim снизу для читаемости (для img — обязателен, для fallback — выключен).
- `.playlist-cover-real-mosaic` — 2x2 grid из реальных постеров когда обложка собирается из items.
- Hover: `translateY(-4px)` + border `rgba(123,92,255,0.3)` + light purple bg overlay.

### 12.5 Why card (`.g-why-card`)
4-колоночная feature grid. `bg: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--r-xl); padding: 28px 24px`. Иконка через `.g-icon-tile--lg`. Hover: `translateY(-4px)` + purple border + glow shadow.

### 12.6 Flow step (`.g-flow li`)
4-колоночный numbered workflow. Сетка с `gap: 1px; background: var(--border); border-radius: var(--r-xl); overflow: hidden` — даёт hairline-разделители без двойных бордеров. `.g-flow-step` — accent-цветный mono-номер. `.g-flow-icon` — `.g-icon-tile--md`.

---

## 13. Бейджи и чипы

3 паттерна, не больше:

1. **`.g-eyebrow`** — uppercase tracking-wide, без pill-фона, для маркера секции.
2. **`.g-chip`** — pill-форма с подложкой `--accent-soft`, для hero-eyebrow с пульсирующей точкой (на главной не используется).
3. **`.moderation-badge` / `.badge-*`** — для админских статусов (Draft / Moderation / Published / Rejected / Hidden / Deleted / Admin / User / Guest). Bg @0.14, border @0.35 от семантического цвета.

Не заводить кастомные `.xxx-badge`. `tag-badge` (для жанров) живёт отдельно — pill `padding: 6px 12px; bg: rgba(255,255,255,0.04); border-strong; font-size: 11px upper tracking 0.04em`. На hover превращается в фиолетовый.

---

## 14. Forms

```css
.form-control, .form-select, textarea {
  background: var(--bg-input);
  border: 1px solid var(--border-strong);
  border-radius: var(--r-lg);
  font-size: 14px;
  padding: 11px 14px;
}
:focus { border-color: var(--accent); box-shadow: 0 0 0 3px var(--accent-soft); }
```
- `.form-control-lg` — 14px 18px / 16px font-size.
- `.form-label` — 11px font-weight 600 upper tracking 0.12em color secondary.
- `.form-help` — 12px muted.
- Checkbox/radio checked → `--accent` bg + border.
- `.is-invalid` → border `--danger`. `.invalid-feedback` — `#f87171` 12px.

---

## 15. Footer (`site-footer`)

Letterboxd-style multi-column.

```
[Brand + tagline]   [Каталог]   [Сообщество]   [Аккаунт]
————————————————————————————————————————————————
© 2026 MovieHub
```

- `border-top: 1px solid var(--border); background: linear-gradient(180deg, transparent, rgba(255,255,255,0.012) 30%), #08090b; padding: 64px 0 32px; margin-top: 96px`.
- Brand берёт `g-nav-brand` (тот же sparkle + wordmark, 20px).
- `.footer-tagline` — 13px muted line-height 1.55 max-width 280px.
- `.footer-col-title` — 10px upper tracking 0.18em color faint.
- `.footer-links a` — 14px secondary, hover → accent.
- `.footer-bottom` — 12px faint, разделитель сверху, `font-feature-settings:"tnum"` для года.

`data-auth-guest` / `data-auth-user` — переключатели для login/register vs profile/logout, обрабатываются `auth.js`.

---

## 16. Motion

### 16.1 Easing
```css
--ease:     cubic-bezier(0.2, 0.8, 0.2, 1);     /* стандарт, 120-200ms */
--ease-out: cubic-bezier(0.22, 1, 0.36, 1);     /* выраженный exit */
```

Длительности:
- Hover на ссылках/иконках — 160ms
- Карточки (lift + shadow) — 220-280ms
- Modal/dropdown появление — 200ms
- Scroll reveal — 700ms
- Marquee — 60-90s
- Aurora orbs — 28-48s

### 16.2 Scroll reveal (`.g-reveal`)
```css
.g-reveal { opacity: 0; transform: translateY(24px); transition: opacity 700ms var(--ease-out), transform 700ms var(--ease-out); }
.g-reveal.is-visible { opacity: 1; transform: translateY(0); }
```

Активируется через `IntersectionObserver` (rootMargin -10%, threshold 0.08). Реализация — `initRevealObserver()` в `page-index.js:340`. Перенести в `ui.js` как глобальный init и вызывать на каждой странице.

Ставить `.g-reveal` на каждый top-level блок: hero-title, hero-lead, cta-row, stat-row, секции целиком.

### 16.3 Stat count-up
`initStatCountUp()` (page-index.js:377) — IntersectionObserver на `.g-stat-num`, ease-out cubic 1400ms от 0 до значения, форматирование через `replace(/\B(?=(\d{3})+(?!\d))/g, ' ')` (1234 → "1 234"). Сохраняет суффикс `+`.

### 16.4 Prefers-reduced-motion
Все анимации (aurora, marquee, hero pseudo-elements, reveal) отключаются через `@media (prefers-reduced-motion: reduce)`. Не забывать оборачивать новые анимации.

---

## 17. States: empty / error / skeleton

### 17.1 Empty state
```html
<div class="empty-state">
  <div class="empty-state-icon">📺</div>
  <h3>Скоро здесь будет хит проката</h3>
  <p>…объяснение…</p>
  <a href="…" class="g-btn g-btn-primary">Перейти в каталог</a>
</div>
```
`.empty-state { text-align: center; padding: 64px 32px; border: 1px dashed var(--border-strong); border-radius: 16px; background: radial-gradient + var(--bg-card) }`. h3 — 22px font-weight 700.

### 17.2 Error state
`.empty-state.error-state` — border-color `rgba(239,68,68,0.3)`, bg `rgba(239,68,68,0.04)`, иконка красная.

### 17.3 Skeleton
`.skeleton { background: linear-gradient(90deg, rgba(255,255,255,0.04), rgba(255,255,255,0.08), rgba(255,255,255,0.04)); background-size: 800px 100%; animation: shimmer 1.4s infinite }`. Используется через `UI.skeletonGrid(count)` (см. ui.js).

Helpers в page-*.js:
```js
function showSkeleton(targetId, count = 6) { … UI.skeletonGrid(count) }
function showError(targetId, retry) { … UI.errorState({ onRetry: retry }) }
function showEmpty(targetId, opts) { … UI.emptyState(opts) }
```

---

## 18. Карта применения паттернов на страницах

Каждая страница берёт ровно те `.g-*` блоки, которые нужны. Запрещено вводить новые композиционные паттерны — только переиспользовать.

| Файл | Hero | Sections | Карточки | Спец |
|---|---|---|---|---|
| `index.html` | `.g-hero` (full) + stat-row | 4× `.g-section` | content-card grid + playlist-card | logos-marquee |
| `movies.html` / `series.html` | компактный `.g-section` с H1+lead | toolbar (`.g-chip` фильтры) + грид | content-card | пагинация |
| `search.html` | `.g-hero` мини (H1 + search input) | результаты в 6-col grid | content-card | empty-state при no-results |
| `content-detail.html` | `.detail-billboard` (blurred backdrop) | `.g-section` для актёров/похожих/рецензий | poster-lg + rating-cluster + rail | review-card |
| `login.html` / `register.html` | по центру `container-tight` (560px) | одна форма-карточка | — | aurora фон |
| `profile.html` / `user-profile.html` | `.user-avatar.user-avatar-lg` + H1 | `.g-section` со статами через `.dashboard-stat` | content-card в подборках | stat-row |
| `my-reviews.html` | `.g-section-head` H1 + tab-фильтр | список `.review-card` | — | empty-state |
| `review-create.html` / `review-edit.html` | `container-narrow` (920px) | большой title input + textarea | — | star-rating |
| `review-detail.html` | `container-narrow` long-form | тело рецензии | — | author + score-pill |
| `my-playlists.html` | `.g-section-head` H1 + create-CTA | grid playlist-card | playlist-card | empty-state |
| `playlist-create.html` / `playlist-edit.html` | `container-narrow` form | inputs + cover-uploader | — | track-list reorder |
| `playlist-detail.html` | `.playlist-hero` (320px square + meta) | `.playlist-item-row` track list | row poster (80px) | share-actions |
| `admin.html` | `.admin-layout` + `.admin-sidebar` (240px) | dashboard tiles | `.dashboard-stat` карточки | stats grid |
| `admin-content.html` / `admin-users.html` / `admin-reviews.html` | `.admin-page-head` H1 + actions | sticky-thead `.table` | — | moderation-badge + bulk actions |

**Правила перевода старой страницы на стиль главной:**
1. Замени `<header>` / `<footer>` старой разметки на `<header data-include="navbar">` / `<footer data-include="footer">`.
2. Оберни все блоки в `<section class="g-section g-reveal"><div class="g-container">…</div></section>`.
3. Переведи H1/H2 на g-* типошкалу (`.g-hero-title` для hero, `.g-section-head h2` для секций).
4. `.btn` → `.g-btn-primary` / `.g-btn-ghost` где это CTA. Старые `.btn-*` оставь только в формах/таблицах.
5. Проверь: иконки в плашках = `.g-icon-tile--*` (нейтральные, не фиолетовые).
6. Добавь `.g-reveal` на секции и инициализируй observer через `ui.js`.
7. Цвета/радиусы/отступы — только токены `var(--*)`, никаких хардкодов.

---

## 19. Анти-паттерны

❌ Не делай:
- Новый `border-radius: NNpx` вне 8 значений из §3.2.
- Новый цвет `#xxxxxx` или `rgba(255,255,255,0.NN)` вне токенов.
- Bold font-weight (700-800) на кнопках. Только regular (400) или semi-bold (500-600).
- Стрелки `→`, ▾ caret-у текстом, иконки внутри кнопок (Gladia/Resend ground truth — голый текст).
- 2+ purple элементов в одном flow подряд (см. §1.4).
- Аватары/badges с фиолетовым bg по умолчанию (только role-admin).
- Кастомные `.xxx-badge` для статусов — используй существующие из §13.
- Локальные orb'ы / gradient-spotlights в секциях (фон Aurora их перекрывает, дублирование = шум).
- Padding'и/margin'ы вне шкалы `--s-*`.
- Дополнительные шрифты (Roboto, Manrope, etc) — только Inter Tight.
- Дублирование nav/footer в HTML — только через `data-include`.
- Hover'ы которые двигают элемент сильнее чем `translateY(-4px)`.
- 2-строчные ellipsis на карточках в гриде — только 1 строка через `white-space:nowrap` (карточки одинаковой высоты).

✅ Делай:
- Один токен → много мест. Меняешь визуал = правишь `:root`.
- Любая новая SVG-иконка обёрнута в `.g-icon-tile--*`.
- Любая секция = `.g-section.g-reveal` + `.g-container`.
- Любая интерактивность под `prefers-reduced-motion: reduce`.
- При сомнении — открой `index.html` и скопируй паттерн.

---

## 20. Когда добавлять токен / класс

✅ Добавляй, если:
- Значение используется 2+ раза.
- Значение семантично (`--bg-nav`, `--surface-tile`).
- Это новый layout-паттерн, который понадобится на 2+ страницах (тогда заводи `.g-*` класс).

❌ Не добавляй:
- One-off значение для одной страницы — используй ближайший существующий токен.
- Класс-обёртку, которая всего лишь переставляет два существующих класса — лучше inline-комбинация.

---

## 21. Связанные файлы

- `static/css/style.css` `:root` (~строка 108) — единственное место с токенами.
- `static/css/style.css` `g-*` namespace (строки 3540-4402) — все Gladia-компоненты.
- `static/css/style.css` aurora (строки 200-353) — фоновая атмосфера.
- `static/index.html` — главная, эталонная страница (hero + 5 секций).
- `static/partials/navbar.html` — nav, использует `.g-nav-*` + `.g-btn-*`.
- `static/partials/footer.html` — footer, использует `.site-footer`.
- `static/js/partials.js` — инжект nav/footer/aurora в DOM, эмитит `partials:ready`.
- `static/js/ui.js` — `UI.contentCard`, `UI.skeletonGrid`, `UI.emptyState`, `UI.errorState`, `UI.escapeHtml`, `UI.formatRating`, `UI.urlForContent`, `UI.pluralize`, `UI.firstChar`, `UI.unwrapRecs`.
- `static/js/page-index.js` — эталонная page-script: load + skeleton + render + reveal + count-up.

При сомнении смотри `index.html` + `page-index.js` — это эталон.
