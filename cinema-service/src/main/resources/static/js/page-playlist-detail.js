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
    const labels = (c.genres && c.genres.length) ? c.genres : (c.tags || []);
    const tags = labels.length ? labels.slice(0, 2).map(t => UI.escapeHtml(t.name)).join(' · ') : '';
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

  // ===== Rating block (5 stars, по образу content-detail) =====
  const ratingState = { my: 0, average: 0, count: 0 };

  function renderRatingSummary() {
    const sum = document.getElementById('pl-rating-summary');
    if (!sum) return;
    if (ratingState.count > 0) {
      sum.hidden = false;
      sum.innerHTML = `${UI.iconSparkle({ size: 14 })}<span>${UI.formatRating(ratingState.average)}</span><span class="text-muted">· ${ratingState.count} ${UI.pluralize(ratingState.count, ['голос', 'голоса', 'голосов'])}</span>`;
    } else {
      sum.hidden = true;
    }
  }
  function paintStars(value) {
    document.querySelectorAll('#pl-stars button').forEach(b => {
      b.classList.toggle('is-active', +b.dataset.v <= value);
    });
    const rm = document.getElementById('pl-rating-remove');
    if (rm) rm.hidden = !(value > 0);
  }
  function bindRating() {
    const root = document.getElementById('pl-stars');
    if (!root) return;
    root.innerHTML = UI.starRatingTemplate({ max: 5 });
    const authed = Auth.isAuthenticated();
    if (!authed) {
      // Анонимам показываем подсказку и блокируем клики
      const hint = document.getElementById('pl-rating-auth-hint');
      if (hint) {
        hint.hidden = false;
        const a = document.getElementById('pl-rating-login-link');
        if (a) a.href = `/login?next=${encodeURIComponent(location.pathname)}`;
      }
      root.querySelectorAll('button').forEach(b => b.disabled = true);
      return;
    }
    const stars = root.querySelectorAll('button');
    stars.forEach(b => {
      b.addEventListener('mouseenter', () => {
        const v = +b.dataset.v;
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= v));
      });
      b.addEventListener('click', async () => {
        const v = +b.dataset.v;
        try {
          const res = await API.put(`/playlists/${id}/rating`, { value: v });
          ratingState.my = res.value;
          ratingState.average = res.average || 0;
          ratingState.count = res.count || 0;
          paintStars(ratingState.my);
          renderRatingSummary();
        } catch (e) {
          alert(e.message || 'Не удалось сохранить оценку');
        }
      });
    });
    root.addEventListener('mouseleave', () => paintStars(ratingState.my));

    const rm = document.getElementById('pl-rating-remove');
    if (rm) rm.addEventListener('click', async () => {
      try {
        await API.delete(`/playlists/${id}/rating`);
        ratingState.my = 0;
        // count/average пересчитаем отдельным GET — backend на DELETE не возвращает summary
        try {
          const me = await API.get(`/playlists/${id}/rating/me`);
          if (me) {
            ratingState.average = me.average || 0;
            ratingState.count = me.count || 0;
          } else {
            // Если /me вернул 204 — мы единственный кто голосовал; пересчёт через GET подборки
            const p2 = await API.playlistById(id);
            ratingState.average = p2.ratingAverage || 0;
            ratingState.count = p2.ratingsCount || 0;
          }
        } catch {}
        paintStars(0);
        renderRatingSummary();
      } catch (e) {
        alert(e.message || 'Не удалось убрать оценку');
      }
    });
  }
  async function loadMyRating() {
    if (!Auth.isAuthenticated()) return;
    try {
      const me = await API.get(`/playlists/${id}/rating/me`);
      if (me) {
        ratingState.my = me.value || 0;
        paintStars(ratingState.my);
      }
    } catch (e) {
      if (e && e.status === 401) return;
      console.warn('[playlist] rating/me', e);
    }
  }

  async function load() {
    if (!id) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Подборка не найдена' })}</div>`;
      return;
    }
    try {
      const p = await API.playlistById(id);
      document.title = `${p.title} · MovieHub`;

      ratingState.average = p.ratingAverage || 0;
      ratingState.count = p.ratingsCount || 0;
      bindRating();
      renderRatingSummary();
      loadMyRating();

      // Cover (real mosaic)
      const heroCover = document.getElementById('hero-cover');
      if (p.coverImageUrl) {
        heroCover.innerHTML = `<div class="playlist-hero-cover"><img src="${UI.escapeHtml(p.coverImageUrl)}" alt="${UI.escapeHtml(p.title)}"></div>`;
      } else {
        heroCover.innerHTML = realCover(p.items || []);
      }

      document.getElementById('hero-eyebrow').textContent =
        `Подборка · ${p.itemsCount || 0} ${UI.pluralize(p.itemsCount || 0, ['фильм', 'фильма', 'фильмов'])} · ${p.isPublic ? 'Публичная' : 'Приватная'}`;
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
