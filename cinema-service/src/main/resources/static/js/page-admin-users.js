/**
 * page-admin-users.js
 *
 * API:
 *  - GET /api/v1/admin/users?role=&active=&page=
 *  - PATCH /api/v1/admin/users/{id}/role { role }
 *  - PATCH /api/v1/admin/users/{id}/status { isActive }
 *  - DELETE /api/v1/admin/users/{id}
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { role: '', active: '', page: 0 };

  function row(u) {
    const roleBadge  = u.role === 'ADMIN' ? 'badge-admin' : 'badge-user';
    const stateBadge = u.isActive ? 'badge-published' : 'badge-rejected';
    const roleLabel  = UI.roleLabel(u.role);
    const stateLabel = u.isActive ? 'Активен' : 'Заблокирован';
    return `
      <tr data-uid="${u.id}" data-role="${u.role}" data-active="${u.isActive ? '1' : '0'}">
        <td class="mono text-muted">${u.id}</td>
        <td><a class="text-light" href="/users/${encodeURIComponent(u.username)}" target="_blank">@${UI.escapeHtml(u.username)}</a></td>
        <td class="text-muted">${UI.escapeHtml(u.email || '')}</td>
        <td><span class="badge ${roleBadge}">${roleLabel}</span></td>
        <td><span class="badge ${stateBadge}">${stateLabel}</span></td>
        <td class="text-muted">${UI.formatDate(u.createdAt)}</td>
        <td class="text-end">
          <button class="btn btn-xs btn-outline-light" data-action="toggle-role">${u.role === 'ADMIN' ? 'Снять админа' : 'Сделать админом'}</button>
          <button class="btn btn-xs btn-outline-light" data-action="toggle-active">${u.isActive ? 'Заблокировать' : 'Разблокировать'}</button>
          <button class="btn btn-xs btn-outline-danger" data-action="delete">Удалить</button>
        </td>
      </tr>`;
  }

  async function load() {
    const tbody = document.getElementById('users-tbody');
    tbody.innerHTML = `<tr><td colspan="7" class="text-muted">Загрузка…</td></tr>`;
    document.getElementById('pagination-mount').innerHTML = '';
    try {
      const params = { page: state.page, size: PAGE_SIZE };
      if (state.role) params.role = state.role;
      if (state.active !== '') params.active = state.active;
      const page = await API.adminUsers(params);
      const items = (page && page.items) || [];
      document.getElementById('users-count').textContent =
        `${page.totalElements} ${UI.pluralize(page.totalElements, ['пользователь','пользователя','пользователей'])}`;
      if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7">${UI.emptyState({ title: 'Никого не найдено' })}</td></tr>`;
      } else {
        tbody.innerHTML = items.map(row).join('');
        bindActions();
        document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => { state.page = p; load(); });
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="7">${UI.errorState({ onRetry: load })}</td></tr>`;
    }
  }

  function bindActions() {
    document.querySelectorAll('tr[data-uid]').forEach(tr => {
      const uid = tr.dataset.uid;
      tr.querySelectorAll('button[data-action]').forEach(btn => {
        btn.addEventListener('click', async () => {
          const action = btn.dataset.action;
          try {
            if (action === 'toggle-role') {
              const next = tr.dataset.role === 'ADMIN' ? 'USER' : 'ADMIN';
              await API.patch(`/admin/users/${uid}/role`, { role: next });
            } else if (action === 'toggle-active') {
              const next = tr.dataset.active !== '1';
              await API.patch(`/admin/users/${uid}/status`, { isActive: next });
            } else if (action === 'delete') {
              await API.delete(`/admin/users/${uid}`);
            }
            load();
          } catch (e) { console.error('[admin-users]', e); }
        });
      });
    });
  }

  function bindFilters() {
    document.getElementById('role-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-role]'); if (!b) return;
      state.role = b.dataset.role; state.page = 0;
      document.querySelectorAll('#role-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.role === state.role));
      load();
    });
    document.getElementById('active-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-active]'); if (!b) return;
      state.active = b.dataset.active; state.page = 0;
      document.querySelectorAll('#active-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.active === state.active));
      load();
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    bindFilters();
    load();
  });
})();
