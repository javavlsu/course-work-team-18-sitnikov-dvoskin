/**
 * page-playlist-create.js
 *
 * Pattern: Spotify "Create playlist" — minimal form, immediate redirect to detail.
 *
 * API: POST /api/v1/playlists { title, description, coverImageUrl, isPublic }
 *      После успеха: если ?contentId=... → POST /playlists/{id}/items {contentId}
 */
(function () {
  'use strict';

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;

    const form = document.getElementById('create-form');
    const err = document.getElementById('form-error');

    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      err.setAttribute('hidden', '');
      const title = document.getElementById('p-title').value.trim();
      if (!title) { err.textContent = 'Введите название'; err.removeAttribute('hidden'); return; }
      const visibility = document.querySelector('input[name="p-visibility"]:checked');
      const payload = {
        title,
        description: document.getElementById('p-desc').value.trim() || null,
        coverImageUrl: document.getElementById('p-cover').value.trim() || null,
        isPublic: visibility ? visibility.value === 'public' : true
      };

      try {
        const res = await API.post('/playlists', payload);
        // если пришли с ?contentId=... — добавим контент сразу
        const p = new URLSearchParams(location.search);
        const cid = p.get('contentId');
        if (cid) {
          try { await API.post(`/playlists/${res.id}/items`, { contentId: parseInt(cid, 10) }); } catch (e) {}
        }
        location.href = `/playlists/${res.id}`;
      } catch (e) {
        err.textContent = e.message || 'Не удалось создать';
        err.removeAttribute('hidden');
      }
    });
  });
})();
