/**
 * page-admin-content.js — управление контентом (status workflow + create + edit + sort).
 *
 * API:
 *  - GET   /api/v1/admin/content?status=&type=&page=&sort=
 *  - PATCH /api/v1/admin/content/{id}/status { status }
 *  - GET   /api/v1/content/{id}    — для preлоада в edit-модалке
 *  - PATCH /api/v1/content/{id}    — admin update
 *  - POST  /api/v1/movies | /series — create
 *  - GET   /api/v1/admin/parse/{search|wikidata|translate}  — автозаполнение
 *  - POST  /api/v1/admin/uploads/poster — загрузка постера
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { status: '', type: '', q: '', page: 0, sort: { field: 'createdAt', dir: 'desc' } };

  // Сейчас редактируется существующий контент? { id, contentType } или null
  let editing = null;

  const STATUS_MAP = {
    DRAFT:      ['badge-draft',      'Черновик'],
    MODERATION: ['badge-moderation', 'На модерации'],
    PUBLISHED:  ['badge-published',  'Опубликован'],
    REJECTED:   ['badge-rejected',   'Отклонён'],
    HIDDEN:     ['badge-hidden',     'Скрыт'],
    DELETED:    ['badge-deleted',    'Удалён']
  };
  const TYPE_RU = { MOVIE: 'Фильм', SERIES: 'Сериал' };

  function statusBadge(s) {
    const [cls, label] = STATUS_MAP[s] || ['badge-draft', s];
    return `<span class="badge ${cls}">${label}</span>`;
  }

  function row(c) {
    const url = c.contentType === 'SERIES' ? `/series/${c.id}` : `/movies/${c.id}`;
    return `
      <tr data-cid="${c.id}" data-ctype="${c.contentType}">
        <td class="mono text-muted">${c.id}</td>
        <td><a class="text-light" href="${url}" target="_blank">${UI.escapeHtml(c.title)}</a></td>
        <td class="text-muted">${TYPE_RU[c.contentType] || c.contentType}</td>
        <td class="mono">${c.releaseYear || ''}</td>
        <td class="mono text-gold">${UI.formatRating(c.averageRating)}</td>
        <td>${statusBadge(c.status)}</td>
        <td class="text-muted">${UI.formatDate(c.createdAt)}</td>
        <td class="text-end">
          <button class="btn btn-xs btn-outline-light" data-act="edit">Изменить</button>
          <button class="btn btn-xs btn-outline-danger ms-1" data-act="delete">Удалить</button>
        </td>
      </tr>`;
  }

  function paintSortIndicators() {
    document.querySelectorAll('th.sortable').forEach(th => {
      const active = th.dataset.sort === state.sort.field;
      th.classList.toggle('is-sorted', active);
      const ind = th.querySelector('.sort-ind');
      if (ind) ind.textContent = active ? (state.sort.dir === 'asc' ? '↑' : '↓') : '↕';
    });
  }

  async function load() {
    const tbody = document.getElementById('content-tbody');
    tbody.innerHTML = `<tr><td colspan="8" class="text-muted">Загрузка…</td></tr>`;
    document.getElementById('pagination-mount').innerHTML = '';
    paintSortIndicators();
    try {
      const params = {
        page: state.page,
        size: PAGE_SIZE,
        sort: state.sort.field + ',' + state.sort.dir
      };
      if (state.status) params.status = state.status;
      if (state.type)   params.type   = state.type;
      if (state.q)      params.q      = state.q;
      const page = await API.adminContent(params);
      const items = (page && page.items) || [];
      document.getElementById('content-count').textContent =
        `${page.totalElements} ${UI.pluralize(page.totalElements, ['запись','записи','записей'])}`;
      if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="8">${UI.emptyState({ title: 'Нет контента', text: 'Сменить фильтры?' })}</td></tr>`;
      } else {
        tbody.innerHTML = items.map(row).join('');
        bindRowActions();
        document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => { state.page = p; load(); });
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="8">${UI.errorState({ onRetry: load })}</td></tr>`;
    }
  }

  function bindRowActions() {
    document.querySelectorAll('tr[data-cid]').forEach(tr => {
      const cid = tr.dataset.cid;
      const title = tr.querySelector('td:nth-child(2)').innerText.trim();
      tr.querySelector('button[data-act="edit"]').addEventListener('click', () => openEdit(cid));
      tr.querySelector('button[data-act="delete"]').addEventListener('click', () => deleteContent(cid, title));
    });
  }

  async function deleteContent(id, title) {
    const ok = await UI.confirmDialog({
      title: 'Удалить контент',
      text: `Удалить «${title}»? Запись будет помечена как удалённая и исчезнет из каталога.`,
      confirmText: 'Удалить',
      danger: true
    });
    if (!ok) return;
    try {
      await API.delete('/content/' + id);
      load();
    } catch (e) {
      alert('Не удалось удалить: ' + (e.message || ''));
    }
  }

  function bindSorting() {
    document.querySelectorAll('th.sortable').forEach(th => {
      th.addEventListener('click', () => {
        const f = th.dataset.sort;
        if (state.sort.field === f) {
          state.sort.dir = state.sort.dir === 'asc' ? 'desc' : 'asc';
        } else {
          state.sort.field = f;
          state.sort.dir = (f === 'createdAt' || f === 'averageRating') ? 'desc' : 'asc';
        }
        state.page = 0;
        load();
      });
    });
  }

  function bindSearch() {
    const input = document.getElementById('search-q');
    if (!input) return;
    let timer = null;
    input.addEventListener('input', () => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        state.q = input.value.trim();
        state.page = 0;
        load();
      }, 250);
    });
  }

  function bindFilters() {
    document.getElementById('status-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-status]'); if (!b) return;
      state.status = b.dataset.status; state.page = 0;
      document.querySelectorAll('#status-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.status === state.status));
      load();
    });
    document.getElementById('type-group').addEventListener('click', (e) => {
      const b = e.target.closest('button[data-type]'); if (!b) return;
      state.type = b.dataset.type; state.page = 0;
      document.querySelectorAll('#type-group .filter-chip').forEach(x => x.classList.toggle('is-active', x.dataset.type === state.type));
      load();
    });
  }

  // ===== Modal: create + edit =====
  let modalType = 'MOVIE';

  function setPosterPreview(url) {
    const box = document.getElementById('nc-poster-preview');
    if (!url) {
      box.innerHTML = '<div class="nc-poster-empty">нет</div>';
      return;
    }
    box.innerHTML = `<img src="${url}" alt="постер">`;
  }

  function applyTypeUi() {
    document.querySelectorAll('#nc-type-group .filter-chip').forEach(c => c.classList.toggle('is-active', c.dataset.type === modalType));
    document.querySelector('.nc-movie-only').toggleAttribute('hidden', modalType !== 'MOVIE');
    document.querySelector('.nc-series-only').toggleAttribute('hidden', modalType !== 'SERIES');
    // В edit-режиме нельзя менять тип — он фиксирован у существующего контента
    document.querySelectorAll('#nc-type-group .filter-chip').forEach(c => c.toggleAttribute('disabled', !!editing));
  }

  function openCreate() {
    const modal = document.getElementById('newContentModal');
    const err   = document.getElementById('new-content-error');
    const form  = document.getElementById('new-content-form');
    editing = null;
    err.setAttribute('hidden', '');
    form.reset();
    modalType = 'MOVIE';
    applyTypeUi();
    setPosterPreview(null);
    document.getElementById('nc-modal-title').textContent = 'Новый контент';
    document.getElementById('nc-submit-btn').textContent = 'Создать';
    document.getElementById('nc-status-wrap').setAttribute('hidden', '');
    document.getElementById('nc-imdb-search').value = '';
    document.getElementById('nc-imdb-results').setAttribute('hidden', '');
    document.getElementById('nc-imdb-results').innerHTML = '';
    new bootstrap.Modal(modal).show();
  }

  async function openEdit(id) {
    const modal = document.getElementById('newContentModal');
    const err   = document.getElementById('new-content-error');
    const form  = document.getElementById('new-content-form');
    err.setAttribute('hidden', '');
    form.reset();
    document.getElementById('nc-modal-title').textContent = 'Редактирование…';
    document.getElementById('nc-imdb-search').value = '';
    document.getElementById('nc-imdb-results').setAttribute('hidden', '');
    document.getElementById('nc-imdb-results').innerHTML = '';
    new bootstrap.Modal(modal).show();

    try {
      const c = await API.get('/content/' + id);
      editing = { id: c.id, contentType: c.contentType };
      modalType = c.contentType || 'MOVIE';
      applyTypeUi();
      document.getElementById('nc-modal-title').textContent = `Редактирование: ${c.title || ''}`;
      document.getElementById('nc-submit-btn').textContent = 'Сохранить';
      document.getElementById('nc-status-wrap').removeAttribute('hidden');

      const set = (id, v) => { const el = document.getElementById(id); if (el && v != null) el.value = v; };
      set('nc-title',     c.title);
      set('nc-original',  c.originalTitle);
      set('nc-year',      c.releaseYear);
      set('nc-country',   c.country);
      set('nc-language',  c.language);
      set('nc-imdb',      c.imdbId);
      set('nc-kp',        c.kinopoiskId);
      set('nc-poster',    c.posterUrl);
      set('nc-description', c.description);
      set('nc-status', c.status);
      setPosterPreview(c.posterUrl);
      if (c.contentType === 'MOVIE') {
        set('nc-duration',  c.duration);
        set('nc-budget',    c.budget);
        set('nc-boxoffice', c.boxOffice);
      } else {
        set('nc-seasons',   c.totalSeasons);
        set('nc-episodes',  c.totalEpisodes);
        if (document.getElementById('nc-finished')) document.getElementById('nc-finished').checked = !!c.isFinished;
      }
    } catch (e) {
      err.textContent = 'Не удалось загрузить: ' + (e.message || '');
      err.removeAttribute('hidden');
      editing = null;
    }
  }

  function bindModal() {
    const modal = document.getElementById('newContentModal');
    const form  = document.getElementById('new-content-form');
    const err   = document.getElementById('new-content-error');

    document.getElementById('add-content-btn').addEventListener('click', openCreate);

    document.getElementById('nc-type-group').addEventListener('click', (e) => {
      if (editing) return; // нельзя менять тип у существующего
      const b = e.target.closest('button[data-type]'); if (!b) return;
      modalType = b.dataset.type;
      applyTypeUi();
    });

    document.getElementById('nc-poster-file').addEventListener('change', async (e) => {
      const f = e.target.files && e.target.files[0];
      if (!f) return;
      const fd = new FormData();
      fd.append('file', f);
      try {
        const res = await fetch('/api/v1/admin/uploads/poster', {
          method: 'POST', headers: Auth.headers(), body: fd
        });
        if (!res.ok) throw new Error('Не удалось загрузить файл');
        const data = await res.json();
        document.getElementById('nc-poster').value = data.url;
        setPosterPreview(data.url);
      } catch (ex) {
        err.textContent = ex.message || 'Ошибка загрузки';
        err.removeAttribute('hidden');
      }
    });

    document.getElementById('nc-poster').addEventListener('input', (e) => {
      setPosterPreview(e.target.value.trim() || null);
    });

    // ----- Поиск через IMDB autocomplete -----
    async function imdbSearch() {
      const q = document.getElementById('nc-imdb-search').value.trim();
      const out = document.getElementById('nc-imdb-results');
      if (!q) return;
      out.innerHTML = '<div class="text-muted small">Ищу…</div>';
      out.removeAttribute('hidden');
      try {
        const data = await API.get('/admin/parse/search?q=' + encodeURIComponent(q));
        const items = data.results || [];
        if (!items.length) {
          out.innerHTML = '<div class="text-muted small">Ничего не найдено</div>';
          return;
        }
        out.innerHTML = items.slice(0, 8).map((r, i) => `
          <button type="button" class="nc-imdb-result" data-idx="${i}">
            <div class="nc-imdb-result-poster">${r.posterUrl ? `<img src="${r.posterUrl}" alt="">` : ''}</div>
            <div class="nc-imdb-result-body">
              <div class="nc-imdb-result-title">${UI.escapeHtml(r.title || '')}</div>
              <div class="nc-imdb-result-sub">${r.releaseYear || '—'} · ${r.type === 'SERIES' ? 'Сериал' : 'Фильм'} · ${r.imdbId || ''}</div>
              <div class="nc-imdb-result-stars">${UI.escapeHtml(r.stars || '')}</div>
            </div>
          </button>
        `).join('');
        out.querySelectorAll('.nc-imdb-result').forEach((b, i) => {
          b.addEventListener('click', () => fillFromImdb(items[i]));
        });
      } catch (ex) {
        out.innerHTML = `<div class="text-muted small">${UI.escapeHtml(ex.message || 'Ошибка поиска')}</div>`;
      }
    }
    document.getElementById('nc-imdb-go').addEventListener('click', imdbSearch);
    document.getElementById('nc-imdb-search').addEventListener('keydown', (e) => {
      if (e.key === 'Enter') { e.preventDefault(); imdbSearch(); }
    });

    async function translateField(srcId) {
      const el = document.getElementById(srcId);
      if (!el || !el.value.trim()) return;
      const ph = el.placeholder;
      el.placeholder = 'Перевожу…';
      const ru = await translateToRu(el.value.trim());
      el.placeholder = ph;
      if (ru) el.value = ru;
    }
    document.getElementById('nc-title-translate').addEventListener('click', () => translateField('nc-title'));
    document.getElementById('nc-desc-translate').addEventListener('click', () => translateField('nc-description'));

    function looksEnglish(text) {
      if (!text) return false;
      const lat = (text.match(/[A-Za-z]/g) || []).length;
      const cyr = (text.match(/[Ѐ-ӿ]/g) || []).length;
      return lat > cyr;
    }

    async function translateToRu(text) {
      if (!text) return null;
      try {
        const res = await API.get('/admin/parse/translate?from=en&to=ru&q=' + encodeURIComponent(text));
        return res && res.text ? res.text : null;
      } catch (e) { return null; }
    }

    async function fetchWikipediaDescription(title, year, isSeries) {
      async function enSummary(t) {
        try {
          const enc = encodeURIComponent(t.replace(/\s+/g, '_'));
          const res = await fetch(`https://en.wikipedia.org/api/rest_v1/page/summary/${enc}`);
          if (!res.ok) return null;
          const d = await res.json();
          if (d.type === 'disambiguation') return null;
          return d;
        } catch (e) { return null; }
      }
      const suffixes = isSeries
        ? [`_(${year}_TV_series)`, '_(TV_series)', '']
        : [`_(${year}_film)`, '_(film)', ''];
      let summary = null;
      for (const s of suffixes) {
        summary = await enSummary(title + s);
        if (summary) break;
      }
      if (!summary) return null;
      try {
        const titleKey = summary.titles?.canonical || summary.title;
        const ll = await fetch(
          `https://en.wikipedia.org/w/api.php?action=query&prop=langlinks&lllang=ru&titles=${encodeURIComponent(titleKey)}&format=json&origin=*`
        ).then(r => r.json());
        const pages = ll?.query?.pages || {};
        const first = Object.values(pages)[0];
        const ruTitle = first?.langlinks?.[0]?.['*'];
        if (ruTitle) {
          const ru = await fetch(`https://ru.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(ruTitle.replace(/\s+/g, '_'))}`);
          if (ru.ok) {
            const d = await ru.json();
            if (d.extract) return d.extract;
          }
        }
      } catch (e) {}
      return summary.extract || null;
    }

    async function fillFromImdb(r) {
      if (!editing && r.type) { modalType = r.type; applyTypeUi(); }
      const setIf = (id, val, force) => {
        const el = document.getElementById(id);
        if (!el || val == null) return;
        if (force || !el.value) el.value = val;
      };
      setIf('nc-title',    r.title,         /*force=*/false);
      setIf('nc-original', r.originalTitle, false);
      setIf('nc-year',     r.releaseYear,   false);
      setIf('nc-imdb',     r.imdbId,        false);
      if (r.posterUrl) {
        const p = document.getElementById('nc-poster');
        if (!p.value) { p.value = r.posterUrl; setPosterPreview(r.posterUrl); }
      }
      document.getElementById('nc-imdb-results').setAttribute('hidden', '');

      const descBox = document.getElementById('nc-description');
      const tasks = [];
      if (r.title) {
        if (descBox && !descBox.value) descBox.placeholder = 'Загружаю описание…';
        tasks.push(fetchWikipediaDescription(r.title, r.releaseYear, (editing ? editing.contentType : modalType) === 'SERIES')
          .then(async text => {
            if (!text || !descBox || descBox.value) return;
            if (looksEnglish(text)) {
              descBox.placeholder = 'Перевожу на русский…';
              const ru = await translateToRu(text);
              text = ru || text;
            }
            descBox.value = text;
          })
          .finally(() => { if (descBox) descBox.placeholder = ''; }));
      }
      if (r.imdbId) {
        tasks.push(API.get('/admin/parse/wikidata?imdbId=' + encodeURIComponent(r.imdbId))
          .then(d => {
            if (!d) return;
            setIf('nc-country',  d.country);
            setIf('nc-language', d.language);
            const t = (editing ? editing.contentType : modalType);
            if (t === 'SERIES') {
              setIf('nc-seasons',  d.totalSeasons);
              setIf('nc-episodes', d.totalEpisodes);
            } else {
              setIf('nc-duration',  d.duration);
              setIf('nc-budget',    d.budget);
              setIf('nc-boxoffice', d.boxOffice);
            }
          })
          .catch(() => {}));
      }
      await Promise.allSettled(tasks);
    }

    const numOrNull = (id) => {
      const v = document.getElementById(id).value.trim();
      return v === '' ? null : Number(v);
    };
    const strOrNull = (id) => {
      const v = document.getElementById(id).value.trim();
      return v === '' ? null : v;
    };

    form.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      err.setAttribute('hidden', '');
      const title = document.getElementById('nc-title').value.trim();
      if (!title) { err.textContent = 'Название обязательно'; err.removeAttribute('hidden'); return; }
      const t = editing ? editing.contentType : modalType;
      const isMovie = t === 'MOVIE';
      const body = {
        title,
        originalTitle: strOrNull('nc-original'),
        description:   strOrNull('nc-description'),
        releaseYear:   numOrNull('nc-year'),
        posterUrl:     strOrNull('nc-poster'),
        country:       strOrNull('nc-country'),
        language:      strOrNull('nc-language'),
        imdbId:        strOrNull('nc-imdb'),
        kinopoiskId:   strOrNull('nc-kp'),
        tagIds:        null,
        duration:      isMovie ? numOrNull('nc-duration') : null,
        budget:        isMovie ? numOrNull('nc-budget') : null,
        boxOffice:     isMovie ? numOrNull('nc-boxoffice') : null,
        totalSeasons:  isMovie ? null : numOrNull('nc-seasons'),
        totalEpisodes: isMovie ? null : numOrNull('nc-episodes'),
        isFinished:    isMovie ? null : document.getElementById('nc-finished').checked
      };
      try {
        if (editing) {
          body.status = strOrNull('nc-status');
          await API.patch('/content/' + editing.id, body);
        } else {
          body.type = modalType;
          const path = isMovie ? '/movies' : '/series';
          await API.post(path, body);
        }
        bootstrap.Modal.getInstance(modal).hide();
        state.page = 0;
        load();
      } catch (e) {
        err.textContent = e.message || 'Не удалось сохранить';
        err.removeAttribute('hidden');
      }
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    bindSearch();
    bindFilters();
    bindSorting();
    bindModal();
    load();
  });
})();
