/**
 * UI helpers — рендеринг карточек, постеров с fallback, скелетонов, empty/error states.
 *
 * Pattern: typographic poster fallback (Spotify default cover), skeleton shimmer
 * (Letterboxd grid loading state), empty state с CTA (Linear empty list).
 */
(function (global) {
  'use strict';

  // ===== Утилиты =====

  function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function firstChar(str) {
    if (!str) return '?';
    const trimmed = String(str).trim();
    return trimmed ? trimmed.charAt(0).toUpperCase() : '?';
  }

  // Детерминированный hue 0..359 от строки (username/id) — для аватаров
  // и плейлист-обложек. Хеш djb2-style, но с простым mod 360. Один и тот же
  // username всегда даёт одинаковый цвет.
  function avatarHue(seed) {
    if (!seed) return 240;
    const s = String(seed);
    let h = 0;
    for (let i = 0; i < s.length; i++) {
      h = ((h << 5) - h + s.charCodeAt(i)) | 0;
    }
    return Math.abs(h) % 360;
  }

  // HTML-готовый аватар-инлайн. Передаёт `--avatar-hue` через style,
  // CSS использует его для hsl-фона. extraClass / extraAttrs — для
  // вариантов размера / aria.
  function avatarHtml(username, opts = {}) {
    const { extraClass = '', extraAttrs = '' } = opts;
    const initials = firstChar(username);
    const hue = avatarHue(username);
    const cls = `user-avatar ${extraClass}`.trim();
    return `<span class="${cls}" style="--avatar-hue:${hue}" ${extraAttrs}>${initials}</span>`;
  }

  // Брендовая искра — единственная иконка-индикатор оценки во всём UI.
  // Та же геометрия, что у g-brand-mark в navbar/footer (4-конечный sparkle,
  // пропорции вытянуты по вертикали). Используется в .rating-badge на постерах,
  // в .score-pill, в кнопках 1..10 модалки оценки и т.п.
  function iconSparkle(opts = {}) {
    const { size = 14, className = 'icon-sparkle' } = opts;
    return `<svg class="${className}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" focusable="false">`
      + `<path d="M12 1L13.8 9.2L22 11L13.8 12.8L12 21L10.2 12.8L2 11L10.2 9.2Z"/>`
      + `</svg>`;
  }

  // Сердечко-индикатор лайков — линейный outline вместо Unicode «♥»,
  // чтобы быть в одной графической системе с .icon-sparkle и .g-icon-tile.
  function iconHeart(opts = {}) {
    const { size = 14, className = 'icon-heart' } = opts;
    return `<svg class="${className}" width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">`
      + `<path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>`
      + `</svg>`;
  }

  // 5 кнопок 1..5 для модалок «Поставь оценку» (.star-rating).
  // Брендовая искра вместо Unicode ★. Используется на content-detail,
  // review-create, review-edit — единая геометрия и поведение.
  function starRatingTemplate(opts = {}) {
    const { max = 5 } = opts;
    let html = '';
    for (let v = 1; v <= max; v++) {
      html += `<button type="button" data-v="${v}" aria-label="Оценка ${v} из ${max}">${iconSparkle({ size: 22 })}</button>`;
    }
    return html;
  }

  function formatRating(value) {
    if (value == null) return '—';
    const n = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(n) || n === 0) return '—';
    return n.toFixed(1);
  }

  // Tier по оценке (шкала 1–5): 4+ зелёный, 3-3.99 амбер, <3 красный.
  function ratingTier(value) {
    if (value == null) return '';
    const n = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(n) || n === 0) return '';
    if (n >= 4) return 'rating-high';
    if (n >= 3) return 'rating-mid';
    return 'rating-low';
  }

  function urlForContent(item) {
    if (!item) return '#';
    return item.contentType === 'SERIES' ? `/series/${item.id}` : `/movies/${item.id}`;
  }

  function pluralize(n, forms) {
    // forms: ['рецензия', 'рецензии', 'рецензий']
    n = Math.abs(n) % 100;
    const n1 = n % 10;
    if (n > 10 && n < 20) return forms[2];
    if (n1 > 1 && n1 < 5)  return forms[1];
    if (n1 === 1)          return forms[0];
    return forms[2];
  }

  function formatDate(iso) {
    if (!iso) return '';
    try {
      const d = new Date(iso);
      return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch (e) {
      return '';
    }
  }

  // 1234 → "1 234". Используется для всех счётчиков (рейтинги, оценки, статы).
  // Один формат с обычным пробелом — toLocaleString даёт NBSP, что ломает count-up.
  function formatCount(value) {
    if (value == null) return '0';
    const n = typeof value === 'string' ? parseFloat(value) : value;
    if (!Number.isFinite(n)) return String(value);
    return String(Math.floor(n)).replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
  }

  // 148 → "2 ч 28 мин"; 47 → "47 мин"; 60 → "1 ч". Без падежей —
  // используются краткие формы единиц измерения, как принято в Кинопоиске
  // и российских onkino-каталогах.
  function formatDuration(minutes) {
    if (minutes == null) return '';
    const m = parseInt(minutes, 10);
    if (!Number.isFinite(m) || m <= 0) return '';
    const h = Math.floor(m / 60);
    const mm = m % 60;
    if (h && mm) return `${h} ч ${mm} мин`;
    if (h)       return `${h} ч`;
    return `${mm} мин`;
  }

  // Готовый текст «N форма-существительного» с правильным склонением.
  // Например: countNoun(1, ['рецензия','рецензии','рецензий']) → "1 рецензия".
  // Для 0 возвращает "0 рецензий" (можно подавить через withZero=false).
  function countNoun(n, forms) {
    return `${formatCount(n)} ${pluralize(n || 0, forms)}`;
  }

  // ISO-коды → русские локализованные имена.
  // Бэкенд хранит c.language/c.country в свободной форме (length 50/100):
  // в БД встречаются и alpha-2 («US», «en»), и alpha-3 («USA», «eng»),
  // и уже-локализованные строки («Россия», «English»). Хелпер нормализует
  // к одному виду — русское имя — для любого варианта.
  let _langDN = null, _regionDN = null;
  function _displayNames(type) {
    try {
      if (type === 'language') return _langDN || (_langDN = new Intl.DisplayNames(['ru'], { type: 'language', fallback: 'none' }));
      if (type === 'region')   return _regionDN || (_regionDN = new Intl.DisplayNames(['ru'], { type: 'region',   fallback: 'none' }));
    } catch (e) { /* старый браузер */ }
    return null;
  }
  function _looksLikeCode(v) { return /^[A-Za-z]{2,3}$/.test(v); }

  // Intl.DisplayNames(type:'region') принимает только alpha-2 + UN M49,
  // на alpha-3 бросает RangeError. Маппим топ кинопроизводящих стран
  // на alpha-2, дальше Intl даёт локализованное имя.
  const ALPHA3_TO_ALPHA2 = {
    USA:'US', GBR:'GB', RUS:'RU', FRA:'FR', DEU:'DE', ITA:'IT', ESP:'ES', PRT:'PT',
    JPN:'JP', KOR:'KR', PRK:'KP', CHN:'CN', HKG:'HK', TWN:'TW', IND:'IN', PAK:'PK',
    CAN:'CA', AUS:'AU', NZL:'NZ', BRA:'BR', ARG:'AR', MEX:'MX', CHL:'CL', COL:'CO',
    POL:'PL', CZE:'CZ', SVK:'SK', HUN:'HU', ROU:'RO', BGR:'BG', GRC:'GR', TUR:'TR',
    SWE:'SE', NOR:'NO', DNK:'DK', FIN:'FI', NLD:'NL', BEL:'BE', AUT:'AT', CHE:'CH',
    IRL:'IE', ISL:'IS', ISR:'IL', SAU:'SA', ARE:'AE', IRN:'IR', EGY:'EG', ZAF:'ZA',
    THA:'TH', VNM:'VN', IDN:'ID', PHL:'PH', MYS:'MY', SGP:'SG', UKR:'UA', BLR:'BY',
    KAZ:'KZ', GEO:'GE', ARM:'AM', AZE:'AZ', UZB:'UZ', LTU:'LT', LVA:'LV', EST:'EE',
    HRV:'HR', SRB:'RS', BIH:'BA', SVN:'SI', MKD:'MK', ALB:'AL', LUX:'LU', MLT:'MT',
    CYP:'CY', MAR:'MA', DZA:'DZ', TUN:'TN', NGA:'NG', KEN:'KE', ETH:'ET'
  };

  function formatCountry(value) {
    if (!value) return '';
    const v = String(value).trim();
    if (!_looksLikeCode(v)) return v;            // уже название («Россия», «United States»)
    const dn = _displayNames('region');
    if (!dn) return v;
    let code = v.toUpperCase();
    if (code.length === 3) code = ALPHA3_TO_ALPHA2[code] || null;
    if (!code) return v;
    try {
      return dn.of(code) || v;
    } catch (e) { return v; }
  }

  function formatLanguage(value) {
    if (!value) return '';
    const v = String(value).trim();
    if (!_looksLikeCode(v)) return v;            // уже название («Английский», «English»)
    const dn = _displayNames('language');
    if (!dn) return v;
    try {
      const name = dn.of(v.toLowerCase());
      if (!name) return v;
      return name.charAt(0).toUpperCase() + name.slice(1);   // «английский» → «Английский»
    } catch (e) { return v; }
  }

  // ===== Role labels (USER/ADMIN/MODERATOR → русский) =====
  const ROLE_LABELS = {
    USER:      'Пользователь',
    ADMIN:     'Администратор',
    MODERATOR: 'Модератор'
  };
  // ===== Playlist card — mosaic из реальных постеров =====
  // Если у подборки coverImageUrl задан — рисуем его как полную обложку.
  // Иначе — mosaic 2×2 из первых до 4 постеров фильмов внутри подборки.
  // Если фильмов нет — typographic fallback с hue от id+title.
  function playlistCover(p) {
    if (p.coverImageUrl) {
      return `<div class="playlist-cover"><img class="playlist-cover-img" src="${escapeHtml(p.coverImageUrl)}" alt="${escapeHtml(p.title || '')}" loading="lazy"></div>`;
    }
    const posters = Array.isArray(p.previewPosters) ? p.previewPosters.filter(Boolean) : [];
    if (posters.length === 0) {
      const hue = avatarHue((p.id || 0) + '-' + (p.title || ''));
      return `<div class="playlist-cover playlist-cover-fallback" style="--hue:${hue}deg" aria-hidden="true"></div>`;
    }
    // Берём до 4. Если меньше 4 — заполняем повторами для красоты mosaic.
    const fill = [];
    for (let i = 0; i < 4; i++) fill.push(posters[i % posters.length]);
    return `<div class="playlist-cover playlist-cover-mosaic" aria-hidden="true">${
      fill.map(u => `<div style="background-image:url('${escapeHtml(u)}')"></div>`).join('')
    }</div>`;
  }

  function playlistCard(p, opts = {}) {
    const { showOwner = false, showVisibility = false } = opts;
    const count = p.itemsCount || 0;
    const bylineParts = [];
    bylineParts.push(`${count} ${pluralize(count, ['фильм','фильма','фильмов'])}`);
    if (showVisibility && p.isPublic === false) bylineParts.push('Приватная');
    if (showOwner && p.owner) bylineParts.push('@' + escapeHtml(p.owner.username));
    return `
      <a class="playlist-card" href="/playlists/${p.id}">
        ${playlistCover(p)}
        <div class="playlist-meta">
          <div class="playlist-title">${escapeHtml(p.title || '')}</div>
          <div class="playlist-byline">${bylineParts.join(' · ')}</div>
        </div>
      </a>`;
  }

  function roleLabel(role) {
    if (!role) return ROLE_LABELS.USER;
    return ROLE_LABELS[role] || role;
  }

  // ===== Review row (общий компонент для /profile, /users/{x}, /me/reviews) =====
  // Letterboxd "Your reviews" паттерн: постер слева, бейджи (оценка + статус) сверху,
  // заголовок-ссылка на рецензию, мета (на чём + дата), excerpt 2 строки, статистика.
  // opts.actions=true добавляет столбец справа с кнопками Опубликовать/Редактировать/Удалить
  // для /me/reviews (обработчики кликов биндятся снаружи через data-action/data-id).

  const REVIEW_STATUS_LABELS = {
    DRAFT:      ['badge-draft',      'Черновик'],
    MODERATION: ['badge-moderation', 'На модерации'],
    PUBLISHED:  ['badge-published',  'Опубликована'],
    REJECTED:   ['badge-rejected',   'Отклонена'],
    HIDDEN:     ['badge-hidden',     'Скрыта'],
    DELETED:    ['badge-deleted',    'Удалена']
  };

  function reviewStatusBadge(s) {
    if (!s) return '';
    const [cls, label] = REVIEW_STATUS_LABELS[s] || ['badge-draft', String(s)];
    return `<span class="badge ${cls}">${label}</span>`;
  }

  function reviewRow(r, opts = {}) {
    const { actions = false, hideStatus = false } = opts;
    const reviewHref = `/reviews/${r.id}`;
    const c = r.content || null;
    const contentHref = c ? urlForContent(c) : null;

    const posterInner = c && c.posterUrl
      ? `<img src="${escapeHtml(c.posterUrl)}" alt="${escapeHtml(c.title || '')}" loading="lazy" onerror="this.parentNode.classList.add('is-empty');this.remove();">`
      : '';
    const poster = contentHref
      ? `<a class="list-row-poster${posterInner ? '' : ' is-empty'}" href="${contentHref}" aria-label="${escapeHtml(c.title || '')}">${posterInner}</a>`
      : `<div class="list-row-poster is-empty" aria-hidden="true"></div>`;

    const rating = r.ratingValue != null
      ? `<span class="rating-pill">${iconSparkle({ size: 11 })}<span class="rating-num">${r.ratingValue}</span></span>`
      : '';
    const status = hideStatus ? '' : reviewStatusBadge(r.status);

    const onContent = c
      ? `на «<a class="subtle" href="${contentHref}">${escapeHtml(c.title)}</a>»`
      : '';
    const dateStr = formatDate(r.createdAt);
    const metaParts = [onContent, dateStr].filter(Boolean).join(' · ');

    const excerpt = r.excerpt
      ? `<p class="list-row-excerpt">${escapeHtml(r.excerpt)}</p>`
      : '';

    const stats = `${formatCount(r.viewCount || 0)} ${pluralize(r.viewCount || 0, ['просмотр','просмотра','просмотров'])}`
      + ` · ${iconHeart({ size: 12 })} ${formatCount(r.likeCount || 0)}`;

    const headerLine = (rating || status)
      ? `<div class="d-flex align-items-center gap-2 mb-2">${rating}${status}</div>`
      : '';

    const body = `
      <div class="list-row-body">
        ${headerLine}
        <div class="list-row-title"><a class="text-light" href="${reviewHref}">${escapeHtml(r.title || 'Без названия')}</a></div>
        ${metaParts ? `<div class="list-row-meta mb-1">${metaParts}</div>` : ''}
        ${excerpt}
        <div class="list-row-meta list-row-stats">${stats}</div>
      </div>`;

    let actionsBlock = '';
    if (actions) {
      const canPublish = r.status === 'DRAFT';
      actionsBlock = `
        <div class="list-row-actions">
          ${canPublish ? `<button class="btn btn-xs btn-outline-gold" data-action="publish" data-id="${r.id}">Опубликовать</button>` : ''}
          <a class="btn btn-xs btn-outline-light" href="/reviews/${r.id}/edit">Редактировать</a>
          <button class="btn btn-xs btn-outline-danger" data-action="delete" data-id="${r.id}">Удалить</button>
        </div>`;
    }

    return `<div class="list-row" data-rid="${r.id}">${poster}${body}${actionsBlock}</div>`;
  }

  // ===== Постер с реальной картинкой + typographic fallback =====
  // Если posterUrl нет ИЛИ изображение не загрузилось — карточка убирается
  // из грида целиком (см. posterFallback ниже). Без визуальных fallback'ов:
  // лучше пустой слот скрыть, чем показывать «букву на градиенте».

  function posterImg(item, opts = {}) {
    const { sizeClass = '', showRating = true, showType = false } = opts;
    if (!item || !item.posterUrl) return '';
    const title = escapeHtml(item.title || 'Без названия');
    const url = item.posterUrl;
    const ratingStr = formatRating(item.averageRating);
    const isSeries = item.contentType === 'SERIES';

    // Размер искры внутри badge — пропорционально размерному варианту:
    // дефолт 13px → искра 11; .rating-lg 18px → 15; .rating-xl 28px → 22.
    let sparkleSize = 11;
    if (sizeClass.includes('poster-lg')) sparkleSize = 22;
    else if (sizeClass.includes('rating-lg')) sparkleSize = 15;

    return `
      <div class="poster ${sizeClass}">
        <img class="poster-img ${sizeClass}" src="${escapeHtml(url)}" alt="${title}" loading="lazy"
             onerror="UI.posterFallback(this)">
        ${showType && isSeries ? `<span class="poster-type-mark" aria-label="Сериал">SR</span>` : ''}
        ${showRating && ratingStr !== '—'
          ? `<span class="rating-badge" aria-label="Оценка ${ratingStr} из 10">${iconSparkle({ size: sparkleSize })}<span class="rating-num">${ratingStr}</span></span>`
          : ''}
      </div>`;
  }

  // ===== Карточка контента (для grid'ов) =====
  // Letterboxd / Kinopoisk / Apple TV паттерн: постер + 1-line title + 1-line meta.
  // Рейтинг в углу постера. Если posterUrl нет — карточка не рендерится.

  function contentCard(item) {
    if (!item || !item.posterUrl) return '';
    const title = escapeHtml(item.title || 'Без названия');
    const year = item.releaseYear || '';
    const isSeries = item.contentType === 'SERIES';
    // Letterboxd/Mubi/Apple TV паттерн: в grid под title только год.
    // Жанры и тип контента — на детальной странице. Минималистично, всегда влезает.
    const metaParts = [];
    if (year) metaParts.push(year);
    if (isSeries) metaParts.push('Сериал');
    const meta = metaParts.join(' · ') || '&nbsp;';
    return `
      <a class="content-card" href="${urlForContent(item)}" aria-label="${title}">
        ${posterImg(item, { showRating: true, showType: false })}
        <div class="content-card-body">
          <div class="content-title">${title}</div>
          <div class="content-meta-line">${meta}</div>
        </div>
      </a>`;
  }

  // Готовая column-обёртка для grid-страниц. Дефолт — 6-col на ≥1200px (как
  // на главной и detail-similar), переопределяется через colClass.
  function contentCardCol(item, colClass = 'col-6 col-md-4 col-lg-2') {
    const card = contentCard(item);
    if (!card) return '';
    return `<div class="${colClass}">${card}</div>`;
  }

  // ===== Skeleton =====

  function skeletonCard() {
    return `
      <div class="content-card">
        <div class="poster">
          <div class="skeleton" style="width:100%;aspect-ratio:2/3;border-radius:var(--r-lg);"></div>
        </div>
        <div class="skeleton" style="height:14px;width:80%;margin-top:12px;"></div>
        <div class="skeleton" style="height:12px;width:50%;margin-top:6px;"></div>
      </div>`;
  }

  function skeletonGrid(count = 12, colClass = 'col-6 col-md-4 col-lg-2') {
    const cols = [];
    for (let i = 0; i < count; i++) cols.push(`<div class="${colClass}">${skeletonCard()}</div>`);
    return cols.join('');
  }

  function skeletonList(count = 5) {
    let html = '';
    for (let i = 0; i < count; i++) {
      html += `
        <div class="list-row">
          <div class="skeleton" style="width:80px;aspect-ratio:2/3;border-radius:var(--r);flex-shrink:0;"></div>
          <div class="list-row-body">
            <div class="skeleton" style="height:18px;width:60%;"></div>
            <div class="skeleton" style="height:13px;width:40%;margin-top:8px;"></div>
            <div class="skeleton" style="height:13px;width:90%;margin-top:8px;"></div>
            <div class="skeleton" style="height:13px;width:80%;margin-top:6px;"></div>
          </div>
        </div>`;
    }
    return html;
  }

  // ===== Empty / error states =====

  function emptyState({ title = 'Пусто', text = '', cta = '', ctaHref = '' } = {}) {
    return `
      <div class="empty-state">
        <svg class="empty-state-illustration" viewBox="0 0 96 96" fill="none" aria-hidden="true">
          <rect x="14" y="22" width="68" height="52" rx="6" stroke="currentColor" stroke-width="1.5" opacity="0.4"/>
          <path d="M14 32 L82 32" stroke="currentColor" stroke-width="1.5" opacity="0.4"/>
          <circle cx="22" cy="27" r="1.5" fill="currentColor" opacity="0.5"/>
          <circle cx="28" cy="27" r="1.5" fill="currentColor" opacity="0.5"/>
          <circle cx="34" cy="27" r="1.5" fill="currentColor" opacity="0.5"/>
          <path d="M40 50 L48 58 L62 44" stroke="var(--gold)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <h3>${escapeHtml(title)}</h3>
        ${text ? `<p>${escapeHtml(text)}</p>` : ''}
        ${cta && ctaHref ? `<a class="g-btn g-btn-primary" href="${escapeHtml(ctaHref)}">${escapeHtml(cta)}</a>` : ''}
      </div>`;
  }

  function errorState({ title = 'Что-то пошло не так', text = 'Попробуйте перезагрузить страницу.', onRetry = null } = {}) {
    const id = `retry-${Math.random().toString(36).slice(2, 8)}`;
    setTimeout(() => {
      const btn = document.getElementById(id);
      if (btn && onRetry) btn.addEventListener('click', onRetry);
    }, 0);
    return `
      <div class="empty-state error-state">
        <svg class="error-state-glyph" viewBox="0 0 64 64" fill="none" aria-hidden="true">
          <path d="M32 12 L56 52 H8 Z"
                stroke="#ef4444" stroke-width="2.5" stroke-linejoin="round"
                fill="rgba(239,68,68,0.10)"/>
          <path d="M32 28 V40" stroke="#ef4444" stroke-width="2.75" stroke-linecap="round"/>
          <circle cx="32" cy="46" r="1.75" fill="#ef4444"/>
        </svg>
        <h3>${escapeHtml(title)}</h3>
        <p>${escapeHtml(text)}</p>
        <button id="${id}" class="g-btn g-btn-ghost" type="button">Перезагрузить</button>
      </div>`;
  }

  // ===== Pagination =====

  function pagination(pageInfo, onPage) {
    if (!pageInfo || pageInfo.totalPages <= 1) return '';
    const cur = pageInfo.page;
    const total = pageInfo.totalPages;
    const max = 7;
    let pages = [];
    if (total <= max) {
      for (let i = 0; i < total; i++) pages.push(i);
    } else {
      pages.push(0);
      const left = Math.max(1, cur - 2);
      const right = Math.min(total - 2, cur + 2);
      if (left > 1) pages.push('...');
      for (let i = left; i <= right; i++) pages.push(i);
      if (right < total - 2) pages.push('...');
      pages.push(total - 1);
    }
    const id = `pg-${Math.random().toString(36).slice(2, 8)}`;
    setTimeout(() => {
      const root = document.getElementById(id);
      if (!root) return;
      root.querySelectorAll('a[data-page]').forEach(a => {
        a.addEventListener('click', (e) => {
          e.preventDefault();
          const p = parseInt(a.dataset.page, 10);
          if (!isNaN(p) && onPage) onPage(p);
        });
      });
    }, 0);
    const items = pages.map(p => {
      if (p === '...') return `<li class="page-item disabled"><span class="page-link">…</span></li>`;
      const active = p === cur ? 'active' : '';
      return `<li class="page-item ${active}"><a class="page-link" data-page="${p}" href="#">${p + 1}</a></li>`;
    }).join('');
    const prev = pageInfo.hasPrev
      ? `<li class="page-item"><a class="page-link" data-page="${cur - 1}" href="#">‹</a></li>`
      : `<li class="page-item disabled"><span class="page-link">‹</span></li>`;
    const next = pageInfo.hasNext
      ? `<li class="page-item"><a class="page-link" data-page="${cur + 1}" href="#">›</a></li>`
      : `<li class="page-item disabled"><span class="page-link">›</span></li>`;
    return `<nav id="${id}"><ul class="pagination justify-content-center mt-4">${prev}${items}${next}</ul></nav>`;
  }

  // Дедуп — чтобы один и тот же id не отправлять при повторных onerror на той
  // же странице (несколько гридов могут показать одну запись).
  const reportedBrokenPosters = new Set();

  // Helper для onerror — если картинка не загрузилась:
  //  1) шлём fire-and-forget POST /api/v1/content/{id}/report-broken-poster,
  //     чтобы сервер пометил запись и не отдавал её в следующих list-запросах
  //  2) удаляем ВСЮ обёртку с col-* (Bootstrap-grid), иначе в сетке остаётся
  //     пустой слот. closest() возвращает первого предка по селектору, поэтому
  //     ищем сначала col-* вверх, затем .content-card как fallback.
  function posterFallback(imgEl) {
    reportBrokenPoster(imgEl);
    const colWrap = imgEl.closest('[class*="col-"]');
    if (colWrap) { colWrap.remove(); return; }
    const card = imgEl.closest('.content-card');
    if (card) { card.remove(); return; }
    imgEl.remove();
  }

  function reportBrokenPoster(imgEl) {
    const link = imgEl.closest('a[href]');
    if (!link) return;
    const m = link.getAttribute('href').match(/\/(?:movies|series|content)\/(\d+)/);
    if (!m) return;
    const id = m[1];
    if (reportedBrokenPosters.has(id)) return;
    reportedBrokenPosters.add(id);
    fetch(`/api/v1/content/${id}/report-broken-poster`, {
      method: 'POST',
      keepalive: true
    }).catch(() => { /* fire-and-forget */ });
  }

  // Разворачивает обёртку RecommendationResponse{content,score,reason} → ContentListItem
  function unwrapRecs(arr) {
    if (!Array.isArray(arr)) return [];
    return arr.map(r => (r && r.content) ? r.content : r).filter(Boolean);
  }

  // ===== Motion observers (общие для всех страниц) =====

  // Mouse tracking — Gladia "soul" effect. Updates global CSS vars --mx, --my.
  // These are used for radial spotlights in hero, glass-card glows, etc.
  function initMouseSpotlight() {
    const root = document.documentElement;
    window.addEventListener('mousemove', (e) => {
      const x = (e.clientX / window.innerWidth).toFixed(3);
      const y = (e.clientY / window.innerHeight).toFixed(3);
      root.style.setProperty('--mx', x);
      root.style.setProperty('--my', y);
    }, { passive: true });
  }

  // Scroll-fade-up для всех `.g-reveal` секций.
  // Gladia style: 600ms cubic-bezier(0.16, 1, 0.3, 1) + 80ms stagger.
  function initReveal() {
    if (!('IntersectionObserver' in window)) {
      document.querySelectorAll('.g-reveal:not(.is-visible)').forEach(el => el.classList.add('is-visible'));
      return;
    }

    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const el = entry.target;
          el.classList.add('is-visible');

          // Если это группа (например, g-stat-row), стаггерим детей
          if (el.classList.contains('g-reveal-group')) {
            const items = el.querySelectorAll(':scope > *');
            items.forEach((item, i) => {
              item.style.transitionDelay = `${i * 80}ms`;
              item.classList.add('is-visible');
            });
          }

          io.unobserve(el);
        }
      });
    }, { rootMargin: '0px 0px -10% 0px', threshold: 0.08 });

    document.querySelectorAll('.g-reveal:not(.is-visible), .g-reveal-group:not(.is-visible)').forEach(el => io.observe(el));
  }

  // Count-up анимация для всех `.g-stat-num` элементов. Снимает текущий текст
  // как target, считает 0 → target за `duration`мс ease-out cubic. Сохраняет
  // суффикс "+" («30+») и десятичную точность («8.5» → анимируется как float
  // и оставляет один знак после запятой). Тире-плейсхолдер «—» пропускается.
  function animateCount(el, target, duration = 1400) {
    if (!el) return;
    const targetStr = String(target);
    const isPlus = targetStr.endsWith('+');
    // Извлекаем число вместе с дробной частью — ВАЖНО не вырезать точку,
    // иначе «8.5» превращается в «85» (это ломало рейтинги в stat-row).
    const cleaned = targetStr.replace(/[^\d.,]/g, '').replace(',', '.');
    const num = parseFloat(cleaned);
    if (!Number.isFinite(num) || num <= 0) { el.textContent = target; return; }
    const decimalPart = cleaned.split('.')[1] || '';
    const decimals = decimalPart.length;
    const start = performance.now();
    function fmt(v) {
      return decimals > 0 ? v.toFixed(decimals) : formatCount(Math.floor(v));
    }
    function frame(now) {
      const t = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - t, 3);
      const value = num * eased;
      el.textContent = fmt(value) + (isPlus && t === 1 ? '+' : '');
      if (t < 1) requestAnimationFrame(frame);
      else el.textContent = fmt(num) + (isPlus ? '+' : '');
    }
    requestAnimationFrame(frame);
  }

  function initStatCountUp() {
    if (!('IntersectionObserver' in window)) return;
    const seen = new WeakSet();
    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (!entry.isIntersecting || seen.has(entry.target)) return;
        seen.add(entry.target);
        const target = entry.target.dataset.statTarget;
        if (target) animateCount(entry.target, target);
        io.unobserve(entry.target);
      });
    }, { threshold: 0.4 });
    // Stats грузятся асинхронно — ждём пока во всех `.g-stat-num` появится
    // нечто отличное от "—", фиксируем как target, обнуляем, ставим observer.
    const tryAttach = () => {
      document.querySelectorAll('.g-stat-num').forEach(el => {
        const txt = (el.textContent || '').trim();
        if (!txt || txt === '—') return;
        if (el.dataset.statTarget) return;
        el.dataset.statTarget = txt;
        el.textContent = '0';
        io.observe(el);
      });
    };
    let attempts = 0;
    const tick = setInterval(() => {
      tryAttach();
      attempts++;
      const ready = Array.from(document.querySelectorAll('.g-stat-num')).every(el => el.dataset.statTarget);
      if (ready || attempts > 30) clearInterval(tick);
    }, 250);
  }

  function confirmDialog({ title = 'Подтверждение', text = '', confirmText = 'Подтвердить', cancelText = 'Отмена', danger = false } = {}) {
    return new Promise((resolve) => {
      const wrap = document.createElement('div');
      wrap.innerHTML = `
        <div class="modal fade" tabindex="-1" aria-hidden="true">
          <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">${escapeHtml(title)}</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Закрыть"></button>
              </div>
              <div class="modal-body">${escapeHtml(text)}</div>
              <div class="modal-footer">
                <button type="button" class="btn btn-outline-light" data-bs-dismiss="modal" data-act="cancel">${escapeHtml(cancelText)}</button>
                <button type="button" class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-act="ok">${escapeHtml(confirmText)}</button>
              </div>
            </div>
          </div>
        </div>`;
      const el = wrap.firstElementChild;
      document.body.appendChild(el);
      const modal = new bootstrap.Modal(el);
      let result = false;
      el.querySelector('[data-act="ok"]').addEventListener('click', () => { result = true; modal.hide(); });
      el.addEventListener('hidden.bs.modal', () => { el.remove(); resolve(result); });
      modal.show();
    });
  }

  global.UI = {
    escapeHtml,
    confirmDialog,
    firstChar,
    avatarHue,
    avatarHtml,
    iconSparkle,
    iconHeart,
    starRatingTemplate,
    formatRating,
    ratingTier,
    formatCount,
    formatDuration,
    countNoun,
    formatLanguage,
    formatCountry,
    urlForContent,
    pluralize,
    formatDate,
    posterImg,
    posterFallback,
    reviewStatusBadge,
    reviewRow,
    roleLabel,
    playlistCard,
    playlistCover,
    contentCard,
    contentCardCol,
    skeletonCard,
    skeletonGrid,
    skeletonList,
    emptyState,
    errorState,
    pagination,
    unwrapRecs,
    initReveal,
    initStatCountUp,
    initMouseSpotlight
  };
})(window);
