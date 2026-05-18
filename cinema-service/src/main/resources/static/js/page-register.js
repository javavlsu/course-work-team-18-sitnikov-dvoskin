/**
 * page-register.js
 *
 * API: POST /api/v1/auth/register {username, email, password} → AuthResponse (201)
 * После успеха: автологин (toast saved) + redirect /
 */
(function () {
  'use strict';

  document.addEventListener('partials:ready', () => {
    if (Auth.isAuthenticated()) {
      location.href = '/profile';
      return;
    }

    const form = document.getElementById('reg-form');
    const err = document.getElementById('reg-error');
    const submit = document.getElementById('reg-submit');

    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      err.setAttribute('hidden', '');

      const username = document.getElementById('reg-username').value.trim();
      const email = document.getElementById('reg-email').value.trim();
      const password = document.getElementById('reg-pw').value;

      if (!/^[A-Za-z0-9_]{3,50}$/.test(username)) {
        err.textContent = 'Username: 3–50 символов, только латиница, цифры и _';
        err.removeAttribute('hidden');
        return;
      }
      if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) {
        err.textContent = 'Введите корректный email';
        err.removeAttribute('hidden');
        return;
      }
      if (password.length < 8) {
        err.textContent = 'Пароль должен быть минимум 8 символов';
        err.removeAttribute('hidden');
        return;
      }

      submit.disabled = true;
      submit.textContent = 'Создаём…';
      try {
        const res = await API.register({ username, email, password });
        Auth.saveAuth(res);
        location.href = '/profile';
      } catch (e) {
        let msg = e.message;
        if (e.status === 409) msg = 'Такой username или email уже занят';
        if (e.body && e.body.errors) {
          msg = Object.values(e.body.errors).join('. ');
        }
        err.textContent = msg || 'Не удалось зарегистрироваться';
        err.removeAttribute('hidden');
      } finally {
        submit.disabled = false;
        submit.textContent = 'Создать аккаунт';
      }
    });
  });
})();
