/**
 * page-review-detail.js — страница рецензии (новый макет в языке главной).
 *
 * API:
 *  - GET /api/v1/reviews/{id} → ReviewDetailResponse{...hasLiked, likeCount}
 *  - POST /api/v1/reviews/{id}/view (анонимно)
 *  - POST /api/v1/reviews/{id}/like → {liked, likeCount} (toggle, требует auth)
 *  - DELETE /api/v1/reviews/{id}
 */
(function () {
  'use strict';

  function getId() {
    const m = location.pathname.match(/\/reviews\/(\d+)/);
    return m ? parseInt(m[1], 10) : null;
  }

  const id = getId();
  let state = { hasLiked: false, likeCount: 0 };

  function paragraphize(text) {
    if (!text) return '';
    return text.split(/\n{2,}/).map(p => `<p>${UI.escapeHtml(p).replace(/\n/g, '<br>')}</p>`).join('');
  }

  function applyLikeState() {
    const btn = document.getElementById('like-btn');
    const count = document.getElementById('like-count');
    btn.classList.toggle('is-liked', !!state.hasLiked);
    btn.setAttribute('aria-pressed', state.hasLiked ? 'true' : 'false');
    count.textContent = UI.formatCount(state.likeCount || 0);
  }

  async function load() {
    if (!id) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Рецензия не найдена' })}</div>`;
      return;
    }
    try {
      const r = await API.reviewById(id);
      document.title = `${r.title} · MovieHub`;

      const c = r.content || null;
      const cHref = c ? UI.urlForContent(c) : '#';
      document.getElementById('content-link').setAttribute('href', cHref);
      document.getElementById('content-title').textContent = c ? c.title : 'Контент';
      const subParts = [];
      if (c && c.releaseYear) subParts.push(c.releaseYear);
      if (c) subParts.push(c.contentType === 'SERIES' ? 'Сериал' : 'Фильм');
      document.getElementById('content-meta').textContent = subParts.join(' · ');

      const posterMount = document.getElementById('poster-mount');
      if (c && c.posterUrl) {
        posterMount.innerHTML = `<img src="${UI.escapeHtml(c.posterUrl)}" alt="${UI.escapeHtml(c.title || '')}" loading="lazy" onerror="this.parentNode.classList.add('is-empty');this.remove();">`;
      } else {
        posterMount.innerHTML = '';
        posterMount.classList.add('is-empty');
      }

      const pill = document.getElementById('score-pill');
      if (r.ratingValue != null) {
        document.getElementById('score-chip-num').textContent = r.ratingValue;
        pill.removeAttribute('hidden');
      }
      const sb = document.getElementById('status-badge');
      if (r.status && r.status !== 'PUBLISHED') {
        const html = UI.reviewStatusBadge(r.status);
        const m = html.match(/class="badge ([^"]+)">([^<]+)</);
        if (m) {
          sb.className = 'badge badge-lg ' + m[1];
          sb.textContent = m[2];
          sb.removeAttribute('hidden');
        }
      }

      document.getElementById('r-title').textContent = r.title || 'Без названия';
      // Причина модерации (use-case D, альт. поток 1) — показываем автору и админу
      const showReason = r.moderationReason
          && Auth.user && r.author
          && (Auth.user.id === r.author.id || Auth.user.role === 'ADMIN');
      if (showReason) {
        const body = document.getElementById('r-body');
        const note = document.createElement('div');
        note.className = 'rv-moderation-note';
        note.innerHTML = `<strong>Причина модерации:</strong> ${UI.escapeHtml(r.moderationReason)}`;
        body.parentNode.insertBefore(note, body);
      }
      document.getElementById('r-body').innerHTML = paragraphize(r.text || '');
      document.getElementById('r-date').textContent = UI.formatDate(r.createdAt);
      document.getElementById('r-views').textContent =
        `${UI.formatCount(r.viewCount || 0)} ${UI.pluralize(r.viewCount || 0, ['просмотр','просмотра','просмотров'])}`;

      if (r.author) {
        const aLink = document.getElementById('author-link');
        aLink.setAttribute('href', `/users/${encodeURIComponent(r.author.username)}`);
        document.getElementById('author-name').textContent = r.author.username;
        const av = document.getElementById('author-avatar');
        av.textContent = (r.author.username || 'U').charAt(0).toUpperCase();
        av.style.setProperty('--avatar-hue', UI.avatarHue(r.author.username || ''));
      }

      state.hasLiked = !!r.hasLiked;
      state.likeCount = r.likeCount || 0;
      applyLikeState();

      if (Auth.isAuthenticated() && r.author && (Auth.user.id === r.author.id || Auth.user.role === 'ADMIN')) {
        const actions = document.getElementById('author-actions');
        actions.removeAttribute('hidden');
        document.getElementById('edit-link').setAttribute('href', `/reviews/${id}/edit`);
      }

      try { API.post(`/reviews/${id}/view`, {}, { requireAuth: false }); } catch (e) {}
    } catch (e) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({
        title: e.status === 404 ? 'Рецензия не найдена' : 'Не удалось загрузить',
        text: e.message,
        onRetry: load
      })}</div>`;
    }
  }

  function bindActions() {
    document.getElementById('like-btn').addEventListener('click', async () => {
      if (!Auth.isAuthenticated()) {
        location.href = `/login?next=${encodeURIComponent(location.pathname)}`;
        return;
      }
      const prev = { ...state };
      state.hasLiked = !state.hasLiked;
      state.likeCount = Math.max(0, state.likeCount + (state.hasLiked ? 1 : -1));
      applyLikeState();
      try {
        const res = await API.post(`/reviews/${id}/like`);
        state.hasLiked = !!res.liked;
        state.likeCount = res.likeCount != null ? res.likeCount : state.likeCount;
        applyLikeState();
      } catch (e) {
        state = prev;
        applyLikeState();
        if (e.status !== 401) alert(e.message || 'Не удалось');
      }
    });
    document.getElementById('delete-btn').addEventListener('click', async () => {
      if (!confirm('Удалить рецензию?')) return;
      try {
        await API.delete(`/reviews/${id}`);
        location.href = '/me/reviews';
      } catch (e) { alert(e.message || 'Не удалось'); }
    });
  }

  document.addEventListener('partials:ready', () => {
    bindActions();
    load();
  });
})();
