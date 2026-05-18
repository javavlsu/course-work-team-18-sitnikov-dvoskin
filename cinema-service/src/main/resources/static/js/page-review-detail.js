/**
 * page-review-detail.js — отдельная страница рецензии.
 *
 * Refero patterns:
 *  - Medium article (narrow column, large headline, author block).
 *  - Letterboxd review (score pill + content header above title).
 *
 * API:
 *  - GET /api/v1/reviews/{id}
 *  - POST /api/v1/reviews/{id}/view (autoincrement, fire-and-forget)
 *  - POST /api/v1/reviews/{id}/like
 *  - DELETE /api/v1/reviews/{id} (только автор/админ)
 */
(function () {
  'use strict';

  function getId() {
    const m = location.pathname.match(/\/reviews\/(\d+)/);
    return m ? parseInt(m[1], 10) : null;
  }

  const id = getId();

  function paragraphize(text) {
    if (!text) return '';
    return text.split(/\n{2,}/).map(p => `<p>${UI.escapeHtml(p).replace(/\n/g, '<br>')}</p>`).join('');
  }

  function statusBadge(s) {
    const map = {
      DRAFT: ['badge-draft', 'Черновик'],
      MODERATION: ['badge-moderation', 'На модерации'],
      PUBLISHED: ['badge-published', 'Опубликована'],
      REJECTED: ['badge-rejected', 'Отклонена'],
      HIDDEN: ['badge-hidden', 'Скрыта']
    };
    const [cls, label] = map[s] || ['', ''];
    return cls ? `${label}` : '';
  }

  async function load() {
    if (!id) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Рецензия не найдена' })}</div>`;
      return;
    }
    try {
      const r = await API.reviewById(id);
      document.title = `${r.title} · MovieHub`;

      // header content link
      const cId = r.content && r.content.id;
      const cType = r.content && r.content.contentType;
      const url = cId ? (cType === 'SERIES' ? `/series/${cId}` : `/movies/${cId}`) : '#';
      document.getElementById('content-link').setAttribute('href', url);
      document.getElementById('content-title').textContent = r.content ? r.content.title : 'Контент';
      document.getElementById('content-meta').textContent = r.content ? (r.content.contentType === 'SERIES' ? 'Сериал' : 'Фильм') : '';
      // poster
      if (r.content) {
        document.getElementById('poster-mount').innerHTML = UI.posterImg({
          title: r.content.title,
          posterUrl: r.content.posterUrl
        }, { sizeClass: 'poster-row', showRating: false, showType: false });
      }

      // score + status
      document.getElementById('score-num').textContent = r.ratingValue != null ? r.ratingValue : '—';
      const sb = document.getElementById('status-badge');
      sb.innerHTML = statusBadge(r.status);
      sb.className = 'badge badge-' + (r.status || '').toLowerCase();
      if (r.status === 'PUBLISHED') sb.style.display = 'none';

      document.getElementById('r-title').textContent = r.title || 'Без названия';
      document.getElementById('r-body').innerHTML = paragraphize(r.text || '');
      document.getElementById('r-date').textContent = UI.formatDate(r.createdAt);
      document.getElementById('r-views').textContent = `${r.viewCount || 0} просмотров`;

      // author block
      if (r.author) {
        const aLink = document.getElementById('author-link');
        aLink.setAttribute('href', `/users/${encodeURIComponent(r.author.username)}`);
        document.getElementById('author-name').textContent = r.author.username;
        document.getElementById('author-handle').textContent = '@' + r.author.username;
        document.getElementById('author-avatar').textContent = (r.author.username || 'U').charAt(0).toUpperCase();
      }

      document.getElementById('like-count').textContent = r.likeCount || 0;

      // author actions
      if (Auth.isAuthenticated() && r.author && (Auth.user.id === r.author.id || Auth.user.role === 'ADMIN')) {
        const actions = document.getElementById('author-actions');
        actions.removeAttribute('hidden');
        document.getElementById('edit-link').setAttribute('href', `/reviews/${id}/edit`);
      }

      // increment view (public)
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
      try {
        const r = await API.post(`/reviews/${id}/like`);
        document.getElementById('like-count').textContent = r.likeCount || 0;
      } catch (e) { alert(e.message || 'Не удалось'); }
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
