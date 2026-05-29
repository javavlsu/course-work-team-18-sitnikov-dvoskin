/**
 * page-review-edit.js — редактирование рецензии (двух-колоночный editor).
 *
 * Layout: левый rail c постером/метой контента, справа — slider оценки 1–10
 * c живым прилагательным, заголовок-textarea (auto-grow, Medium-style),
 * тело-textarea. Действия — sticky toolbar сверху.
 *
 * API:
 *  - GET /api/v1/reviews/{id}
 *  - PUT /api/v1/reviews/{id} { title, text, ratingValue }
 *  - DELETE /api/v1/reviews/{id}
 *  - POST /api/v1/reviews/{id}/publish
 */
(function () {
  'use strict';

  const RATING_ADJ = [
    /* 0 */ 'оценка не выбрана',
    /* 1 */ 'Ужасно',
    /* 2 */ 'Плохо',
    /* 3 */ 'Средне',
    /* 4 */ 'Хорошо',
    /* 5 */ 'Отлично'
  ];

  function getId() {
    const m = location.pathname.match(/\/reviews\/(\d+)\/edit/);
    return m ? parseInt(m[1], 10) : null;
  }

  const id = getId();
  let RATING = 0;
  let REVIEW = null;
  let saveStatusTimer = null;

  function $(x) { return document.getElementById(x); }

  function setSaveStatus(text, kind) {
    const el = $('save-status');
    if (!el) return;
    el.textContent = text || '';
    el.className = 'rev-toolbar-status' + (kind ? ' is-' + kind : '');
    if (saveStatusTimer) clearTimeout(saveStatusTimer);
    if (text && kind === 'saved') {
      saveStatusTimer = setTimeout(() => {
        el.textContent = '';
        el.className = 'rev-toolbar-status';
      }, 2200);
    }
  }

  function showError(msg) {
    const el = $('form-error');
    el.classList.remove('is-success');
    el.textContent = msg;
    el.removeAttribute('hidden');
    setTimeout(() => el.setAttribute('hidden', ''), 4000);
  }

  function sparkleSvg() {
    return '<svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">'
      + '<path d="M12 1L13.8 9.2L22 11L13.8 12.8L12 21L10.2 12.8L2 11L10.2 9.2Z"/>'
      + '</svg>';
  }

  function renderStars() {
    const root = $('stars');
    if (root.children.length) return;
    let html = '';
    for (let v = 1; v <= 5; v++) {
      html += `<button type="button" class="rev-star" data-v="${v}" role="radio" aria-checked="false" aria-label="Оценка ${v}">${sparkleSvg()}</button>`;
    }
    root.innerHTML = html;
  }

  function paintRating(previewVal) {
    const v = previewVal != null ? previewVal : RATING;
    document.querySelectorAll('.rev-star').forEach(b => {
      const star = +b.dataset.v;
      b.classList.toggle('is-on', v > 0 && star <= v);
      b.setAttribute('aria-checked', star === RATING ? 'true' : 'false');
    });
    $('rating-num').textContent = v > 0 ? v : '—';
    $('rating-adj').textContent = RATING_ADJ[v] || RATING_ADJ[0];
    $('rating-clear').toggleAttribute('hidden', RATING === 0);
  }

  function bindRating() {
    renderStars();
    const stars = document.querySelectorAll('.rev-star');
    stars.forEach(b => {
      b.addEventListener('click', () => { RATING = +b.dataset.v; paintRating(); });
      b.addEventListener('mouseenter', () => paintRating(+b.dataset.v));
    });
    $('stars').addEventListener('mouseleave', () => paintRating());
    $('stars').addEventListener('keydown', (e) => {
      let next = RATING;
      if (e.key === 'ArrowRight' || e.key === 'ArrowUp')   next = Math.min(5, (RATING || 0) + 1);
      else if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') next = Math.max(0, (RATING || 1) - 1);
      else if (e.key === 'Home') next = 1;
      else if (e.key === 'End')  next = 5;
      else if (/^[1-5]$/.test(e.key)) next = +e.key;
      else return;
      e.preventDefault();
      RATING = next;
      paintRating();
    });
    $('rating-clear').addEventListener('click', () => { RATING = 0; paintRating(); });
  }

  function bindInputs() {
    const t = $('r-title');
    const b = $('r-body');
    t.addEventListener('input', () => { $('title-counter').textContent = t.value.length; });
    b.addEventListener('input', () => { $('body-counter').textContent = b.value.length; });
  }

  function bindMoreMenu() {
    const trigger = $('more-trigger');
    const pop = $('more-pop');
    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      const open = !pop.hasAttribute('hidden');
      pop.toggleAttribute('hidden', open);
      trigger.setAttribute('aria-expanded', open ? 'false' : 'true');
    });
    document.addEventListener('click', () => {
      pop.setAttribute('hidden', '');
      trigger.setAttribute('aria-expanded', 'false');
    });
    pop.addEventListener('click', (e) => e.stopPropagation());
  }

  async function load() {
    if (!id) return;
    try {
      const r = await API.reviewById(id);
      REVIEW = r;
      if (Auth.user && r.author && r.author.id !== Auth.user.id && Auth.user.role !== 'ADMIN') {
        document.querySelector('main').innerHTML =
          `<div class="container my-5">${UI.errorState({ title: 'Нет доступа', text: 'Эту рецензию писали не вы.' })}</div>`;
        return;
      }

      // Left rail
      const c = r.content || null;
      const cHref = c ? UI.urlForContent(c) : '#';
      $('content-link').setAttribute('href', cHref);
      $('rail-title').textContent = c ? c.title : 'Контент';
      const sub = [];
      if (c && c.releaseYear) sub.push(c.releaseYear);
      if (c) sub.push(c.contentType === 'SERIES' ? 'Сериал' : 'Фильм');
      $('rail-sub').textContent = sub.join(' · ');
      const poster = $('rail-poster');
      if (c && c.posterUrl) {
        poster.innerHTML = `<img src="${UI.escapeHtml(c.posterUrl)}" alt="${UI.escapeHtml(c.title || '')}" loading="lazy" onerror="this.parentNode.classList.add('is-empty');this.remove();">`;
      } else {
        poster.classList.add('is-empty');
        poster.innerHTML = '';
      }

      // Status chip in rail
      if (r.status) {
        $('rail-status-value').innerHTML = UI.reviewStatusBadge(r.status);
        $('rail-status').removeAttribute('hidden');
      }

      // Fields
      const t = $('r-title');
      const b = $('r-body');
      t.value = r.title || '';
      b.value = r.text || '';
      $('title-counter').textContent = (r.title || '').length;
      $('body-counter').textContent = (r.text || '').length;
      RATING = r.ratingValue || 0;
      paintRating();

      if (r.status === 'DRAFT') $('publish-btn').removeAttribute('hidden');

      setSaveStatus('');
    } catch (e) {
      document.querySelector('main').innerHTML =
        `<div class="container my-5">${UI.errorState({
          title: e.status === 404 ? 'Рецензия не найдена' : 'Не удалось загрузить',
          text: e.message,
          onRetry: load
        })}</div>`;
    }
  }

  async function save() {
    const title = $('r-title').value.trim();
    const text = $('r-body').value.trim();
    if (!title || !text) { showError('Заполните заголовок и текст'); return; }
    setSaveStatus('Сохранение…', 'saving');
    try {
      await API.put(`/reviews/${id}`, { title, text, ratingValue: RATING || null });
      setSaveStatus('Сохранено', 'saved');
    } catch (e) {
      setSaveStatus('');
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
    setSaveStatus('Отправка…', 'saving');
    try {
      await API.post(`/reviews/${id}/publish`);
      setSaveStatus('Отправлено на модерацию', 'saved');
      setTimeout(() => location.href = `/reviews/${id}`, 900);
    } catch (e) {
      setSaveStatus('');
      showError(e.message || 'Не удалось опубликовать');
    }
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindRating();
    bindInputs();
    bindMoreMenu();
    $('save-btn').addEventListener('click', save);
    $('publish-btn').addEventListener('click', publish);
    $('delete-btn').addEventListener('click', deleteReview);
    document.getElementById('review-form').addEventListener('submit', (e) => {
      e.preventDefault();
      save();
    });
    // Ctrl/Cmd+S — Сохранить
    document.addEventListener('keydown', (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 's') {
        e.preventDefault();
        save();
      }
    });
    load();
  });
})();
