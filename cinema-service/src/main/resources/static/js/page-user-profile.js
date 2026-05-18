/**
 * page-user-profile.js — публичный профиль /users/{username}.
 *
 * API:
 *  - GET /api/v1/users/{username} → PublicUserResponse + UserStats
 *  - GET /api/v1/users/{username}/reviews
 *  - GET /api/v1/users/{username}/playlists
 */
(function () {
  'use strict';

  function getUsername() {
    const m = location.pathname.match(/\/users\/([^/]+)/);
    return m ? decodeURIComponent(m[1]) : null;
  }

  const username = getUsername();

  function playlistCard(p) {
    return `
      <div class="col-6 col-md-4 col-lg-3">
        <a class="playlist-card" href="/playlists/${p.id}">
          <div class="playlist-cover playlist-cover-mosaic"><div></div><div></div><div></div><div></div></div>
          <div class="playlist-meta">
            <div class="playlist-title">${UI.escapeHtml(p.title)}</div>
            <div class="playlist-byline">${p.itemsCount || 0} ${UI.pluralize(p.itemsCount || 0, ['фильм','фильма','фильмов'])}</div>
          </div>
        </a>
      </div>`;
  }

  async function load() {
    if (!username) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Пользователь не указан' })}</div>`;
      return;
    }
    try {
      const u = await API.userByUsername(username);
      document.title = `@${u.username} · MovieHub`;
      document.getElementById('pub-name').textContent = u.username;
      document.getElementById('pub-handle').textContent = '@' + u.username;
      document.getElementById('pub-meta').textContent = `${UI.roleLabel(u.role)} · с ${UI.formatDate(u.createdAt)}`;
      document.getElementById('pub-avatar').textContent = (u.username || 'U').charAt(0).toUpperCase();

      const stats = u.stats || {};
      document.getElementById('pub-stats').innerHTML = `
        <div class="profile-stat-tile"><div class="num">${stats.reviewsCount || 0}</div><div class="label">Рецензии</div></div>
        <div class="profile-stat-tile"><div class="num">${stats.playlistsCount || 0}</div><div class="label">Подборки</div></div>
        <div class="profile-stat-tile"><div class="num">${stats.ratingsCount || 0}</div><div class="label">Оценки</div></div>
        <div class="profile-stat-tile"><div class="num">${stats.averageRatingGiven != null ? Number(stats.averageRatingGiven).toFixed(1) : '—'}</div><div class="label">Средняя</div></div>
      `;

      loadReviews();
      loadPlaylists();
    } catch (e) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({
        title: e.status === 404 ? 'Пользователь не найден' : 'Не удалось загрузить',
        text: e.message,
        onRetry: load
      })}</div>`;
    }
  }

  async function loadReviews() {
    const mount = document.getElementById('reviews-mount');
    mount.innerHTML = UI.skeletonList(3);
    try {
      const page = await API.userReviews(username, 0, 12);
      const items = (page && page.items) || [];
      document.getElementById('cnt-reviews').textContent = page.totalElements;
      if (!items.length) {
        mount.innerHTML = UI.emptyState({ title: 'Этот пользователь ещё не писал рецензий' });
      } else {
        mount.innerHTML = items.map(r => UI.reviewRow(r, { hideStatus: true })).join('');
      }
    } catch (e) {
      mount.innerHTML = UI.errorState({ onRetry: loadReviews });
    }
  }

  async function loadPlaylists() {
    const mount = document.getElementById('playlists-mount');
    mount.innerHTML = '<div class="col-12"><div class="skeleton" style="height:120px;border-radius:var(--r-lg)"></div></div>';
    try {
      const list = await API.userPlaylists(username);
      document.getElementById('cnt-playlists').textContent = list.length;
      if (!list.length) {
        mount.innerHTML = `<div class="col-12">${UI.emptyState({ title: 'Подборок ещё нет' })}</div>`;
      } else {
        mount.innerHTML = list.map(playlistCard).join('');
      }
    } catch (e) {
      mount.innerHTML = `<div class="col-12">${UI.errorState({ onRetry: loadPlaylists })}</div>`;
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

  document.addEventListener('partials:ready', () => {
    bindTabs();
    load();
  });
})();
