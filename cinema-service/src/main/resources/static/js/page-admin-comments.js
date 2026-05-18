/**
 * page-admin-comments.js — модерация комментариев (use-case Этап 2).
 * API: GET /api/v1/admin/comments?status=...&page=&size= ; PATCH /api/v1/admin/comments/{id}/status
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const STATE = { status: 'MODERATION', page: 0 };
  const LABELS = {
    MODERATION: 'На модерации',
    PUBLISHED:  'Опубликован',
    HIDDEN:     'Скрыт',
    DELETED:    'Удалён'
  };
  const BADGE = {
    MODERATION: 'badge-moderation',
    PUBLISHED:  'badge-published',
    HIDDEN:     'badge-hidden',
    DELETED:    'badge-deleted'
  };

  function contentLink(c) {
    if (!c.content) return '';
    const href = UI.urlForContent(c.content);
    const typeLabel = c.content.contentType === 'SERIES' ? 'сериалу' : 'фильму';
    return `<span class="text-muted small">к ${typeLabel} <a class="subtle" href="${href}">«${UI.escapeHtml(c.content.title)}»</a></span>`;
  }

  function row(c) {
    return `
      <div class="list-row" data-id="${c.id}">
        <div class="list-row-body">
          <div class="d-flex align-items-center gap-2 mb-2 flex-wrap">
            <span class="badge ${BADGE[c.status] || 'badge-draft'}">${LABELS[c.status] || c.status}</span>
            <span class="text-muted small">${c.author ? '@' + UI.escapeHtml(c.author.username) : ''}</span>
            <span class="text-muted small">${UI.formatDate(c.createdAt)}</span>
            ${contentLink(c)}
          </div>
          <div class="text-light">${UI.escapeHtml(c.text || '')}</div>
        </div>
        <div class="list-row-actions">
          ${c.status !== 'PUBLISHED' ? `<button class="rv-action" data-act="PUBLISHED" data-id="${c.id}">Опубликовать</button>` : ''}
          ${c.status !== 'HIDDEN'    ? `<button class="rv-action" data-act="HIDDEN"    data-id="${c.id}">Скрыть</button>`     : ''}
          ${c.status !== 'DELETED'   ? `<button class="rv-action rv-action-danger" data-act="DELETED" data-id="${c.id}">Удалить</button>` : ''}
        </div>
      </div>`;
  }

  async function load() {
    const mount = document.getElementById('comments-mount');
    mount.innerHTML = UI.skeletonList(4);
    try {
      const page = await API.get(`/admin/comments?status=${STATE.status}&page=${STATE.page}&size=${PAGE_SIZE}`);
      const items = (page && page.items) || [];
      document.getElementById('cm-count').textContent =
        `${UI.formatCount(page.totalElements)} ${UI.pluralize(page.totalElements, ['комментарий','комментария','комментариев'])}`;
      if (!items.length) {
        mount.innerHTML = UI.emptyState({ title: 'Нет комментариев в этом статусе' });
        document.getElementById('pagination-mount').innerHTML = '';
        return;
      }
      mount.innerHTML = items.map(row).join('');
      document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => {
        STATE.page = p; window.scrollTo({ top: 0, behavior: 'smooth' }); load();
      });
      bindRowActions();
    } catch (e) {
      mount.innerHTML = UI.errorState({ onRetry: load });
    }
  }

  function bindRowActions() {
    document.querySelectorAll('button[data-act]').forEach(b => {
      b.addEventListener('click', async () => {
        const id = b.dataset.id;
        const status = b.dataset.act;
        try {
          await API.patch(`/admin/comments/${id}/status`, { status });
          load();
        } catch (e) {}
      });
    });
  }

  function bindFilter() {
    document.querySelectorAll('#status-group .filter-chip').forEach(c => {
      c.addEventListener('click', () => {
        document.querySelectorAll('#status-group .filter-chip').forEach(x => x.classList.remove('is-active'));
        c.classList.add('is-active');
        STATE.status = c.dataset.status;
        STATE.page = 0;
        load();
      });
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    if (!Auth.user || Auth.user.role !== 'ADMIN') {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Нет доступа' })}</div>`;
      return;
    }
    bindFilter();
    load();
  });
})();
