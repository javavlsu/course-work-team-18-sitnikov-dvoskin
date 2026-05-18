/**
 * page-playlist-detail.js — просмотр подборки.
 *
 * Refero patterns:
 *  - Spotify playlist page: cover/title/owner left+right hero, list of items below.
 *  - Letterboxd list page: poster-row items.
 *
 * API:
 *  - GET /api/v1/playlists/{id}
 *  - DELETE /api/v1/playlists/{id} (только owner)
 */
(function () {
  'use strict';

  function getId() {
    const m = location.pathname.match(/\/playlists\/(\d+)/);
    return m ? parseInt(m[1], 10) : null;
  }
  const id = getId();

  function realCover(items) {
    const withPoster = (items || []).filter(pi => pi && pi.content && pi.content.posterUrl).slice(0, 4);
    if (!withPoster.length) return '';
    const tiles = withPoster.map(pi =>
      `<img src="${UI.escapeHtml(pi.content.posterUrl)}" alt="" loading="lazy" onerror="this.remove()">`
    );
    while (tiles.length < 4) tiles.push('<div class="cover-mosaic-blank"></div>');
    return `<div class="playlist-hero-cover cover-mosaic">${tiles.join('')}</div>`;
  }

  function itemRow(pi, idx, canEdit) {
    const c = pi.content;
    if (!c) return '';
    const url = c.contentType === 'SERIES' ? `/series/${c.id}` : `/movies/${c.id}`;
    const rating = UI.formatRating(c.averageRating);
    const tags = c.tags && c.tags.length ? c.tags.slice(0, 2).map(t => UI.escapeHtml(t.name)).join(' · ') : '';
    return `
      <div class="playlist-item-row" data-cid="${c.id}">
        <div class="order-num">${String(idx + 1).padStart(2, '0')}</div>
        ${UI.posterImg(c, { sizeClass: 'poster-tile', showRating: false, showType: false })}
        <div class="item-body">
          <a href="${url}" class="item-title text-light">${UI.escapeHtml(c.title)}</a>
          <div class="item-meta">${c.releaseYear || ''}${tags ? ' · ' + tags : ''}${rating !== '—' ? ` · <span class="rating-inline">${UI.iconSparkle({ size: 11 })}<span>${rating}</span></span>` : ''}</div>
        </div>
        <div class="item-actions">
          ${canEdit ? `<button class="btn btn-xs btn-outline-danger" data-action="remove" data-cid="${c.id}">Убрать</button>` : ''}
          <a class="btn btn-xs btn-outline-light" href="${url}">Открыть</a>
        </div>
      </div>`;
  }

  async function load() {
    if (!id) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Подборка не найдена' })}</div>`;
      return;
    }
    try {
      const p = await API.playlistById(id);
      document.title = `${p.title} · MovieHub`;

      // Cover (real mosaic)
      const heroCover = document.getElementById('hero-cover');
      if (p.coverImageUrl) {
        heroCover.innerHTML = `<div class="playlist-hero-cover"><img src="${UI.escapeHtml(p.coverImageUrl)}" alt="${UI.escapeHtml(p.title)}"></div>`;
      } else {
        heroCover.innerHTML = realCover(p.items || []);
      }

      document.getElementById('hero-eyebrow').textContent =
        `Подборка · ${p.itemsCount || 0} ${UI.pluralize(p.itemsCount || 0, ['фильм', 'фильма', 'фильмов'])} · ${p.isPublic ? 'публичная' : 'приватная'}`;
      document.getElementById('hero-title').textContent = p.title;
      document.getElementById('hero-desc').textContent = p.description || '';

      // Owner
      if (p.owner) {
        const link = document.getElementById('owner-link');
        link.setAttribute('href', `/users/${encodeURIComponent(p.owner.username)}`);
        document.getElementById('owner-avatar').textContent = (p.owner.username || 'U').charAt(0).toUpperCase();
        document.getElementById('owner-name').textContent = '@' + p.owner.username;
        document.getElementById('owner-meta').textContent = `Создано ${UI.formatDate(p.createdAt)}`;
      }

      // Owner-only actions
      const isOwner = Auth.isAuthenticated() && p.owner && Auth.user.id === p.owner.id;
      const isAdmin = Auth.isAdmin();
      if (isOwner || isAdmin) {
        document.getElementById('edit-link').setAttribute('href', `/playlists/${id}/edit`);
        document.getElementById('edit-link').removeAttribute('hidden');
        document.getElementById('delete-btn').removeAttribute('hidden');
      }
      document.getElementById('delete-btn').addEventListener('click', async () => {
        if (!confirm('Удалить подборку?')) return;
        try { await API.delete(`/playlists/${id}`); location.href = '/me/playlists'; }
        catch (e) { alert(e.message); }
      });

      // Items
      const mount = document.getElementById('items-mount');
      if (!p.items || !p.items.length) {
        mount.innerHTML = UI.emptyState({
          title: 'В подборке пока пусто',
          text: isOwner ? 'Найдите фильм и добавьте его кнопкой «В подборку».' : 'Скоро здесь появятся фильмы.',
          cta: isOwner ? 'Открыть каталог' : '',
          ctaHref: isOwner ? '/movies' : ''
        });
      } else {
        const visible = p.items.filter(pi => pi && pi.content && pi.content.posterUrl);
        mount.innerHTML = visible.map((pi, idx) => itemRow(pi, idx, isOwner)).join('');
        if (isOwner) {
          mount.querySelectorAll('button[data-action="remove"]').forEach(btn => {
            btn.addEventListener('click', async () => {
              const cid = btn.dataset.cid;
              if (!confirm('Убрать фильм из подборки?')) return;
              try { await API.delete(`/playlists/${id}/items/${cid}`); load(); }
              catch (e) { alert(e.message); }
            });
          });
        }
      }
    } catch (e) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({
        title: e.status === 404 ? 'Подборка не найдена' : 'Не удалось загрузить',
        text: e.message,
        onRetry: load
      })}</div>`;
    }
  }

  document.addEventListener('partials:ready', load);
})();
