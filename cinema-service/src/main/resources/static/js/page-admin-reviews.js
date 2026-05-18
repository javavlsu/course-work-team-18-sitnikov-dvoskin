/**
 * page-admin-reviews.js — модерация рецензий.
 *
 * API:
 *  - GET /api/v1/admin/reviews?status=&page=
 *  - PATCH /api/v1/admin/reviews/{id}/status { status }
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { status: 'MODERATION', page: 0 };

  function statusBadge(s) {
    const map = {
      DRAFT: ['badge-draft', 'Черновик'],
      MODERATION: ['badge-moderation', 'На модерации'],
      PUBLISHED: ['badge-published', 'Опубликована'],
      REJECTED: ['badge-rejected', 'Отклонена'],
      HIDDEN: ['badge-hidden', 'Скрыта'],
      DELETED: ['badge-deleted', 'Удалена']
    };
    const [cls, label] = map[s] || ['badge-draft', s];
    return `<span class="badge ${cls}">${label}</span>`;
  }

  function row(r) {
    const author = r.author ? `@${UI.escapeHtml(r.author.username)}` : 'anon';
    const cTitle = r.content ? UI.escapeHtml(r.content.title) : '—';
    return `
      <div class="list-row" data-rid="${r.id}">
        <div class="list-row-body">
          <div class="d-flex gap-2 align-items-center mb-2">
            ${statusBadge(r.status)}
            <span class="text-muted small">${author} · на «${cTitle}» · ${UI.formatDate(r.createdAt)}</span>
          </div>
          <div class="list-row-title">
            <a href="/reviews/${r.id}" target="_blank" class="text-light">${UI.escapeHtml(r.title || 'Без названия')}</a>
          </div>
          <div class="text-muted small">${r.viewCount || 0} просмотров · ♥ ${r.likeCount || 0}</div>
        </div>
        <div class="d-flex flex-column gap-2 align-items-end">
          <a class="btn btn-xs btn-ghost" href="/reviews/${r.id}" target="_blank">Открыть</a>
          ${r.status !== 'PUBLISHED' ? `<button class="btn btn-xs btn-outline-gold" data-action="PUBLISHED">Опубликовать</button>` : ''}
          ${r.status !== 'REJECTED' ? `<button class="btn btn-xs btn-outline-danger" data-action="REJECTED">Отклонить</button>` : ''}
          ${r.status !== 'HIDDEN' ? `<button class="btn btn-xs btn-outline-light" data-action="HIDDEN">Скрыть</button>` : ''}
        </div>
      </div>`;
  }

  async function load() {
    const mount = document.getElementById('reviews-mount');
    mount.innerHTML = UI.skeletonList(4);
    try {
      const params = { page: state.page, size: PAGE_SIZE };
      if (state.status) params.status = state.status;
      const page = await API.adminReviews(params);
      const items = (page && page.items) || [];
      document.getElementById('rev-count').textContent = `${page.totalElements} ${UI.pluralize(page.totalElements, ['рецензия', 'рецензии', 'рецензий'])}`;
      if (!items.length) {
        mount.innerHTML = UI.emptyState({ title: 'Очередь пуста', text: 'Все рецензии в этом статусе обработаны.' });
        document.getElementById('pagination-mount').innerHTML = '';
      } else {
        mount.innerHTML = items.map(row).join('');
        bindActions();
        document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => { state.page = p; load(); });
      }
    } catch (e) {
      mount.innerHTML = UI.errorState({ onRetry: load });
    }
  }

  function bindActions() {
    document.querySelectorAll('div[data-rid]').forEach(div => {
      const rid = div.dataset.rid;
      div.querySelectorAll('button[data-action]').forEach(btn => {
        btn.addEventListener('click', async () => {
          const action = btn.dataset.action;
          let reason = null;
          // Для REJECTED/HIDDEN модератор обязан указать причину (use-case D, альт. поток 1).
          if (action === 'REJECTED' || action === 'HIDDEN') {
            reason = prompt(action === 'REJECTED' ? 'Причина отклонения:' : 'Причина скрытия:');
            if (reason == null) return;          // отменили
            reason = reason.trim();
            if (!reason) return;                  // пустую причину не принимаем
          }
          try {
            await API.patch(`/admin/reviews/${rid}/status`, { status: action, reason: reason });
            load();
          } catch (e) { console.error('[admin-reviews]', e); }
        });
      });
    });
  }

  function bindFilters() {
    document.getElementById('status-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-status]'); if (!b) return;
      state.status = b.dataset.status; state.page = 0;
      document.querySelectorAll('#status-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.status === state.status));
      load();
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    bindFilters();
    load();
  });
})();
