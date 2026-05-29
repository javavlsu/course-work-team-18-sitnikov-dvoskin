/**
 * page-playlist-edit.js — редактирование подборки.
 *
 * API:
 *  - GET /api/v1/playlists/{id}
 *  - PATCH /api/v1/playlists/{id} {title?, description?, coverImageUrl?, isPublic?}
 *  - POST /api/v1/playlists/{id}/items {contentId}
 *  - DELETE /api/v1/playlists/{id}/items/{contentId}
 *  - PATCH /api/v1/playlists/{id}/items/reorder { items: [{contentId, sortOrder}, ...] }
 *
 * UI: сортировка через ↑/↓ (а не drag-only-visual handle, как было раньше).
 */
(function () {
  'use strict';

  function getId() {
    const m = location.pathname.match(/\/playlists\/(\d+)\/edit/);
    return m ? parseInt(m[1], 10) : null;
  }
  const id = getId();
  let PLAYLIST = null;
  let debounce = null;

  function showError(m) {
    const e = document.getElementById('form-error');
    e.classList.remove('is-success');
    e.textContent = m;
    e.removeAttribute('hidden');
  }
  function showSuccess(m) {
    const e = document.getElementById('form-success');
    e.textContent = m;
    e.removeAttribute('hidden');
    setTimeout(() => e.setAttribute('hidden', ''), 2000);
  }

  async function load() {
    if (!id) return;
    try {
      const p = await API.playlistById(id);
      PLAYLIST = p;
      // Только owner может редактировать (на бэке тоже проверяется)
      if (Auth.user && p.owner && p.owner.id !== Auth.user.id && !Auth.isAdmin()) {
        location.href = `/playlists/${id}`;
        return;
      }
      document.getElementById('back-link').setAttribute('href', `/playlists/${id}`);
      document.getElementById('p-title').value = p.title || '';
      document.getElementById('p-desc').value = p.description || '';
      document.getElementById('p-cover').value = p.coverImageUrl || '';
      document.getElementById('p-public').checked = !!p.isPublic;
      renderItems(p.items || []);
    } catch (e) {
      showError(e.message || 'Не удалось загрузить');
    }
  }

  function renderItems(items) {
    const mount = document.getElementById('items-mount');
    if (!items.length) {
      mount.innerHTML = `<p class="text-muted small">Подборка пуста — добавьте фильмы выше.</p>`;
      return;
    }
    mount.innerHTML = items.map((pi, idx) => {
      const c = pi.content;
      if (!c) return '';
      return `
        <div class="playlist-item-row" data-cid="${c.id}">
          <div class="order-num">${String(idx + 1).padStart(2, '0')}</div>
          ${UI.posterImg(c, { sizeClass: 'poster-tile', showRating: false, showType: false })}
          <div class="item-body">
            <div class="item-title">${UI.escapeHtml(c.title)}</div>
            <div class="item-meta">${c.releaseYear || ''}${c.contentType === 'SERIES' ? ' · Сериал' : ' · Фильм'}</div>
          </div>
          <div class="item-actions">
            <button class="btn btn-xs btn-outline-light" data-move="up" data-cid="${c.id}" ${idx === 0 ? 'disabled' : ''} title="Переместить вверх">↑</button>
            <button class="btn btn-xs btn-outline-light" data-move="down" data-cid="${c.id}" ${idx === items.length - 1 ? 'disabled' : ''} title="Переместить вниз">↓</button>
            <button class="btn btn-xs btn-outline-danger" data-action="remove" data-cid="${c.id}">×</button>
          </div>
        </div>`;
    }).join('');

    mount.querySelectorAll('button[data-action="remove"]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const cid = btn.dataset.cid;
        try {
          await API.delete(`/playlists/${id}/items/${cid}`);
          load();
        } catch (e) { alert(e.message); }
      });
    });

    mount.querySelectorAll('button[data-move]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const cid = parseInt(btn.dataset.cid, 10);
        const dir = btn.dataset.move;
        const arr = items.slice();
        const i = arr.findIndex(x => x.content && x.content.id === cid);
        if (i < 0) return;
        const j = dir === 'up' ? i - 1 : i + 1;
        if (j < 0 || j >= arr.length) return;
        [arr[i], arr[j]] = [arr[j], arr[i]];
        const payload = { items: arr.map((x, idx) => ({ contentId: x.content.id, sortOrder: idx })) };
        try {
          const updated = await API.patch(`/playlists/${id}/items/reorder`, payload);
          renderItems(updated.items || []);
        } catch (e) { alert(e.message); }
      });
    });
  }

  function bindForm() {
    document.getElementById('edit-form').addEventListener('submit', async (ev) => {
      ev.preventDefault();
      const payload = {
        title: document.getElementById('p-title').value.trim(),
        description: document.getElementById('p-desc').value.trim() || null,
        coverImageUrl: document.getElementById('p-cover').value.trim() || null,
        isPublic: document.getElementById('p-public').checked
      };
      try {
        await API.patch(`/playlists/${id}`, payload);
        showSuccess('Сохранено');
      } catch (e) { showError(e.message || 'Не удалось сохранить'); }
    });
  }

  function bindAddSearch() {
    const input = document.getElementById('add-search');
    const sug = document.getElementById('add-suggestions');
    input.addEventListener('input', () => {
      clearTimeout(debounce);
      const q = input.value.trim();
      if (q.length < 2) { sug.innerHTML = ''; return; }
      debounce = setTimeout(async () => {
        try {
          const page = await API.search({ q, size: 5 });
          const items = (page && page.items) || [];
          if (!items.length) { sug.innerHTML = `<p class="text-muted small">Ничего не нашлось</p>`; return; }
          sug.innerHTML = items.map(it => `
            <button type="button" class="list-row text-start w-100 border-0 bg-transparent" data-id="${it.id}">
              ${UI.posterImg(it, { sizeClass: 'poster-row', showRating: false, showType: false })}
              <div class="list-row-body">
                <div class="list-row-title" style="font-size:14px">${UI.escapeHtml(it.title)}</div>
                <div class="text-muted small">${it.releaseYear || ''} · ${it.contentType === 'SERIES' ? 'Сериал' : 'Фильм'}</div>
              </div>
            </button>
          `).join('');
          sug.querySelectorAll('button[data-id]').forEach(btn => {
            btn.addEventListener('click', async () => {
              const cid = parseInt(btn.dataset.id, 10);
              try {
                await API.post(`/playlists/${id}/items`, { contentId: cid });
                input.value = ''; sug.innerHTML = '';
                load();
              } catch (e) { alert(e.message); }
            });
          });
        } catch (e) { sug.innerHTML = `<p class="text-danger small">${e.message}</p>`; }
      }, 250);
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindForm();
    bindAddSearch();
    load();
  });
})();
