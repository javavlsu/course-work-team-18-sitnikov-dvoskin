/**
 * page-review-create.js
 *
 * Refero patterns:
 *  - Medium writer (large title + body textarea, minimal chrome).
 *  - Letterboxd add review (rating stars + body + Save / Publish).
 *
 * API:
 *  - GET /api/v1/search?q=...&size=5 — typeahead для выбора контента
 *  - POST /api/v1/reviews { contentId, title, text, ratingValue } → создаёт DRAFT
 *  - POST /api/v1/reviews/{id}/publish → публикация (ставит в MODERATION)
 *
 * Если ?contentId=... — content-picker скрыт, контент уже выбран.
 *
 * НЕТ spoiler-toggle: в Review entity нет такого поля.
 */
(function () {
  'use strict';

  let CONTENT = null;
  let RATING = 0;
  let debounce = null;

  function setContent(c) {
    CONTENT = c;
    document.getElementById('content-info').innerHTML =
      `Рецензия на «<strong>${UI.escapeHtml(c.title)}</strong>»${c.releaseYear ? ' (' + c.releaseYear + ')' : ''}`;
    document.getElementById('content-picker-card').setAttribute('hidden', '');
  }

  function bindStars() {
    const root = document.getElementById('stars');
    if (root && !root.children.length) root.innerHTML = UI.starRatingTemplate({ max: 10 });
    const stars = document.querySelectorAll('#stars button');
    const val = document.getElementById('stars-val');
    stars.forEach(b => {
      b.addEventListener('click', () => {
        RATING = +b.dataset.v;
        val.textContent = `${RATING} / 10`;
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= RATING));
      });
      b.addEventListener('mouseenter', () => {
        stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= +b.dataset.v));
      });
    });
    document.getElementById('stars').addEventListener('mouseleave', () => {
      stars.forEach(x => x.classList.toggle('is-active', +x.dataset.v <= RATING));
    });
  }

  function bindCounters() {
    const t = document.getElementById('r-title');
    const b = document.getElementById('r-body');
    t.addEventListener('input', () => document.getElementById('title-counter').textContent = t.value.length);
    b.addEventListener('input', () => document.getElementById('body-counter').textContent = b.value.length);
  }

  function bindContentPicker() {
    const input = document.getElementById('content-search');
    const sug = document.getElementById('content-suggestions');
    input.addEventListener('input', () => {
      clearTimeout(debounce);
      const q = input.value.trim();
      if (q.length < 2) { sug.innerHTML = ''; return; }
      debounce = setTimeout(async () => {
        try {
          const page = await API.search({ q, size: 5 });
          const items = (page && page.items) || [];
          if (!items.length) {
            sug.innerHTML = `<p class="text-muted small mb-0">Ничего не нашлось</p>`;
            return;
          }
          sug.innerHTML = items.map(it => `
            <button type="button" class="list-row text-start w-100 border-0 bg-transparent" data-id="${it.id}" data-type="${it.contentType}">
              ${UI.posterImg(it, { sizeClass: 'poster-row', showRating: false, showType: false })}
              <div class="list-row-body">
                <div class="list-row-title" style="font-size:14px">${UI.escapeHtml(it.title)}</div>
                <div class="text-muted small">${it.releaseYear || ''} · ${it.contentType === 'SERIES' ? 'Сериал' : 'Фильм'}</div>
              </div>
            </button>
          `).join('');
          sug.querySelectorAll('button[data-id]').forEach(btn => {
            btn.addEventListener('click', () => {
              const item = items.find(x => x.id === parseInt(btn.dataset.id, 10));
              setContent(item);
            });
          });
        } catch (e) {
          sug.innerHTML = `<p class="text-danger small mb-0">${e.message}</p>`;
        }
      }, 250);
    });
  }

  async function preselectFromUrl() {
    const p = new URLSearchParams(location.search);
    const cid = p.get('contentId');
    if (!cid) return;
    try {
      const c = await API.contentById(cid);
      setContent(c);
    } catch (e) {
      console.warn('[review-create] cant preload content', e);
    }
  }

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
  }

  function getPayload() {
    const title = document.getElementById('r-title').value.trim();
    const text = document.getElementById('r-body').value.trim();
    if (!CONTENT) { showError('Сначала выберите фильм или сериал'); return null; }
    if (!title) { showError('Заполните заголовок'); return null; }
    if (!text) { showError('Заполните текст рецензии'); return null; }
    if (!RATING) { showError('Поставьте оценку'); return null; }
    return { contentId: CONTENT.id, title, text: text, ratingValue: RATING };
  }

  async function saveDraft() {
    document.getElementById('form-error').setAttribute('hidden', '');
    const payload = getPayload();
    if (!payload) return;
    const btn = document.getElementById('save-draft');
    btn.disabled = true; btn.textContent = 'Сохраняем…';
    try {
      const res = await API.post('/reviews', payload);
      showSuccess('Черновик сохранён');
      setTimeout(() => location.href = `/reviews/${res.id}/edit`, 600);
    } catch (e) {
      showError(e.message || 'Не удалось сохранить');
    } finally {
      btn.disabled = false; btn.textContent = 'Сохранить черновик';
    }
  }

  async function publish(ev) {
    if (ev) ev.preventDefault();
    document.getElementById('form-error').setAttribute('hidden', '');
    const payload = getPayload();
    if (!payload) return;
    try {
      const res = await API.post('/reviews', payload);
      await API.post(`/reviews/${res.id}/publish`);
      showSuccess('Рецензия отправлена на модерацию');
      setTimeout(() => location.href = `/reviews/${res.id}`, 800);
    } catch (e) {
      showError(e.message || 'Не удалось опубликовать');
    }
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindStars();
    bindCounters();
    bindContentPicker();
    preselectFromUrl();
    document.getElementById('save-draft').addEventListener('click', saveDraft);
    document.getElementById('review-form').addEventListener('submit', publish);
  });
})();
