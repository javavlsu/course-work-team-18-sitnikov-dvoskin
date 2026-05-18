/**
 * page-settings.js — /settings: профиль (логин/почта) + выход.
 * API: GET /api/v1/users/me, PATCH /api/v1/users/me
 */
(function () {
  'use strict';

  function bindNav() {
    const items = document.querySelectorAll('.settings-nav-item');
    items.forEach(b => {
      b.addEventListener('click', () => {
        items.forEach(x => x.classList.toggle('is-active', x === b));
        document.querySelectorAll('[data-section-panel]').forEach(p => {
          p.toggleAttribute('hidden', p.dataset.sectionPanel !== b.dataset.section);
        });
      });
    });
  }

  async function loadMe() {
    try {
      const me = await API.me();
      document.getElementById('set-username').value = me.username || '';
      document.getElementById('set-email').value = me.email || '';
    } catch (e) {
      console.error('[settings] me', e);
    }
  }

  function bindForm() {
    const form = document.getElementById('settings-form');
    const success = document.getElementById('settings-success');
    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      const username = document.getElementById('set-username').value.trim();
      const email = document.getElementById('set-email').value.trim();
      try {
        const me = await API.patch('/users/me', { username, email });
        Auth.user = me;
        success.classList.add('is-success');
        success.textContent = 'Сохранено';
        success.removeAttribute('hidden');
        setTimeout(() => success.setAttribute('hidden', ''), 2000);
      } catch (e) {
        success.classList.remove('is-success');
        success.textContent = e.message || 'Не удалось сохранить';
        success.removeAttribute('hidden');
      }
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindNav();
    bindForm();
    loadMe();
  });
})();
