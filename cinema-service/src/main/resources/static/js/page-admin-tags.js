/**
 * page-admin-tags.js — управление тегами.
 *
 * API:
 *  - GET    /api/v1/tags
 *  - POST   /api/v1/admin/tags { name, slug, description }
 *  - DELETE /api/v1/admin/tags/{id}
 */
(function () {
  'use strict';

  function row(t) {
    return `
      <tr data-id="${t.id}">
        <td class="mono text-muted">${t.id}</td>
        <td>${UI.escapeHtml(t.name)}</td>
        <td class="mono text-muted">${UI.escapeHtml(t.slug)}</td>
        <td class="text-muted">${UI.escapeHtml(t.description || '')}</td>
        <td class="mono">${t.usageCount != null ? t.usageCount : 0}</td>
        <td class="text-end">
          <button class="btn btn-xs btn-outline-danger" data-act="delete">Удалить</button>
        </td>
      </tr>`;
  }

  async function load() {
    const tbody = document.getElementById('tags-tbody');
    tbody.innerHTML = `<tr><td colspan="6" class="text-muted">Загрузка…</td></tr>`;
    try {
      const tags = await API.listTags();
      document.getElementById('tags-count').textContent = `всего: ${tags.length}`;
      if (!tags.length) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-muted">Пока ни одного тега. Добавьте первый сверху.</td></tr>`;
        return;
      }
      tbody.innerHTML = tags.map(row).join('');
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="6" class="text-danger">Ошибка загрузки: ${UI.escapeHtml(e.message || '')}</td></tr>`;
    }
  }

  function showError(msg) {
    const box = document.getElementById('new-tag-error');
    if (!box) return;
    box.textContent = msg;
    box.hidden = !msg;
  }

  function autoSlug(name) {
    if (!name) return '';
    const map = {
      'а':'a','б':'b','в':'v','г':'g','д':'d','е':'e','ё':'e','ж':'zh','з':'z','и':'i','й':'i','к':'k','л':'l','м':'m','н':'n','о':'o','п':'p','р':'r','с':'s','т':'t','у':'u','ф':'f','х':'h','ц':'c','ч':'ch','ш':'sh','щ':'sch','ъ':'','ы':'y','ь':'','э':'e','ю':'yu','я':'ya'
    };
    return name.toLowerCase().split('').map(ch => map[ch] != null ? map[ch] : ch).join('')
      .replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 50);
  }

  function bindForm() {
    const name = document.getElementById('nt-name');
    const slug = document.getElementById('nt-slug');
    let slugDirty = false;
    slug.addEventListener('input', () => { slugDirty = !!slug.value; });
    name.addEventListener('input', () => {
      if (!slugDirty) slug.value = autoSlug(name.value);
    });

    document.getElementById('new-tag-form').addEventListener('submit', async (e) => {
      e.preventDefault();
      showError('');
      const payload = {
        name: name.value.trim(),
        slug: slug.value.trim() || autoSlug(name.value),
        description: document.getElementById('nt-description').value.trim() || null
      };
      if (!payload.name || !payload.slug) {
        showError('Название и slug обязательны');
        return;
      }
      try {
        await API.post('/admin/tags', payload);
        name.value = '';
        slug.value = '';
        document.getElementById('nt-description').value = '';
        slugDirty = false;
        load();
      } catch (err) {
        showError(err.message || 'Не удалось создать тег');
      }
    });
  }

  function bindTable() {
    document.getElementById('tags-tbody').addEventListener('click', async (e) => {
      const btn = e.target.closest('button[data-act="delete"]');
      if (!btn) return;
      const tr = btn.closest('tr');
      const id = tr && tr.dataset.id;
      if (!id) return;
      if (!confirm('Удалить тег? Связи с контентом будут потеряны.')) return;
      try {
        await API.delete(`/admin/tags/${id}`);
        load();
      } catch (err) {
        alert(err.message || 'Не удалось удалить');
      }
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    bindForm();
    bindTable();
    load();
  });
})();
