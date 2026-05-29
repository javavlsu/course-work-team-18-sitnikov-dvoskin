/**
 * page-login.js
 *
 * Refero patterns:
 *  - Spotify login (centered card + bold heading + gold CTA).
 *  - Apple ID sign-in (минимальная форма, без маркетинга).
 *
 * API: POST /api/v1/auth/login {emailOrUsername, password} → AuthResponse
 * После успеха: redirect на ?next=... либо на /
 */
(function () {
  'use strict';

  function nextUrl() {
    const p = new URLSearchParams(location.search);
    const n = p.get('next');
    return n && n.startsWith('/') ? n : '/';
  }

  document.addEventListener('partials:ready', () => {
    if (Auth.isAuthenticated()) {
      location.href = nextUrl();
      return;
    }

    const form = document.getElementById('login-form');
    const err = document.getElementById('login-error');
    const submit = document.getElementById('login-submit');

    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      err.setAttribute('hidden', '');

      const emailOrUsername = document.getElementById('login-id').value.trim();
      const password = document.getElementById('login-pw').value;

      if (!emailOrUsername || password.length < 8) {
        err.textContent = 'Заполните поля корректно';
        err.removeAttribute('hidden');
        return;
      }

      submit.disabled = true;
      submit.textContent = 'Входим…';
      try {
        const res = await API.login(emailOrUsername, password);
        Auth.saveAuth(res);
        location.href = nextUrl();
      } catch (e) {
        err.textContent = e.status === 401 ? 'Неверный логин или пароль' : (e.message || 'Не удалось войти');
        err.removeAttribute('hidden');
      } finally {
        submit.disabled = false;
        submit.textContent = 'Войти';
      }
    });
  });
})();
