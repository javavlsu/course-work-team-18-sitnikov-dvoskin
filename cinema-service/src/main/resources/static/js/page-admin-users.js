/**
 * page-admin-users.js
 *
 * API:
 *  - GET /api/v1/admin/users?role=&active=&page=
 *  - PATCH /api/v1/admin/users/{id} { role?, isActive? }
 *  - DELETE /api/v1/admin/users/{id}
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { role: '', active: '', page: 0 };

  function row(u) {
    const roleBadge = u.role === 'ADMIN' ? 'badge-admin' : 'badge-user';
    const stateBadge = u.isActive ? 'badge-published' : 'badge-rejected';
    return `
      <tr data-uid="${u.id}">
        <td class="mono text-muted">${u.id}</td>
        <td>
          <div class="d-flex align-items-center gap-2">
            <span class="user-avatar" style="width:28px;height:28px;font-size:12px">${(u.username || 'U').charAt(0).toUpperCase()}</span>
            <a class="text-light" href="/users/${encodeURIComponent(u.username)}" target="_blank">@${UI.escapeHtml(u.username)}</a>
          </div>
        </td>
        <td class="text-muted small">${UI.escapeHtml(u.email || '')}</td>
        <td><span class="badge ${roleBadge}">${u.role}</span></td>
        <td><span class="badge ${stateBadge}">${u.isActive ? 'active' : 'blocked'}</span></td>
        <td class="text-muted small">${UI.formatDate(u.createdAt)}</td>
        <td class="text-end">
          <button class="btn btn-xs btn-outline-light" data-action="toggle-role">${u.role === 'ADMIN' ? 'Снять admin' : 'Сделать admin'}</button>
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
      document.getElementById('users-count').textContent = `${page.totalElements} ${UI.pluralize(page.totalElements, ['пользователь', 'пользователя', 'пользователей'])}`;
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
              const cur = tr.querySelector('.badge.badge-admin') ? 'ADMIN' : 'USER';
              await API.patch(`/admin/users/${uid}`, { role: cur === 'ADMIN' ? 'USER' : 'ADMIN' });
            } else if (action === 'toggle-active') {
              const isActive = !!tr.querySelector('.badge.badge-published');
              await API.patch(`/admin/users/${uid}`, { isActive: !isActive });
            } else if (action === 'delete') {
              if (!confirm('Удалить пользователя? Это действие нельзя отменить.')) return;
              await API.delete(`/admin/users/${uid}`);
            }
            load();
          } catch (e) { alert(e.message || 'Не удалось'); }
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
