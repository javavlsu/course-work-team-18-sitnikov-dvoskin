/**
 * page-review-edit.js — редактирование рецензии.
 *
 * API:
 *  - GET /api/v1/reviews/{id}
 *  - PUT /api/v1/reviews/{id} { title, text, ratingValue }
 *  - DELETE /api/v1/reviews/{id}
 *  - POST /api/v1/reviews/{id}/publish (для DRAFT)
 */
(function () {
  'use strict';

  function getId() {
    const m = location.pathname.match(/\/reviews\/(\d+)\/edit/);
    return m ? parseInt(m[1], 10) : null;
  }

  const id = getId();
  let RATING = 0;
  let REVIEW = null;

  function showError(msg) {
    const el = document.getElementById('form-error');
    el.classList.remove('is-success');
    el.textContent = msg;
    el.removeAttribute('hidden');
  }
  function showSuccess(msg) {
    const el = document.getElementById('form-success');
    el.textContent = msg;
    el.removeAttribute('hidden');
    setTimeout(() => el.setAttribute('hidden', ''), 2400);
  }

  function paintStars() {
    document.querySelectorAll('#stars button').forEach(b => {
      b.classList.toggle('is-active', +b.dataset.v <= RATING);
    });
    document.getElementById('stars-val').textContent = RATING ? `${RATING} / 10` : '— / 10';
  }

  function bindStars() {
    const root = document.getElementById('stars');
    if (root && !root.children.length) root.innerHTML = UI.starRatingTemplate({ max: 10 });
    const stars = document.querySelectorAll('#stars button');
    stars.forEach(b => {
      b.addEventListener('click', () => { RATING = +b.dataset.v; paintStars(); });
      b.addEventListener('mouseenter', () => {
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= +b.dataset.v));
      });
    });
    document.getElementById('stars').addEventListener('mouseleave', paintStars);
  }

  async function load() {
    if (!id) return;
    try {
      const r = await API.reviewById(id);
      REVIEW = r;
      // только автор может редактировать (UI-уровень, бэк всё равно проверит)
      if (Auth.user && r.author && r.author.id !== Auth.user.id && Auth.user.role !== 'ADMIN') {
        document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({ title: 'Нет доступа', text: 'Эту рецензию писали не вы.' })}</div>`;
        return;
      }

      document.getElementById('content-info').innerHTML = r.content
        ? `Рецензия на «<strong>${UI.escapeHtml(r.content.title)}</strong>» · статус: <strong>${r.status}</strong>`
        : '';
      document.getElementById('r-title').value = r.title || '';
      document.getElementById('r-body').value = r.text || '';
      document.getElementById('title-counter').textContent = (r.title || '').length;
      document.getElementById('body-counter').textContent = (r.text || '').length;
      RATING = r.ratingValue || 0;
      paintStars();

      if (r.status === 'DRAFT') {
        document.getElementById('publish-btn').removeAttribute('hidden');
      }
    } catch (e) {
      document.querySelector('main').innerHTML = `<div class="container my-5">${UI.errorState({
        title: e.status === 404 ? 'Рецензия не найдена' : 'Не удалось загрузить',
        text: e.message,
        onRetry: load
      })}</div>`;
    }
  }

  async function save(ev) {
    if (ev) ev.preventDefault();
    document.getElementById('form-error').setAttribute('hidden', '');
    const title = document.getElementById('r-title').value.trim();
    const text = document.getElementById('r-body').value.trim();
    if (!title || !text) { showError('Заполните все поля'); return; }
    try {
      await API.put(`/reviews/${id}`, { title, text, ratingValue: RATING || null });
      showSuccess('Сохранено');
    } catch (e) {
      showError(e.message || 'Не удалось сохранить');
    }
  }

  async function deleteReview() {
    if (!confirm('Удалить рецензию? Это действие нельзя отменить.')) return;
    try {
      await API.delete(`/reviews/${id}`);
      location.href = '/me/reviews';
    } catch (e) {
      showError(e.message || 'Не удалось удалить');
    }
  }

  async function publish() {
    try {
      await API.post(`/reviews/${id}/publish`);
      showSuccess('Отправлено на модерацию');
      setTimeout(() => location.href = `/reviews/${id}`, 800);
    } catch (e) {
      showError(e.message || 'Не удалось опубликовать');
    }
  }

  function bindCounters() {
    const t = document.getElementById('r-title');
    const b = document.getElementById('r-body');
    t.addEventListener('input', () => document.getElementById('title-counter').textContent = t.value.length);
    b.addEventListener('input', () => document.getElementById('body-counter').textContent = b.value.length);
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindStars();
    bindCounters();
    document.getElementById('review-form').addEventListener('submit', save);
    document.getElementById('delete-btn').addEventListener('click', deleteReview);
    document.getElementById('publish-btn').addEventListener('click', publish);
    load();
  });
})();
