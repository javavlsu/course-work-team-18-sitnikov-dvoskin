/**
 * page-content-detail.js — страница фильма/сериала (V2 redesign).
 *
 * Применяет паттерны homepage (.g-* namespace) + Refero references
 * (см. content-detail.html). Структура:
 *   1. Billboard hero (full-bleed, 2-layer overlay)
 *   2. 4-up stat row (.g-stat-row)
 *   3. Reviews list
 *   4. Similar grid (6-col, .content-card)
 *   5. Production facts (.facts-list, full-width)
 *   6. Comments
 *
 * API:
 *  - GET  /api/v1/content/{id}
 *  - GET  /api/v1/reviews?contentId=...
 *  - GET  /api/v1/content/{id}/comments
 *  - GET  /api/v1/recommendations/similar/{id}
 *  - PUT  /api/v1/content/{id}/rating { value }
 *  - GET  /api/v1/content/{id}/rating/me
 *  - POST /api/v1/content/{id}/comments { text }
 */
(function () {
  'use strict';

  function getContentId() {
    const m = location.pathname.match(/\/(?:movies|series|content)\/(\d+)/);
    return m ? parseInt(m[1], 10) : null;
  }

  let CONTENT = null;
  const contentId = getContentId();

  function $(id) { return document.getElementById(id); }
  function setText(id, text) { const el = $(id); if (el) el.textContent = text; }
  function setHtml(id, html) { const el = $(id); if (el) el.innerHTML = html; }

  // Меняет содержимое async-секции и помечает её is-loaded, чтобы CSS
  // [data-mount].is-loaded > * fade-in отыграл по children.
  function setMount(id, html) {
    const el = $(id);
    if (!el) return;
    el.classList.remove('is-loaded');
    el.innerHTML = html;
    // двойной rAF — гарантированно после style flush, чтобы анимация перезапустилась
    requestAnimationFrame(() => requestAnimationFrame(() => el.classList.add('is-loaded')));
  }

  // ===== Hero =================================================

  function renderHero(c) {
    document.title = `${c.title} · MovieHub`;

    // Back link
    const isSeries = c.contentType === 'SERIES';
    const back = $('back-link');
    back.setAttribute('href', isSeries ? '/catalog?type=SERIES' : '/catalog?type=MOVIE');
    back.textContent = 'К каталогу';

    // Poster (через UI helper)
    $('poster-mount').innerHTML = UI.posterImg(c, {
      sizeClass: 'poster-lg',
      showRating: false,
      showType: false
    });

    // Title + original
    setText('hero-title', c.title);
    setText('hero-original', c.originalTitle && c.originalTitle !== c.title ? c.originalTitle : '');

    // Rating cluster (big tier-colored number) — единый стиль с .rating-badge,
    // .score-pill и пр. через брендовую искру UI.iconSparkle.
    const cluster = $('rating-cluster');
    if (c.averageRating != null && c.averageRating > 0) {
      cluster.removeAttribute('hidden');
      cluster.className = `detail-rating-cluster ${UI.ratingTier(c.averageRating)}`;
      $('rating-sparkle').innerHTML = UI.iconSparkle({ size: 28 });
      $('rating-big').textContent = UI.formatRating(c.averageRating);
      const votes = c.totalRatings || 0;
      $('rating-votes').textContent = votes
        ? UI.countNoun(votes, ['оценка', 'оценки', 'оценок'])
        : 'Нет оценок';
    } else {
      cluster.setAttribute('hidden', '');
    }

    // Description
    setText('hero-description', c.description || 'Описание скоро появится.');

    // Сначала жанры (широкие категории), потом теги (тонкие пометки). Оба кликабельны.
    const genreBadges = (c.genres || []).map(g =>
      `<a href="/catalog?genre=${g.id}" class="tag-badge">${UI.escapeHtml(g.name)}</a>`
    );
    const tagBadges = (c.tags || []).map(t =>
      `<a href="/catalog?tag=${t.id}" class="tag-badge tag-badge-soft">${UI.escapeHtml(t.name)}</a>`
    );
    setHtml('tag-row', genreBadges.concat(tagBadges).join(''));

    // CTA write-review href
    $('cta-write-review').setAttribute('href', `/reviews/new?contentId=${c.id}`);

    // Inline meta-line под title
    renderMetaLine(c);

    // Триггерим entrance-анимацию через двойной rAF, чтобы initial opacity:0
    // успел применить, и keyframes стартанули с правильного состояния.
    const billboard = document.querySelector('.detail-billboard');
    if (billboard) {
      requestAnimationFrame(() => requestAnimationFrame(() => billboard.classList.add('is-loaded')));
    }
  }

  // Apple TV inline meta-line: типаж • год • длительность/сезоны • страна •
  // язык • IMDb. Пустые поля пропускаются. Внешние ID — кликабельные ссылки.
  function renderMetaLine(c) {
    const isSeries = c.contentType === 'SERIES';
    const parts = [];
    parts.push(`<span>${isSeries ? 'Сериал' : 'Фильм'}</span>`);
    if (c.releaseYear) parts.push(`<span>${c.releaseYear}</span>`);

    if (!isSeries && c.duration) {
      parts.push(`<span>${UI.formatDuration(c.duration)}</span>`);
    }
    if (isSeries) {
      const sub = [];
      if (c.totalSeasons)  sub.push(UI.countNoun(c.totalSeasons,  ['сезон', 'сезона', 'сезонов']));
      if (c.totalEpisodes) sub.push(UI.countNoun(c.totalEpisodes, ['эпизод', 'эпизода', 'эпизодов']));
      if (sub.length) parts.push(`<span>${sub.join(' · ')}</span>`);
      if (c.isFinished === true)  parts.push(`<span>Завершён</span>`);
      if (c.isFinished === false) parts.push(`<span>Продолжается</span>`);
    }

    if (c.country)  parts.push(`<span>${UI.escapeHtml(UI.formatCountry(c.country))}</span>`);
    if (c.language) parts.push(`<span>${UI.escapeHtml(UI.formatLanguage(c.language))}</span>`);

    if (c.imdbId) {
      parts.push(`<a class="meta-ext meta-ext-imdb" href="https://www.imdb.com/title/${UI.escapeHtml(c.imdbId)}" target="_blank" rel="noopener" aria-label="Открыть на IMDb"></a>`);
    }
    if (c.kinopoiskId) {
      parts.push(`<a class="meta-ext meta-ext-kinopoisk" href="https://www.kinopoisk.ru/film/${UI.escapeHtml(String(c.kinopoiskId))}" target="_blank" rel="noopener" aria-label="Открыть на Кинопоиске"></a>`);
    }

    if (c.budget)    parts.push(`<span>$${UI.formatCount(c.budget)} бюджет</span>`);
    if (c.boxOffice) parts.push(`<span>$${UI.formatCount(c.boxOffice)} сборы</span>`);

    setHtml('meta-line', parts.join(''));
  }

  // ===== Reviews =============================================

  // Карточка рецензии: header (avatar + автор + оценка) — анкер чьё мнение,
  // затем заголовок рецензии как главное высказывание, затем excerpt
  // вторым планом. Карточка целиком кликабельная.
  function reviewCard(r) {
    const username = r.author && r.author.username;
    const author = username ? UI.escapeHtml(username) : 'anon';
    const score = r.ratingValue != null ? r.ratingValue : '—';
    const views = r.viewCount || 0;
    const metaParts = [UI.formatDate(r.createdAt)];
    if (views) metaParts.push(UI.countNoun(views, ['просмотр', 'просмотра', 'просмотров']));
    const excerpt = r.excerpt
      ? `<p class="review-excerpt">${UI.escapeHtml(r.excerpt)}</p>`
      : '';
    return `
      <a class="review-card review-card-link" href="/reviews/${r.id}">
        <div class="review-head">
          ${UI.avatarHtml(username, { extraAttrs: 'aria-hidden="true"' })}
          <div class="review-author-block">
            <span class="review-author">@${author}</span>
            <span class="review-meta">${metaParts.join(' · ')}</span>
          </div>
          <span class="review-score ${UI.ratingTier(r.ratingValue)}">${UI.iconSparkle({ size: 14 })}<span>${score}</span></span>
        </div>
        <h3 class="review-title">${UI.escapeHtml(r.title || 'Без названия')}</h3>
        ${excerpt}
        <div class="review-foot">
          <span class="review-likes">${UI.iconHeart({ size: 14 })}<span>${UI.formatCount(r.likeCount)}</span></span>
          <span class="read-link">Читать</span>
        </div>
      </a>`;
  }

  // Featured pull-quote — самая залайканная рецензия с excerpt'ом, идёт
  // перед обычным списком. Эмоциональный якорь страницы.
  function featuredReviewBlock(r) {
    if (!r || !r.excerpt) return '';
    const username = r.author && r.author.username;
    const author = username ? UI.escapeHtml(username) : 'anon';
    const score = r.ratingValue != null ? r.ratingValue : '—';
    const likes = r.likeCount || 0;
    return `
      <a class="review-featured" href="/reviews/${r.id}">
        <div class="review-featured-eyebrow">Главная рецензия</div>
        <p class="review-featured-quote">${UI.escapeHtml(r.excerpt)}</p>
        <div class="review-featured-foot">
          ${UI.avatarHtml(username, { extraAttrs: 'aria-hidden="true"' })}
          <div class="featured-author-block">
            <span class="featured-author">@${author}</span>
            <span class="featured-meta">${UI.formatDate(r.createdAt)}${likes ? ' · ' + UI.countNoun(likes, ['лайк','лайка','лайков']) : ''}</span>
          </div>
          <span class="review-score ${UI.ratingTier(r.ratingValue)}">${UI.iconSparkle({ size: 14 })}<span>${score}</span></span>
        </div>
      </a>`;
  }

  function renderReviews(page, id) {
    const items = (page && page.items) || [];
    const total = page ? (page.totalElements || 0) : 0;

    // Если рецензий нет — скрываем секцию целиком, не показываем empty-state.
    const section = $('reviews-section');
    if (!items.length) {
      if (section) section.setAttribute('hidden', '');
      return;
    }
    if (section) section.removeAttribute('hidden');

    const allLink = $('all-reviews-link');
    if (total === 1) allLink.textContent = '1 рецензия';
    else             allLink.textContent = `Все ${UI.countNoun(total, ['рецензия', 'рецензии', 'рецензий'])}`;
    allLink.setAttribute('href', `/reviews?contentId=${id}`);

    // Featured = самая залайканная рецензия с непустым excerpt. Остальные —
    // обычным списком ниже. Если только 1 рецензия — она и есть featured.
    const sorted = [...items].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
    const featured = sorted.find(r => r.excerpt) || null;
    const rest = featured ? items.filter(r => r.id !== featured.id) : items;

    const html = (featured ? featuredReviewBlock(featured) : '')
      + (rest.length ? rest.map(reviewCard).join('') : '');
    setMount('reviews-mount', html);
  }

  // ===== Comments ============================================

  // Row-layout (YouTube/Reddit): avatar слева, контент справа,
  // hairline-дивайдер между картами через .comments-list CSS.
  function commentCard(c) {
    const username = c.author && c.author.username;
    const author = username ? UI.escapeHtml(username) : 'anon';
    const editedLabel = c.isEdited ? ' · отредактировано' : '';
    const hue = UI.avatarHue(username);
    const initials = UI.firstChar(username);
    return `
      <article class="comment-card">
        <div class="comment-avatar-wrap">
          <a href="/users/${encodeURIComponent(author)}" class="user-avatar" style="--avatar-hue:${hue}" aria-label="Профиль ${author}">${initials}</a>
        </div>
        <div class="comment-body-wrap">
          <div class="comment-head">
            <a href="/users/${encodeURIComponent(author)}" class="comment-author">@${author}</a>
            <span class="comment-meta">${UI.formatDate(c.createdAt)}${editedLabel}</span>
          </div>
          <p class="comment-body">${UI.escapeHtml(c.text || '')}</p>
        </div>
      </article>`;
  }

  function renderComposeBox(id) {
    const body = $('compose-body');
    const compose = $('compose-card');
    if (!Auth.isAuthenticated()) {
      // Guest-вариант — row-layout как у обычной комментарии, без card-chrome.
      // Класс на .compose-card убирает bg/border/padding — блок сливается
      // с комменто-списком ниже визуально.
      if (compose) compose.classList.add('comments-compose--guest');
      body.innerHTML = `
        <article class="comment-card comment-card-guest">
          <div class="comment-avatar-wrap">
            <span class="user-avatar comment-avatar-ghost" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.418 3.582-8 8-8s8 3.582 8 8"/></svg>
            </span>
          </div>
          <div class="comment-body-wrap">
            <p class="comment-guest-prompt">
              <a href="/login?next=${encodeURIComponent(location.pathname)}">Войдите</a>, чтобы оставить комментарий
            </p>
          </div>
        </article>`;
      return;
    }
    if (compose) compose.classList.remove('comments-compose--guest');
    body.innerHTML = `
      <textarea class="form-control" id="comment-text" rows="3" placeholder="Поделись мыслями о фильме…" maxlength="2000"></textarea>
      <div class="d-flex justify-content-between align-items-center mt-3">
        <span class="text-muted small" id="comment-counter">0 / 2000</span>
        <div class="d-flex gap-2">
          <button class="g-btn g-btn-ghost g-btn-sm" id="comment-cancel">Очистить</button>
          <button class="g-btn g-btn-primary g-btn-sm" id="comment-submit">Отправить</button>
        </div>
      </div>
      <div id="comment-error" class="auth-alert mt-3" hidden></div>`;
    const ta = $('comment-text');
    const counter = $('comment-counter');
    ta.addEventListener('input', () => counter.textContent = `${ta.value.length} / 2000`);
    $('comment-cancel').addEventListener('click', () => { ta.value = ''; counter.textContent = '0 / 2000'; });
    $('comment-submit').addEventListener('click', async () => {
      const text = ta.value.trim();
      if (!text) return;
      try {
        await API.post(`/content/${id}/comments`, { text });
        ta.value = '';
        counter.textContent = '0 / 2000';
        loadComments(id);
      } catch (e) {
        const err = $('comment-error');
        err.textContent = e.message || 'Не удалось отправить комментарий';
        err.removeAttribute('hidden');
      }
    });
  }

  function renderComments(page, id) {
    renderComposeBox(id);
    const items = (page && page.items) || [];
    const total = page ? (page.totalElements || 0) : 0;
    $('comments-count').textContent = UI.countNoun(total, ['комментарий', 'комментария', 'комментариев']);
    if (!items.length) {
      setMount('comments-mount', `<p class="text-muted small" style="padding:8px 0">Будь первым, кто оставит комментарий.</p>`);
    } else {
      setMount('comments-mount', items.map(commentCard).join(''));
    }
  }

  // Локальный fetch обёртка для commenta — после публикации перерисовываем.
  async function loadComments(id) {
    try {
      const page = await API.contentComments(id);
      renderComments(page, id);
    } catch (e) {
      console.error('[content-detail] comments', e);
      setMount('comments-mount', UI.errorState({ onRetry: () => loadComments(id) }));
    }
  }

  // ===== Similar (6-col grid, .content-card) =================

  function renderSimilar(items, id) {
    const filtered = (items || []).filter(x => x && x.id !== id && x.posterUrl).slice(0, 6);
    if (!filtered.length) {
      $('similar-section').setAttribute('hidden', '');
    } else {
      setMount('similar-mount', filtered.map(it => UI.contentCardCol(it)).join(''));
    }
  }

  // ===== Rating modal ========================================

  function bindRatingModal() {
    // Брендовые искры (1..5) — шкала из ТЗ Этапа 3 (1–5).
    const starsRoot = $('stars');
    if (starsRoot && !starsRoot.children.length) {
      starsRoot.innerHTML = UI.starRatingTemplate({ max: 5 });
    }

    const stars = document.querySelectorAll('#stars button');
    const valEl = $('rate-val');
    let current = 0;

    stars.forEach(b => {
      b.addEventListener('click', () => {
        current = +b.dataset.v;
        valEl.textContent = current + ' / 5';
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= current));
      });
      b.addEventListener('mouseenter', () => {
        const v = +b.dataset.v;
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= v));
      });
    });
    $('stars').addEventListener('mouseleave', () => {
      stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= current));
    });

    // Auth gate на CTA — если не залогинен, редиректим
    $('cta-rate').addEventListener('click', (e) => {
      if (!Auth.isAuthenticated()) {
        e.preventDefault();
        e.stopPropagation();
        location.href = `/login?next=${encodeURIComponent(location.pathname)}`;
      }
    });
    $('my-rating-edit').addEventListener('click', () => {
      // Если уже стоит оценка — показываем "Убрать оценку", иначе скрываем
      const has = $('my-rating-edit').hasAttribute('hidden') === false &&
                  $('my-rating-num').textContent !== '—';
      $('remove-rating').toggleAttribute('hidden', !has);
      const m = new bootstrap.Modal($('rateModal'));
      m.show();
    });

    $('save-rating').addEventListener('click', async () => {
      if (!current) return;
      try {
        await API.put(`/content/${contentId}/rating`, { value: current });
        $('my-rating-edit').removeAttribute('hidden');
        $('my-rating-num').textContent = current;
        $('cta-rate').classList.add('is-active');
        bootstrap.Modal.getInstance($('rateModal')).hide();
      } catch (e) {
        alert(e.message || 'Не удалось сохранить оценку');
      }
    });

    $('remove-rating').addEventListener('click', async () => {
      try {
        await API.delete(`/content/${contentId}/rating`);
        $('my-rating-edit').setAttribute('hidden', '');
        $('my-rating-num').textContent = '—';
        $('cta-rate').classList.remove('is-active');
        current = 0;
        stars.forEach(x => x.classList.remove('is-active'));
        $('rate-val').textContent = '—';
        bootstrap.Modal.getInstance($('rateModal')).hide();
      } catch (e) {
        console.error('[content] remove rating', e);
      }
    });
  }

  // ===== Add to playlist =====================================

  function bindAddPlaylist() {
    $('cta-add-playlist').addEventListener('click', async () => {
      if (!Auth.isAuthenticated()) {
        location.href = `/login?next=${encodeURIComponent(location.pathname)}`;
        return;
      }
      const m = new bootstrap.Modal($('playlistModal'));
      m.show();
      const body = $('playlist-modal-body');
      try {
        const me = Auth.user;
        const lists = await API.userPlaylists(me.username);
        if (!lists || !lists.length) {
          body.innerHTML = `
            <p class="text-muted">У вас ещё нет подборок.</p>
            <a class="g-btn g-btn-primary" href="/playlists/new?contentId=${contentId}">Создать подборку</a>`;
        } else {
          body.innerHTML = lists.map(p => `
            <button class="g-btn g-btn-ghost w-100 mb-2" style="justify-content:space-between" data-pid="${p.id}">
              <span>${UI.escapeHtml(p.title)}</span>
              <span class="text-muted small">${p.itemsCount} в подборке</span>
            </button>
          `).join('') + `<a class="g-btn g-btn-ghost w-100 mt-2" href="/playlists/new?contentId=${contentId}">+ Создать новую</a>`;
          body.querySelectorAll('button[data-pid]').forEach(btn => {
            btn.addEventListener('click', async () => {
              const pid = btn.dataset.pid;
              try {
                await API.post(`/playlists/${pid}/items`, { contentId });
                btn.querySelector('span').textContent = '✓ Добавлено';
                btn.disabled = true;
                setTimeout(() => bootstrap.Modal.getInstance($('playlistModal')).hide(), 600);
              } catch (e) {
                alert(e.message || 'Не удалось добавить');
              }
            });
          });
        }
      } catch (e) {
        body.innerHTML = `<p class="text-danger">Не удалось загрузить подборки.</p>`;
      }
    });
  }

  // ===== Share button (Web Share API + clipboard fallback) ===

  function bindShare() {
    const btn = $('cta-share');
    btn.addEventListener('click', async () => {
      const url = location.href;
      const title = (CONTENT && CONTENT.title) || document.title;
      try {
        if (navigator.share) {
          await navigator.share({ title, url });
        } else if (navigator.clipboard) {
          await navigator.clipboard.writeText(url);
          btn.classList.add('is-active');
          btn.setAttribute('title', 'Ссылка скопирована');
          setTimeout(() => {
            btn.classList.remove('is-active');
            btn.setAttribute('title', 'Поделиться ссылкой');
          }, 1400);
        }
      } catch (_) { /* user cancelled — ok */ }
    });
  }

  // ===== Bootstrap ===========================================

  // Параллельный fetch + строго последовательный render в DOM-порядке.
  // Это убирает «рандомный порядок» — секции светятся сверху вниз
  // независимо от того, какой запрос ответил быстрее. CSS-fade на каждом
  // [data-mount].is-loaded даёт плавную замену скелетона.
  async function loadContent() {
    if (!contentId) {
      document.querySelector('main').innerHTML = `
        <div class="g-container" style="padding: 96px 0">
          ${UI.errorState({ title: 'Контент не найден', text: 'Проверьте адрес и попробуйте снова.' })}
        </div>`;
      return;
    }
    try {
      const c = await API.contentById(contentId);
      CONTENT = c;
      renderHero(c);

      // Старт всех вторичных запросов параллельно — сеть не сериализуем
      const reviewsP = API.listReviews({ contentId: c.id, status: 'PUBLISHED', size: 5 })
        .catch(e => { console.error('[content-detail] reviews', e); return null; });

      const similarP = (async () => {
        try {
          const recs = await API.recommendations(`similar/${c.id}`, { limit: 12 });
          if (recs && Array.isArray(recs)) return UI.unwrapRecs(recs);
          if (recs && recs.items) return recs.items;
          const fallback = await API.listContent({ size: 12 });
          return (fallback && fallback.items) || [];
        } catch (e) {
          console.warn('[content-detail] similar', e.message);
          return [];
        }
      })();

      const commentsP = API.contentComments(c.id)
        .catch(e => { console.error('[content-detail] comments', e); return null; });

      const myRatingP = Auth.isAuthenticated()
        ? API.get(`/content/${c.id}/rating/me`).catch(() => null)
        : Promise.resolve(null);

      // Render в строгом DOM-порядке: reviews → similar → comments. Каждый
      // await ждёт свой fetch, но не следующий — все они уже летят.
      myRatingP.then(applyMyRating);                  // hero-pill, в любой момент

      const reviewsPage = await reviewsP;
      reviewsPage === null
        ? setMount('reviews-mount', UI.errorState({ onRetry: () => loadContent() }))
        : renderReviews(reviewsPage, c.id);

      const similarItems = await similarP;
      renderSimilar(similarItems, c.id);

      const commentsPage = await commentsP;
      commentsPage === null
        ? setMount('comments-mount', UI.errorState({ onRetry: () => loadComments(c.id) }))
        : renderComments(commentsPage, c.id);
    } catch (e) {
      console.error('[content-detail]', e);
      document.querySelector('main').innerHTML = `
        <div class="g-container" style="padding: 96px 0">
          ${UI.errorState({
            title: e.status === 404 ? 'Фильм не найден' : 'Не удалось загрузить',
            text: e.message,
            onRetry: () => location.reload()
          })}
        </div>`;
    }
  }

  function applyMyRating(r) {
    if (r && r.value) {
      $('my-rating-edit').removeAttribute('hidden');
      $('my-rating-num').textContent = r.value;
      $('cta-rate').classList.add('is-active');
    }
  }

  document.addEventListener('partials:ready', () => {
    loadContent();
    bindRatingModal();
    bindAddPlaylist();
    bindShare();
    UI.initReveal();
  });
})();
