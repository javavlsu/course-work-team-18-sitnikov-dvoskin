/**
 * page-my-playlists.js
 *
 * Refero patterns:
 *  - Spotify "Your Library" — grid карточек с mosaic-cover, public/private badge.
 *  - Letterboxd Lists — title + count + owner.
 *
 * API: GET /api/v1/users/{me.username}/playlists
 */
(function () {
  'use strict';

  function playlistCard(p) {
    return `
      <div class="col-6 col-md-4 col-lg-3">
        <a class="playlist-card" href="/playlists/${p.id}">
          <div class="playlist-cover playlist-cover-mosaic" aria-hidden="true">
            <div></div><div></div><div></div><div></div>
          </div>
          <div class="playlist-meta">
            <div class="d-flex align-items-center gap-2 mb-1">
              <span class="badge ${p.isPublic ? 'badge-published' : 'badge-hidden'}">${p.isPublic ? 'public' : 'private'}</span>
            </div>
            <div class="playlist-title">${UI.escapeHtml(p.title)}</div>
            <div class="playlist-byline">${p.itemsCount || 0} ${UI.pluralize(p.itemsCount || 0, ['фильм','фильма','фильмов'])}</div>
          </div>
        </a>
      </div>`;
  }

  async function load() {
    const grid = document.getElementById('grid');
    grid.innerHTML = '<div class="col-12"><div class="skeleton" style="height:200px;border-radius:var(--r-lg)"></div></div>';
    const me = Auth.user;
    if (!me) return;
    try {
      const list = await API.userPlaylists(me.username);
      if (!list.length) {
        grid.innerHTML = `<div class="col-12">${UI.emptyState({
          title: 'Здесь будут ваши подборки',
          text: 'Создайте первую коллекцию любимых фильмов.',
          cta: 'Создать подборку',
          ctaHref: '/playlists/new'
        })}</div>`;
      } else {
        grid.innerHTML = list.map(playlistCard).join('');
      }
    } catch (e) {
      grid.innerHTML = `<div class="col-12">${UI.errorState({ onRetry: load })}</div>`;
    }
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    load();
  });
})();
