/**
 * page-index.js — главная страница MovieHub.
 *
 * Источники (Refero pattern application):
 *  - Refero 8bba2b13 (Gladia /competitors): bento 2x2 layout, glass cards с
 *    cinema-постерами как featured tile + 4 mini, glass-card с purple glow.
 *  - Refero 8bba2b13 (Gladia stats strip): 4-up data tiles с большими числами.
 *  - Refero 4eea16b4 (Apple TV): visual-first poster grids + horizontal carousel.
 *  - Refero 71af3359 (HBO Max): row of posters grouped by category, hover lift.
 *
 * API:
 *  - GET /api/v1/recommendations/trending?limit=12 (fallback: /content?sort=popular)
 *  - GET /api/v1/recommendations/for-me?limit=12 (auth-only)
 *  - GET /api/v1/content?sort=top&size=12
 *  - GET /api/v1/playlists?isPublic=true&size=8
 *  - GET /api/v1/admin/stats (опционально, для stats-strip)
 */
(function () {
  'use strict';

  // ===== Утилиты компоновки колонок =====

  function playlistCol(p) {
    return `<div class="col-6 col-md-4 col-lg-3">${playlistCard(p)}</div>`;
  }

  function playlistCard(p) {
    const title = UI.escapeHtml(p.title || 'Без названия');
    const owner = p.owner && p.owner.username ? '@' + UI.escapeHtml(p.owner.username) : '';
    const count = p.itemsCount != null ? p.itemsCount : 0;
    const byline = `${owner ? owner + ' · ' : ''}${count} ${UI.pluralize(count, ['фильм', 'фильма', 'фильмов'])}`;
    const hue = (((p.id || 0) * 47) % 360 + 360) % 360;
    const inner = `
      <div class="playlist-cover-overlay">
        <div class="playlist-cover-title">${title}</div>
        <div class="playlist-cover-byline">${byline}</div>
      </div>`;
    if (p.coverImageUrl) {
      return `
        <a class="playlist-card" href="/playlists/${p.id}">
          <div class="playlist-cover">
            <img class="playlist-cover-img" src="${UI.escapeHtml(p.coverImageUrl)}" alt="${title}" loading="lazy">
            ${inner}
          </div>
        </a>`;
    }
    return `
      <a class="playlist-card" href="/playlists/${p.id}">
        <div class="playlist-cover playlist-cover-fallback" style="--hue: ${hue}deg">
          ${inner}
        </div>
      </a>`;
  }

  // ===== Bento rendering — Gladia signature 2x2 layout =====
  // 1 large featured tile (2x2) с full-bleed постером + scrim + meta overlay
  // + 4 mini tiles (1x1) с poster-thumb + title + rating

  function bentoFeatured(item, tint = '') {
    if (!item) return '';
    const url = UI.urlForContent(item);
    const title = UI.escapeHtml(item.title || 'Без названия');
    const year = item.releaseYear || '';
    const tagsLine = item.tags && item.tags.length
      ? UI.escapeHtml(item.tags.slice(0, 2).map(t => t.name).join(' · '))
      : (item.country ? UI.escapeHtml(UI.formatCountry(item.country)) : '');
    const ratingStr = UI.formatRating(item.averageRating);
    const typeLabel = item.contentType === 'SERIES' ? 'Сериал' : 'Фильм';
    const posterStyle = item.posterUrl
      ? `background-image: url('${UI.escapeHtml(item.posterUrl)}');`
      : '';

    return `
      <a class="bento-tile tile-featured tile-2x2 ${tint}" href="${url}" aria-label="${title}">
        <div class="bento-poster" style="${posterStyle}"></div>
        <div class="bento-body">
          <span class="bento-eyebrow">${typeLabel} &middot; В прокате</span>
          <h3>${title}</h3>
          <div class="bento-meta">
            ${year ? `<span>${year}</span>` : ''}
            ${year && tagsLine ? `<span class="dot"></span>` : ''}
            ${tagsLine ? `<span>${tagsLine}</span>` : ''}
            ${ratingStr !== '—' ? `<span class="bento-rating">${UI.iconSparkle({ size: 14 })}${ratingStr}</span>` : ''}
          </div>
        </div>
      </a>`;
  }

  function bentoMini(item, tint = '') {
    if (!item) return '';
    const url = UI.urlForContent(item);
    const title = UI.escapeHtml(item.title || 'Без названия');
    const year = item.releaseYear || '';
    const tagsLine = item.tags && item.tags.length
      ? UI.escapeHtml(item.tags[0].name)
      : (item.country ? UI.escapeHtml(UI.formatCountry(item.country)) : '');
    const ratingStr = UI.formatRating(item.averageRating);
    const posterStyle = item.posterUrl
      ? `background-image: url('${UI.escapeHtml(item.posterUrl)}');`
      : '';
    const letter = UI.escapeHtml(UI.firstChar(item.title));
    const fallbackCss = item.posterUrl
      ? ''
      : `background: linear-gradient(140deg, #2a2c34 0%, #11131a 100%); display:grid; place-items:center; font-family: var(--font-display); font-weight:800; font-size:36px; color: rgba(255,255,255,0.18);`;

    return `
      <a class="bento-tile tile-mini tile-1x1 ${tint}" href="${url}" aria-label="${title}">
        <div class="bento-mini-poster" style="${posterStyle}${fallbackCss}">
          ${item.posterUrl ? '' : `<span>${letter}</span>`}
        </div>
        <div class="bento-mini-body">
          <div>
            <h3 class="bento-mini-title">${title}</h3>
            <div class="bento-mini-meta">${year}${year && tagsLine ? ' · ' : ''}${tagsLine}</div>
          </div>
          ${ratingStr !== '—' ? `<div class="bento-mini-rating">${UI.iconSparkle({ size: 12 })}${ratingStr}</div>` : ''}
        </div>
      </a>`;
  }

  function renderBento(targetId, items) {
    const el = document.getElementById(targetId);
    if (!el) return;
    if (!items || !items.length) {
      el.innerHTML = `<div style="grid-column:1/-1">${UI.emptyState({ title: 'Скоро здесь будет хит проката' })}</div>`;
      return;
    }
    const featured = items[0];
    const minis = items.slice(1, 5);
    const mainHtml = bentoFeatured(featured, '');
    const miniHtml = minis.map((it) => bentoMini(it, '')).join('');
    el.innerHTML = mainHtml + miniHtml;
  }

  function renderBentoSkeleton(targetId) {
    const el = document.getElementById(targetId);
    if (!el) return;
    el.innerHTML = `
      <div class="bento-tile tile-2x2" style="padding:28px">
        <div class="skeleton" style="width:100%;height:100%;border-radius:12px"></div>
      </div>
      <div class="bento-tile tile-1x1" style="padding:18px">
        <div class="skeleton" style="width:100%;height:100%;border-radius:8px"></div>
      </div>
      <div class="bento-tile tile-1x1" style="padding:18px">
        <div class="skeleton" style="width:100%;height:100%;border-radius:8px"></div>
      </div>
      <div class="bento-tile tile-1x1" style="padding:18px">
        <div class="skeleton" style="width:100%;height:100%;border-radius:8px"></div>
      </div>
      <div class="bento-tile tile-1x1" style="padding:18px">
        <div class="skeleton" style="width:100%;height:100%;border-radius:8px"></div>
      </div>`;
  }

  // ===== Skeleton/error helpers для row-rail секций =====

  function showSkeleton(targetId, count = 6) {
    const el = document.getElementById(targetId);
    if (el) el.innerHTML = UI.skeletonGrid(count);
  }

  function showError(targetId, retry) {
    const el = document.getElementById(targetId);
    if (el) el.innerHTML = UI.errorState({ onRetry: retry });
  }

  function showEmpty(targetId, opts) {
    const el = document.getElementById(targetId);
    if (el) el.innerHTML = UI.emptyState(opts);
  }

  function renderGrid(targetId, items) {
    const el = document.getElementById(targetId);
    if (!el) return;
    el.innerHTML = items.map(it => UI.contentCardCol(it)).join('');
  }

  function renderPlaylists(targetId, items) {
    const el = document.getElementById(targetId);
    if (!el) return;
    el.innerHTML = items.map(playlistCol).join('');
  }

  // ---- Загрузка контента ----

  async function loadTrending() {
    showSkeleton('trending-grid', 6);
    try {
      let items = null;
      const recs = await API.recommendations('trending', { limit: 12 });
      if (recs && Array.isArray(recs)) {
        items = UI.unwrapRecs(recs);
      } else if (recs && recs.items) {
        items = recs.items;
      } else {
        const page = await API.listContent({ sort: 'popular', size: 12 });
        items = (page && page.items) || [];
      }
      if (!items || !items.length) {
        showEmpty('trending-grid', { title: 'Скоро здесь будет хит проката' });
      } else {
        renderGrid('trending-grid', items.slice(0, 12));
      }
    } catch (e) {
      console.error('[index] trending', e);
      showError('trending-grid', loadTrending);
    }
  }

  async function loadTopRated() {
    showSkeleton('top-rated-grid', 6);
    try {
      const page = await API.listContent({ sort: 'rating', size: 12 });
      const items = (page && page.items) || [];
      if (!items.length) {
        showEmpty('top-rated-grid', { title: 'Рейтинги ещё не сформировались' });
      } else {
        renderGrid('top-rated-grid', items);
      }
    } catch (e) {
      console.error('[index] top-rated', e);
      showError('top-rated-grid', loadTopRated);
    }
  }

  async function loadPlaylists() {
    showSkeleton('playlists-grid', 4);
    try {
      const page = await API.listPlaylists({ isPublic: true, size: 8 });
      const items = (page && page.items) || [];
      if (!items.length) {
        showEmpty('playlists-grid', { title: 'Подборки появятся скоро' });
      } else {
        renderPlaylists('playlists-grid', items);
      }
    } catch (e) {
      console.error('[index] playlists', e);
      showError('playlists-grid', loadPlaylists);
    }
  }

  async function loadForYou() {
    if (!Auth.isAuthenticated()) return;
    const section = document.getElementById('for-you-section');
    if (!section) return;
    section.removeAttribute('hidden');
    showSkeleton('for-you-grid', 6);
    try {
      let items = null;
      const recs = await API.recommendations('for-me', { limit: 12 });
      if (recs && Array.isArray(recs)) items = UI.unwrapRecs(recs);
      else if (recs && recs.items) items = recs.items;
      else {
        section.setAttribute('hidden', '');
        return;
      }
      if (!items.length) {
        section.setAttribute('hidden', '');
      } else {
        renderGrid('for-you-grid', items);
      }
    } catch (e) {
      console.warn('[index] for-you skipped:', e.message);
      section.setAttribute('hidden', '');
    }
  }

  // ---- Stats strip — параллельно подсчитываем общие цифры ----
  // Для гостей admin/stats недоступен (403/401), берём публичные эндпоинты.

  function setStat(id, value) {
    const el = document.getElementById(id);
    if (!el) return;
    if (value == null) { el.textContent = '—'; return; }
    el.textContent = typeof value === 'string' && /\D/.test(value)
      ? value                       // строки с суффиксом ("30+") — как есть
      : UI.formatCount(value);
  }

  async function loadStats() {
    // Параллельно: контент + публичные подборки + список рецензий PUBLISHED.
    // Каждый pageInfo отдаёт totalElements (Spring Page) — то что нужно.
    try {
      const [contentP, playlistsP, reviewsP] = await Promise.allSettled([
        API.listContent({ size: 1 }),
        API.listPlaylists({ isPublic: true, size: 1 }),
        API.listReviews({ status: 'PUBLISHED', size: 1 })
      ]);

      if (contentP.status === 'fulfilled' && contentP.value) {
        const total = contentP.value.totalElements != null
          ? contentP.value.totalElements
          : (contentP.value.totalItems != null ? contentP.value.totalItems : null);
        setStat('stat-content', total);
      }
      if (playlistsP.status === 'fulfilled' && playlistsP.value) {
        const total = playlistsP.value.totalElements != null
          ? playlistsP.value.totalElements
          : (playlistsP.value.totalItems != null ? playlistsP.value.totalItems : null);
        setStat('stat-playlists', total);
      }
      if (reviewsP.status === 'fulfilled' && reviewsP.value) {
        const total = reviewsP.value.totalElements != null
          ? reviewsP.value.totalElements
          : (reviewsP.value.totalItems != null ? reviewsP.value.totalItems : null);
        setStat('stat-reviews', total);
      }

      // Юзеров в публичном API нет — пробуем admin/stats только если admin
      if (Auth.isAuthenticated() && Auth.user && Auth.user.role === 'ADMIN') {
        try {
          const stats = await API.adminStats();
          if (stats) {
            const u = stats.users || stats.usersTotal || stats.totalUsers;
            if (u != null) setStat('stat-users', u);
          }
        } catch (_) { /* silent */ }
      } else {
        // Фолбэк: показываем оценочное число «30+» как guidance
        setStat('stat-users', '30+');
      }
    } catch (e) {
      console.warn('[index] stats failed:', e.message);
    }
  }

  document.addEventListener('partials:ready', () => {
    loadStats();
    loadForYou();
    loadTrending();
    loadTopRated();
    loadPlaylists();
    UI.initReveal();
    UI.initStatCountUp();
  });
})();
