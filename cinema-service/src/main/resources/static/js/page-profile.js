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


  function playlistCard(p) {
    return `<div class="col-6 col-md-4 col-lg-3">${UI.playlistCard(p, { showVisibility: true })}</div>`;
  }

  function fillStats(stats) {
    const tiles = document.getElementById('prof-stats');
    tiles.innerHTML = `
      <button type="button" class="profile-stat-tile" data-jump="reviews"><div class="num">${stats.reviewsCount || 0}</div><div class="label">Рецензии</div></button>
      <button type="button" class="profile-stat-tile" data-jump="playlists"><div class="num">${stats.playlistsCount || 0}</div><div class="label">Подборки</div></button>
      <div class="profile-stat-tile"><div class="num">${stats.ratingsCount || 0}</div><div class="label">Оценки</div></div>
      <div class="profile-stat-tile"><div class="num">${stats.averageRatingGiven != null ? Number(stats.averageRatingGiven).toFixed(1) : '—'}</div><div class="label">Средняя</div></div>
    `;
    tiles.querySelectorAll('[data-jump]').forEach(b => {
      b.addEventListener('click', () => switchTab(b.dataset.jump));
    });
  }

  function switchTab(name) {
    document.querySelectorAll('.tab-pill').forEach(t => t.classList.toggle('is-active', t.dataset.tab === name));
    document.querySelectorAll('.tab-content').forEach(c => c.setAttribute('hidden', ''));
    const target = document.getElementById('tab-' + name);
    if (target) target.removeAttribute('hidden');
    window.scrollTo({ top: document.querySelector('.tab-pills').offsetTop - 24, behavior: 'smooth' });
  }

  function renderMetaChips(me) {
    const chips = [];
    const role = me.role || 'USER';
    chips.push(`<span class="profile-chip profile-chip-role profile-chip-${role.toLowerCase()}">${UI.escapeHtml(UI.roleLabel(role))}</span>`);
    if (me.createdAt) chips.push(`<span class="profile-chip">с ${UI.formatDate(me.createdAt)}</span>`);
    if (me.email) chips.push(`<span class="profile-chip profile-chip-muted">${UI.escapeHtml(me.email)}</span>`);
    document.getElementById('prof-meta-chips').innerHTML = chips.join('');
  }

  async function loadMe() {
    try {
      const me = await API.me();
      document.getElementById('prof-name').textContent = me.username || 'Пользователь';
      document.getElementById('prof-handle').textContent = '@' + (me.username || 'user');
      renderMetaChips(me);
      const av = document.querySelector('.profile-header .user-avatar');
      if (av) {
        av.textContent = (me.username || 'U').charAt(0).toUpperCase();
        av.style.setProperty('--avatar-hue', UI.avatarHue(me.username || ''));
      }

      if (me.stats) fillStats(me.stats);

      loadMyReviews(me.username);
      loadMyPlaylists(me.username);
    } catch (e) {
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
        const seeMore = page.totalElements > items.length
          ? `<a class="view-all-link" href="/me/reviews">Все рецензии (${page.totalElements}) →</a>`
          : '';
        mount.innerHTML = items.map(r => UI.reviewRow(r)).join('') + seeMore;
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
        const seeMore = list.length >= 12
          ? `<div class="col-12"><a class="view-all-link" href="/me/playlists">Все подборки →</a></div>`
          : '';
        mount.innerHTML = list.map(playlistCard).join('') + seeMore;
      }
    } catch (e) {
      mount.innerHTML = `<div class="col-12">${UI.errorState({ onRetry: () => loadMyPlaylists(username) })}</div>`;
    }
  }

  function bindTabs() {
    document.querySelectorAll('.tab-pill').forEach(t => {
      t.addEventListener('click', () => switchTab(t.dataset.tab));
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindTabs();
    loadMe();
  });
})();
