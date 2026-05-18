/**
 * Auth — простой клиент-сайд helper для управления токеном и текущим юзером.
 * Хранит accessToken + user в localStorage.
 *
 * Используется во всех страницах (подключается до partials.js).
 *
 * Pattern: Spotify Web Player auth state (refero search "spotify auth dropdown").
 */
(function (global) {
  'use strict';

  const KEY_TOKEN = 'mh.accessToken';
  const KEY_REFRESH = 'mh.refreshToken';
  const KEY_USER = 'mh.user';

  const Auth = {
    get token() {
      try { return localStorage.getItem(KEY_TOKEN); } catch (e) { return null; }
    },
    set token(v) {
      try { v ? localStorage.setItem(KEY_TOKEN, v) : localStorage.removeItem(KEY_TOKEN); } catch (e) {}
    },

    get refreshToken() {
      try { return localStorage.getItem(KEY_REFRESH); } catch (e) { return null; }
    },
    set refreshToken(v) {
      try { v ? localStorage.setItem(KEY_REFRESH, v) : localStorage.removeItem(KEY_REFRESH); } catch (e) {}
    },

    get user() {
      try {
        const raw = localStorage.getItem(KEY_USER);
        return raw ? JSON.parse(raw) : null;
      } catch (e) { return null; }
    },
    set user(v) {
      try { v ? localStorage.setItem(KEY_USER, JSON.stringify(v)) : localStorage.removeItem(KEY_USER); } catch (e) {}
    },

    isAuthenticated() {
      return !!this.token && !!this.user;
    },

    isAdmin() {
      return this.isAuthenticated() && this.user && this.user.role === 'ADMIN';
    },

    headers() {
      return this.token ? { 'Authorization': `Bearer ${this.token}` } : {};
    },

    /**
     * Сохранить ответ /auth/login или /auth/register.
     */
    saveAuth(authResponse) {
      if (!authResponse) return;
      this.token = authResponse.accessToken;
      this.refreshToken = authResponse.refreshToken;
      this.user = authResponse.user;
    },

    logout() {
      this.token = null;
      this.refreshToken = null;
      this.user = null;
      // best-effort logout endpoint, без ожидания
      try { fetch('/api/v1/auth/logout', { method: 'POST', headers: this.headers() }); } catch (e) {}
      const next = encodeURIComponent(location.pathname + location.search);
      location.href = '/?logged_out=1';
    },

    /**
     * Редирект на /login?next=... — для страниц, требующих авторизации.
     */
    requireAuth() {
      if (this.isAuthenticated()) return true;
      const next = encodeURIComponent(location.pathname + location.search);
      location.href = `/login?next=${next}`;
      return false;
    },

    /**
     * Редирект на главную если не админ.
     */
    requireAdmin() {
      if (!this.requireAuth()) return false;
      if (!this.isAdmin()) {
        location.href = '/?error=admin_required';
        return false;
      }
      return true;
    }
  };

  global.Auth = Auth;
})(window);
