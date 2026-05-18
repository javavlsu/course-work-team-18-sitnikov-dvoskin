/**
 * page-admin-content.js — управление контентом (status workflow).
 *
 * API:
 *  - GET /api/v1/admin/content?status=&type=&page=
 *  - PATCH /api/v1/admin/content/{id}/status { status }
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { status: '', type: '', page: 0 };

  function statusBadge(s) {
    const map = {
      DRAFT: ['badge-draft', 'Черновик'],
      PUBLISHED: ['badge-published', 'Публикация'],
      HIDDEN: ['badge-hidden', 'Скрыт'],
      DELETED: ['badge-deleted', 'Удалён']
    };
    const [cls, label] = map[s] || ['badge-draft', s];
    return `<span class="badge ${cls}">${label}</span>`;
  }

  function row(c) {
    const url = c.contentType === 'SERIES' ? `/series/${c.id}` : `/movies/${c.id}`;
    return `
      <tr>
        <td class="mono text-muted">${c.id}</td>
        <td><a class="text-light" href="${url}" target="_blank">${UI.escapeHtml(c.title)}</a></td>
        <td><span class="badge badge-hidden">${c.contentType}</span></td>
        <td class="mono">${c.releaseYear || ''}</td>
        <td class="mono text-gold">${UI.formatRating(c.averageRating)}</td>
        <td>${statusBadge(c.status)}</td>
        <td class="text-muted small">${UI.formatDate(c.createdAt)}</td>
        <td class="text-end">
          <select class="form-select form-select-sm d-inline-block" style="width:auto" data-cid="${c.id}">
            <option value="">Изменить статус…</option>
            <option value="PUBLISHED">Опубликовать</option>
            <option value="DRAFT">В черновики</option>
            <option value="HIDDEN">Скрыть</option>
            <option value="DELETED">Удалить</option>
          </select>
        </td>
      </tr>`;
  }

  async function load() {
    const tbody = document.getElementById('content-tbody');
    tbody.innerHTML = `<tr><td colspan="8" class="text-muted">Загрузка…</td></tr>`;
    document.getElementById('pagination-mount').innerHTML = '';
    try {
      const params = { page: state.page, size: PAGE_SIZE };
      if (state.status) params.status = state.status;
      if (state.type) params.type = state.type;
      const page = await API.adminContent(params);
      const items = (page && page.items) || [];
      document.getElementById('content-count').textContent = `${page.totalElements} ${UI.pluralize(page.totalElements, ['запись', 'записи', 'записей'])}`;
      if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="8">${UI.emptyState({ title: 'Нет контента', text: 'Сменить фильтры?' })}</td></tr>`;
      } else {
        tbody.innerHTML = items.map(row).join('');
        bindStatusSelects();
        document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => { state.page = p; load(); });
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8">${UI.errorState({ onRetry: load })}</td></tr>`;
    }
  }

  function bindStatusSelects() {
    document.querySelectorAll('select[data-cid]').forEach(sel => {
      sel.addEventListener('change', async () => {
        if (!sel.value) return;
        const cid = sel.dataset.cid;
        try {
          await API.patch(`/admin/content/${cid}/status`, { status: sel.value });
          load();
        } catch (e) {
          alert(e.message || 'Не удалось');
        }
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
    document.getElementById('type-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-type]'); if (!b) return;
      state.type = b.dataset.type; state.page = 0;
      document.querySelectorAll('#type-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.type === state.type));
      load();
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    bindFilters();
    load();
  });
})();
