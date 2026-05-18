/**
 * page-profile.js — мой профиль (требует auth).
 *
 * Refero patterns:
 *  - Letterboxd profile (avatar + name + 4-stat grid + tab pills).
 *
 * API:
 *  - GET /api/v1/users/me — UserResponse + UserStats
 *  - GET /api/v1/users/{username}/reviews — мои рецензии
 *  - GET /api/v1/users/{username}/playlists — мои подборки
 *  - PATCH /api/v1/users/me — обновить профиль
 */
(function () {
  'use strict';

  let ME = null;

  function reviewListItem(r) {
    const cId = r.content && r.content.id;
    const url = cId ? (r.content.contentType === 'SERIES' ? `/series/${cId}` : `/movies/${cId}`) : '#';
    return `
      <a class="list-row" href="/reviews/${r.id}">
        <div class="list-row-body">
          <div class="d-flex align-items-center gap-2 mb-1">
            ${r.ratingValue ? `<span class="rating-badge">${r.ratingValue}</span>` : ''}
            <span class="badge badge-${(r.status || 'PUBLISHED').toLowerCase()}">${r.status || 'PUBLISHED'}</span>
          </div>
          <div class="list-row-title">${UI.escapeHtml(r.title || 'Без названия')}</div>
          <div class="text-muted small">${r.content ? UI.escapeHtml(r.content.title) : ''} · ${UI.formatDate(r.createdAt)}</div>
        </div>
        <div class="text-muted small">♥ ${r.likeCount || 0}</div>
      </a>`;
  }

  function playlistCard(p) {
    return `
      <div class="col-6 col-md-4 col-lg-3">
        <a class="playlist-card" href="/playlists/${p.id}">
          <div class="playlist-cover playlist-cover-mosaic" aria-hidden="true">
            <div></div><div></div><div></div><div></div>
          </div>
          <div class="playlist-meta">
            <div class="playlist-title">${UI.escapeHtml(p.title)}</div>
            <div class="playlist-byline">${p.itemsCount || 0} ${UI.pluralize(p.itemsCount || 0, ['фильм','фильма','фильмов'])} · ${p.isPublic ? 'публичная' : 'приватная'}</div>
          </div>
        </a>
      </div>`;
  }

  function fillStats(stats) {
    const tiles = document.getElementById('prof-stats');
    tiles.innerHTML = `
      <div class="profile-stat-tile"><div class="num">${stats.reviewsCount || 0}</div><div class="label">Рецензии</div></div>
      <div class="profile-stat-tile"><div class="num">${stats.playlistsCount || 0}</div><div class="label">Подборки</div></div>
      <div class="profile-stat-tile"><div class="num">${stats.ratingsCount || 0}</div><div class="label">Оценки</div></div>
      <div class="profile-stat-tile"><div class="num">${stats.averageRatingGiven != null ? Number(stats.averageRatingGiven).toFixed(1) : '—'}</div><div class="label">Средняя</div></div>
    `;
  }

  async function loadMe() {
    try {
      const me = await API.me();
      ME = me;
      document.getElementById('prof-name').textContent = me.username || 'Пользователь';
      document.getElementById('prof-handle').textContent = '@' + (me.username || 'user');
      document.getElementById('prof-meta').textContent =
        `${me.role || 'USER'} · с ${UI.formatDate(me.createdAt)} · ${me.email}`;
      const av = document.querySelector('.profile-header .user-avatar');
      if (av) av.textContent = (me.username || 'U').charAt(0).toUpperCase();

      if (me.stats) fillStats(me.stats);

      // Settings form prefill
      document.getElementById('set-username').value = me.username || '';
      document.getElementById('set-email').value = me.email || '';

      loadMyReviews(me.username);
      loadMyPlaylists(me.username);
    } catch (e) {
      // 401 уже обработан в api.js — редирект на login
      console.error('[profile] me', e);
    }
  }

  async function loadMyReviews(username) {
    const mount = document.getElementById('reviews-mount');
    mount.innerHTML = UI.skeletonList(3);
    try {
      const page = await API.userReviews(username, 0, 8);
      const items = (page && page.items) || [];
      document.getElementById('cnt-reviews').textContent = page.totalElements;
      if (!items.length) {
        mount.innerHTML = UI.emptyState({
          title: 'Ещё нет рецензий',
          text: 'Поделитесь мыслями о любимом фильме.',
          cta: 'Написать рецензию',
          ctaHref: '/reviews/new'
        });
      } else {
        mount.innerHTML = items.map(reviewListItem).join('');
      }
    } catch (e) {
      mount.innerHTML = UI.errorState({ onRetry: () => loadMyReviews(username) });
    }
  }

  async function loadMyPlaylists(username) {
    const mount = document.getElementById('playlists-mount');
    mount.innerHTML = '<div class="col-12"><div class="skeleton" style="height:120px;border-radius:var(--r-lg)"></div></div>';
    try {
      const list = await API.userPlaylists(username);
      document.getElementById('cnt-playlists').textContent = list.length;
      if (!list.length) {
        mount.innerHTML = `<div class="col-12">${UI.emptyState({
          title: 'Подборок пока нет',
          text: 'Создайте первую коллекцию любимых фильмов.',
          cta: 'Создать подборку',
          ctaHref: '/playlists/new'
        })}</div>`;
      } else {
        mount.innerHTML = list.map(playlistCard).join('');
      }
    } catch (e) {
      mount.innerHTML = `<div class="col-12">${UI.errorState({ onRetry: () => loadMyPlaylists(username) })}</div>`;
    }
  }

  function bindTabs() {
    const tabs = document.querySelectorAll('.tab-pill');
    tabs.forEach(t => {
      t.addEventListener('click', () => {
        tabs.forEach(x => x.classList.remove('is-active'));
        t.classList.add('is-active');
        document.querySelectorAll('.tab-content').forEach(c => c.setAttribute('hidden', ''));
        const target = document.getElementById('tab-' + t.dataset.tab);
        if (target) target.removeAttribute('hidden');
      });
    });
  }

  function bindSettings() {
    const form = document.getElementById('settings-form');
    const success = document.getElementById('settings-success');
    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      const username = document.getElementById('set-username').value.trim();
      const email = document.getElementById('set-email').value.trim();
      try {
        const me = await API.patch('/users/me', { username, email });
        Auth.user = me;
        ME = me;
        success.classList.add('is-success');
        success.textContent = 'Сохранено';
        success.removeAttribute('hidden');
        setTimeout(() => success.setAttribute('hidden', ''), 2000);
      } catch (e) {
        success.classList.remove('is-success');
        success.textContent = e.message || 'Не удалось сохранить';
        success.removeAttribute('hidden');
      }
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindTabs();
    bindSettings();
    loadMe();
  });
})();
