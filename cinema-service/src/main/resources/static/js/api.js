/**
 * API — централизованный fetch-helper для всех страниц.
 * Базовый URL — /api/v1, добавляет Authorization: Bearer ... если токен есть.
 *
 * Все методы возвращают Promise<json> или бросают APIError(status, message, body).
 *
 * Pattern: централизованный clients из больших SPA (Letterboxd JS, Linear API).
 */
(function (global) {
  'use strict';

  const BASE = '/api/v1';

  class APIError extends Error {
    constructor(status, message, body) {
      super(message);
      this.status = status;
      this.body = body;
      this.name = 'APIError';
    }
  }

  // Silent refresh: при 401 пробуем POST /auth/refresh с refreshToken (живёт 7 дней),
  // обновляем access+refresh, повторяем исходный запрос. Все параллельные 401 ждут
  // одну и ту же Promise — без штормового шквала refresh'ей.
  let _refreshing = null;
  async function refreshTokens() {
    if (!global.Auth || !global.Auth.refreshToken) return false;
    if (_refreshing) return _refreshing;
    _refreshing = (async () => {
      try {
        const res = await fetch(`${BASE}/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
          body: JSON.stringify({ refreshToken: global.Auth.refreshToken })
        });
        if (!res.ok) return false;
        const auth = await res.json();
        if (!auth || !auth.accessToken) return false;
        global.Auth.token = auth.accessToken;
        if (auth.refreshToken) global.Auth.refreshToken = auth.refreshToken;
        if (auth.user) global.Auth.user = auth.user;
        return true;
      } catch (e) {
        return false;
      }
    })();
    try { return await _refreshing; }
    finally { _refreshing = null; }
  }

  async function request(path, opts = {}) {
    const url = path.startsWith('http') ? path : `${BASE}${path}`;
    const headers = {
      'Accept': 'application/json',
      ...(opts.body && !(opts.body instanceof FormData) ? { 'Content-Type': 'application/json' } : {}),
      ...(global.Auth ? global.Auth.headers() : {}),
      ...(opts.headers || {})
    };

    const init = { ...opts, headers };
    if (init.body && typeof init.body === 'object' && !(init.body instanceof FormData)) {
      init.body = JSON.stringify(init.body);
    }

    const res = await fetch(url, init);

    if (res.status === 401 && opts.requireAuth !== false) {
      const isAuthCall = typeof path === 'string' && path.startsWith('/auth/');
      if (!isAuthCall && !opts._retried && global.Auth && global.Auth.refreshToken) {
        const ok = await refreshTokens();
        if (ok) {
          return request(path, { ...opts, _retried: true });
        }
      }
      if (global.Auth) {
        global.Auth.token = null;
        global.Auth.refreshToken = null;
        global.Auth.user = null;
      }
      const next = encodeURIComponent(location.pathname + location.search);
      location.href = `/login?next=${next}`;
      throw new APIError(401, 'Не авторизован');
    }

    if (res.status === 204) return null;

    let body = null;
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('application/json')) {
      body = await res.json().catch(() => null);
    } else {
      body = await res.text().catch(() => null);
    }

    if (!res.ok) {
      const msg = (body && (body.message || body.error)) || `HTTP ${res.status}`;
      throw new APIError(res.status, msg, body);
    }

    return body;
  }

  const API = {
    APIError,

    get:    (p, opts)        => request(p, { ...opts, method: 'GET' }),
    post:   (p, body, opts)  => request(p, { ...opts, method: 'POST', body }),
    put:    (p, body, opts)  => request(p, { ...opts, method: 'PUT', body }),
    patch:  (p, body, opts)  => request(p, { ...opts, method: 'PATCH', body }),
    delete: (p, opts)        => request(p, { ...opts, method: 'DELETE' }),

    // ===== Convenience helpers — используются на множестве страниц =====

    listContent(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/content?${q.toString()}`);
    },

    contentById(id) {
      return this.get(`/content/${id}`);
    },

    listMovies(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/movies?${q.toString()}`);
    },

    listSeries(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/series?${q.toString()}`);
    },

    search(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/search?${q.toString()}`);
    },

    listTags() {
      return this.get('/tags');
    },

    listGenres() {
      return this.get('/genres');
    },

    contentComments(id, page = 0, size = 20) {
      return this.get(`/content/${id}/comments?page=${page}&size=${size}`);
    },

    listReviews(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/reviews?${q.toString()}`);
    },

    reviewById(id) {
      return this.get(`/reviews/${id}`);
    },

    listPlaylists(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/playlists?${q.toString()}`);
    },

    playlistById(id) {
      return this.get(`/playlists/${id}`);
    },

    me() {
      return this.get('/users/me');
    },

    userByUsername(username) {
      return this.get(`/users/${username}`);
    },

    userReviews(username, page = 0, size = 20) {
      return this.get(`/users/${username}/reviews?page=${page}&size=${size}`);
    },

    userPlaylists(username) {
      return this.get(`/users/${username}/playlists`);
    },

    // Recommendations — endpoint появится в BE-этапе. Если 404 — тихо вернём null,
    // вызывающий код сделает фолбэк на /content?sort=...
    async recommendations(kind, params = {}) {
      try {
        const q = new URLSearchParams();
        Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
        const path = kind.startsWith('similar/')
          ? `/recommendations/${kind}?${q.toString()}`
          : `/recommendations/${kind}?${q.toString()}`;
        return await this.get(path);
      } catch (e) {
        if (e.status === 404) return null;
        throw e;
      }
    },

    login(emailOrUsername, password) {
      return this.post('/auth/login', { emailOrUsername, password });
    },

    register(payload) {
      return this.post('/auth/register', payload);
    },

    // === Admin ===
    adminStats() {
      return this.get('/admin/stats');
    },
    adminUsers(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/admin/users?${q.toString()}`);
    },
    adminContent(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/admin/content?${q.toString()}`);
    },
    adminReviews(params = {}) {
      const q = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => { if (v !== undefined && v !== null && v !== '') q.set(k, v); });
      return this.get(`/admin/reviews?${q.toString()}`);
    }
  };

  global.API = API;
})(window);
