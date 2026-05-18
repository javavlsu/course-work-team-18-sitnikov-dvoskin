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
    // visibility badge показываем поверх для своих подборок (видеть приват/публ),
    // owner не нужен — это страница «мои»
    const card = UI.playlistCard(p, { showVisibility: true });
    return `<div class="col-6 col-md-4 col-lg-3">${card}</div>`;
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
