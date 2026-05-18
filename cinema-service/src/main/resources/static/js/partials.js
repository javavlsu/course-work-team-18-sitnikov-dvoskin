/**
 * Partials loader — подгружает navbar и footer на каждую страницу.
 *
 * Использование на странице:
 *   <header data-include="navbar"></header>
 *   <footer data-include="footer"></footer>
 *
 * Дополнительно: подсветка активного nav-link по pathname; рендер auth-блоков
 * (data-auth-guest / data-auth-user) на основе window.Auth.
 *
 * Pattern: классический client-side include + auth-aware nav (Letterboxd dropdown).
 */
(function () {
  'use strict';

  async function loadPartial(el) {
    const name = el.dataset.include;
    if (!name) return;
    try {
      const res = await fetch(`/partials/${name}.html`, { cache: 'no-cache' });
      if (!res.ok) throw new Error(`partial ${name} → ${res.status}`);
      el.innerHTML = await res.text();
    } catch (err) {
      console.warn('[partials] failed to load', name, err);
    }
  }

  function highlightActive() {
    const path = location.pathname;
    document.querySelectorAll('header [data-include="navbar"], header.navbar, nav.navbar').forEach((nav) => {
      nav.querySelectorAll('a.nav-link[href]').forEach((a) => {
        const href = a.getAttribute('href');
        if (!href) return;
        if (href === path) {
          a.classList.add('active');
        } else if (href !== '/' && path.startsWith(href)) {
          a.classList.add('active');
        }
      });
    });
  }

  function applyAuthState() {
    const auth = window.Auth;
    const user = auth && auth.user;
    const guestEls = document.querySelectorAll('[data-auth-guest]');
    const userEls  = document.querySelectorAll('[data-auth-user]');
    const adminEls = document.querySelectorAll('[data-auth-admin]');

    if (auth && auth.isAuthenticated()) {
      guestEls.forEach((e) => e.setAttribute('hidden', ''));
      userEls.forEach((e) => e.removeAttribute('hidden'));

      const isAdmin = auth.isAdmin();
      adminEls.forEach((e) => isAdmin ? e.removeAttribute('hidden') : e.setAttribute('hidden', ''));

      // hydrate avatar + username
      document.querySelectorAll('[data-user-username]').forEach((el) => {
        el.textContent = '@' + (user.username || 'user');
      });
      document.querySelectorAll('[data-user-avatar]').forEach((el) => {
        el.textContent = (user.username || 'U').charAt(0).toUpperCase();
      });
    } else {
      guestEls.forEach((e) => e.removeAttribute('hidden'));
      userEls.forEach((e) => e.setAttribute('hidden', ''));
      adminEls.forEach((e) => e.setAttribute('hidden', ''));
    }
  }

  function bindAuthHandlers() {
    document.querySelectorAll('[data-auth-logout]').forEach((el) => {
      el.addEventListener('click', (ev) => {
        ev.preventDefault();
        if (window.Auth) window.Auth.logout();
      });
    });
  }

  function bindNavToggle() {
    const toggle = document.querySelector('[data-g-nav-toggle]');
    const menu = document.querySelector('[data-g-nav-menu]');
    if (!toggle || !menu) return;
    toggle.setAttribute('aria-expanded', 'false');
    toggle.addEventListener('click', () => {
      const open = menu.classList.toggle('is-open');
      toggle.classList.toggle('is-open', open);
      toggle.setAttribute('aria-expanded', String(open));
      document.body.classList.toggle('g-nav-locked', open);
    });
    menu.querySelectorAll('a').forEach((a) => {
      a.addEventListener('click', () => {
        menu.classList.remove('is-open');
        toggle.classList.remove('is-open');
        toggle.setAttribute('aria-expanded', 'false');
        document.body.classList.remove('g-nav-locked');
      });
    });
  }

  function injectAurora() {
    if (document.querySelector('.aurora')) return;
    const aurora = document.createElement('div');
    aurora.className = 'aurora';
    aurora.setAttribute('aria-hidden', 'true');
    aurora.innerHTML = [
      '<div class="aurora-orb aurora-orb--1"></div>',
      '<div class="aurora-orb aurora-orb--2"></div>',
      '<div class="aurora-orb aurora-orb--3"></div>',
      '<div class="aurora-orb aurora-orb--4"></div>',
      '<div class="aurora-conic"></div>',
      '<div class="aurora-grid"></div>',
      '<div class="aurora-noise"></div>',
      '<div class="aurora-vignette"></div>'
    ].join('');
    document.body.prepend(aurora);
  }

  async function init() {
    injectAurora();
    if (window.UI && window.UI.initMouseSpotlight) {
      window.UI.initMouseSpotlight();
    }
    const includes = Array.from(document.querySelectorAll('[data-include]'));
    await Promise.all(includes.map(loadPartial));
    highlightActive();
    applyAuthState();
    bindAuthHandlers();
    bindNavToggle();
    // .g-reveal / .g-reveal-group стартуют с opacity:0 и проявляются IO-наблюдателем.
    // Вызываем здесь, чтобы любая страница (auth-shell, простые формы) получала reveal,
    // а не только index/content-detail, у которых initReveal был руками.
    if (window.UI && window.UI.initReveal) window.UI.initReveal();
    document.dispatchEvent(new CustomEvent('partials:ready'));
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
