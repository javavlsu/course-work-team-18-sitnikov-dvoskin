/**
 * page-catalog.js — единый код для /catalog (объединённого каталога+поиска).
 *
 * Pattern: Netflix /search + browse merged surface — один экран с filter+search,
 * вместо отдельных /movies, /series, /search. Все три URL редиректят сюда.
 *
 * URL state: /catalog?q=...&type=MOVIE|SERIES&tag=N&sort=new
 * Backend: GET /api/v1/content?q=&type=&tag=&sort=&page=&size= (он принимает все параметры).
 */
(function () {
  'use strict';

  const PAGE_SIZE = 24;
  const RECENT_KEY = 'moviehub:recent-searches';
  const RECENT_MAX = 6;

  const state = {
    q: '',
    type: '',
    tag: null,
    genre: null,
    sort: 'new',
    page: 0,
    loading: false,
    hasMore: true,
    chunk: 0,
  };

  let searchDebTimer = null;
  let recordTimer = null;

  // ===== URL =====
  function readUrl() {
    const p = new URLSearchParams(location.search);
    state.q = p.get('q') || '';
    state.type = p.get('type') || '';
    state.tag = p.get('tag') ? (parseInt(p.get('tag'), 10) || null) : null;
    state.genre = p.get('genre') ? (parseInt(p.get('genre'), 10) || null) : null;
    state.sort = p.get('sort') || 'new';
  }
  function writeUrl() {
    const p = new URLSearchParams();
    if (state.q) p.set('q', state.q);
    if (state.type) p.set('type', state.type);
    if (state.tag) p.set('tag', state.tag);
    if (state.genre) p.set('genre', state.genre);
    if (state.sort && state.sort !== 'new') p.set('sort', state.sort);
    const qs = p.toString();
    history.replaceState(null, '', qs ? `?${qs}` : location.pathname);
  }

  // ===== Recent searches =====
  function pushRecent(q) {
    if (!q || q.length < 2) return;
    try {
      const list = JSON.parse(localStorage.getItem(RECENT_KEY) || '[]')
        .filter(x => x && x.toLowerCase() !== q.toLowerCase());
      list.unshift(q);
      localStorage.setItem(RECENT_KEY, JSON.stringify(list.slice(0, RECENT_MAX)));
    } catch {}
  }

  // ===== UI sync =====
  function applyTypeChips() {
    document.querySelectorAll('#type-group .filter-chip').forEach(b =>
      b.classList.toggle('is-active', b.dataset.type === state.type));
  }
  function applyTagChips() {
    document.querySelectorAll('#tag-group .filter-chip').forEach(b => {
      const v = b.dataset.tag === '' ? null : parseInt(b.dataset.tag, 10);
      b.classList.toggle('is-active', v === state.tag);
    });
  }
  function applyGenreChips() {
    document.querySelectorAll('#genre-group .filter-chip').forEach(b => {
      const v = b.dataset.genre === '' ? null : parseInt(b.dataset.genre, 10);
      b.classList.toggle('is-active', v === state.genre);
    });
  }
  function applySortActive() {
    const opts = document.querySelectorAll('[data-sort-menu] [data-sort]');
    let activeLabel = 'Новые';
    opts.forEach(b => {
      const on = b.dataset.sort === state.sort;
      b.classList.toggle('is-active', on);
      b.setAttribute('aria-checked', String(on));
      if (on) activeLabel = b.textContent.trim();
    });
    const valueEl = document.querySelector('[data-sort-value]');
    if (valueEl) valueEl.textContent = activeLabel;
  }
  function toggleClearButton() {
    const input = document.getElementById('q-input');
    const clear = document.getElementById('q-clear');
    if (clear) clear.hidden = !input.value;
  }

  // ===== Sort dropdown =====
  function bindSort() {
    const root = document.querySelector('[data-sort-root]');
    if (!root) return;
    const trigger = root.querySelector('[data-sort-trigger]');
    const menu = root.querySelector('[data-sort-menu]');

    function setOpen(open) {
      if (open) {
        menu.hidden = false;
        requestAnimationFrame(() => menu.classList.add('is-open'));
        trigger.setAttribute('aria-expanded', 'true');
        document.body.classList.add('has-sort-sheet');
      } else {
        menu.classList.remove('is-open');
        trigger.setAttribute('aria-expanded', 'false');
        document.body.classList.remove('has-sort-sheet');
        setTimeout(() => { if (!menu.classList.contains('is-open')) menu.hidden = true; }, 220);
      }
    }
    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      setOpen(menu.hidden);
    });
    menu.addEventListener('click', (e) => {
      const opt = e.target.closest('[data-sort]');
      if (!opt) return;
      if (opt.dataset.sort === state.sort) { setOpen(false); return; }
      state.sort = opt.dataset.sort;
      applySortActive();
      writeUrl();
      setOpen(false);
      reset();
    });
    document.addEventListener('click', (e) => {
      if (!root.contains(e.target)) setOpen(false);
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && !menu.hidden) { setOpen(false); trigger.focus(); }
    });
  }

  // ===== Search input =====
  function bindSearch() {
    const input = document.getElementById('q-input');
    const clear = document.getElementById('q-clear');
    input.value = state.q;
    toggleClearButton();

    input.addEventListener('input', () => {
      toggleClearButton();
      clearTimeout(searchDebTimer);
      searchDebTimer = setTimeout(() => {
        const q = input.value.trim();
        if (q === state.q) return;
        state.q = q;
        writeUrl();
        reset();
      }, 300);
    });
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && input.value) clearAll();
    });
    clear.addEventListener('click', () => {
      clearAll();
      input.focus();
    });

    function clearAll() {
      input.value = '';
      toggleClearButton();
      if (state.q) { state.q = ''; writeUrl(); reset(); }
    }
  }

  function bindTypes() {
    document.getElementById('type-group').addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-type]');
      if (!btn) return;
      state.type = btn.dataset.type;
      writeUrl();
      applyTypeChips();
      reset();
    });
  }

  // ===== Genres (широкие категории: Драма, Боевик, ...) =====
  async function loadGenres() {
    try {
      const genres = await API.listGenres();
      const group = document.getElementById('genre-group');
      group.innerHTML = `<button class="filter-chip" data-genre="">Все жанры</button>` +
        genres.slice(0, 24).map(g => `<button class="filter-chip" data-genre="${g.id}">${UI.escapeHtml(g.name)}</button>`).join('');
      group.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-genre]');
        if (!btn) return;
        state.genre = btn.dataset.genre === '' ? null : parseInt(btn.dataset.genre, 10);
        writeUrl();
        applyGenreChips();
        reset();
      });
      applyGenreChips();
    } catch (e) {
      console.warn('[catalog] genres', e);
    }
  }

  // ===== Tags (тонкие пометки: ностальгия, новогоднее, ...) =====
  async function loadTags() {
    try {
      const tags = await API.listTags();
      const group = document.getElementById('tag-group');
      if (!tags.length) {
        group.style.display = 'none';
        return;
      }
      group.innerHTML = `<button class="filter-chip" data-tag="">Все теги</button>` +
        tags.slice(0, 24).map(t => `<button class="filter-chip" data-tag="${t.id}">${UI.escapeHtml(t.name)}</button>`).join('');
      group.addEventListener('click', (e) => {
        const btn = e.target.closest('button[data-tag]');
        if (!btn) return;
        state.tag = btn.dataset.tag === '' ? null : parseInt(btn.dataset.tag, 10);
        writeUrl();
        applyTagChips();
        reset();
      });
      applyTagChips();
    } catch (e) {
      console.warn('[catalog] tags', e);
    }
  }

  // ===== Render =====
  function renderCards(items) {
    return items.map((it, i) =>
      `<div class="col-6 col-md-4 col-lg-3 col-xl-2 catalog-card-in" style="--i:${Math.min(i, 11)}">${UI.contentCard(it)}</div>`
    ).join('');
  }
  function setSentinelState(loading, done) {
    const sentinel = document.getElementById('grid-sentinel');
    const end = document.getElementById('grid-end');
    if (sentinel) {
      sentinel.classList.toggle('is-loading', !!loading);
      sentinel.hidden = !!done;
    }
    if (end) end.hidden = !done;
  }

  function renderEmptyState(opts) {
    clearEmptyState();
    const grid = document.getElementById('grid');
    const wrap = document.createElement('div');
    wrap.id = 'catalog-empty';
    wrap.innerHTML = opts.error ? UI.errorState({ onRetry: () => fetchPage(false) }) : UI.emptyState(opts);
    grid.parentNode.insertBefore(wrap, grid.nextSibling);
  }
  function clearEmptyState() {
    const old = document.getElementById('catalog-empty');
    if (old) old.remove();
  }

  async function fetchPage(append) {
    if (state.loading) return;
    if (append && !state.hasMore) return;
    state.loading = true;

    const grid = document.getElementById('grid');
    if (!append) {
      state.page = 0;
      state.hasMore = true;
      state.chunk = 0;
      clearEmptyState();
      grid.innerHTML = UI.skeletonGrid(12);
      document.getElementById('grid-end').hidden = true;
      document.getElementById('grid-sentinel').hidden = false;
    } else {
      setSentinelState(true, false);
    }

    try {
      const params = { sort: state.sort, page: state.page, size: PAGE_SIZE };
      if (state.q)     params.q = state.q;
      if (state.type)  params.type = state.type;
      if (state.tag)   params.tag = state.tag;
      if (state.genre) params.genre = state.genre;

      // /search умеет оба фильтра (tag + genre); /content только tag.
      const usesSearch = !!state.genre || !!state.q;
      const page = usesSearch ? await API.search(params) : await API.listContent(params);
      const items = (page && page.items) || [];
      const visible = items.filter(it => it && it.posterUrl);

      const countEl = document.getElementById('result-count');
      const total = page.totalElements;
      const noun = UI.pluralize(total, ['результат', 'результата', 'результатов']);
      countEl.textContent = state.q ? `${total} ${noun} по запросу «${state.q}»` : `${total} ${noun}`;

      state.hasMore = !page.last && (state.page + 1) < page.totalPages;

      if (!append) {
        if (!visible.length) {
          // Empty-state рендерим РЯДОМ с гридом, не внутри Bootstrap row — иначе col-12
          // обёртка ломает горизонтальное центрирование контента.
          grid.innerHTML = '';
          renderEmptyState({
            title: state.q ? 'Ничего не нашлось' : 'Каталог пуст по этим фильтрам',
            text: state.q ? 'Попробуйте сменить запрос или сбросить фильтры.' : 'Попробуйте сменить тип, жанр или сортировку.',
            cta: 'Сбросить фильтры',
            ctaHref: location.pathname
          });
          state.hasMore = false;
        } else {
          grid.innerHTML = renderCards(visible);
        }
      } else if (visible.length) {
        grid.insertAdjacentHTML('beforeend', renderCards(visible));
      }

      state.page += 1;
      state.chunk += 1;
      setSentinelState(false, !state.hasMore);

      // Записать query в recent searches с задержкой (после успешного поиска)
      if (state.q) {
        clearTimeout(recordTimer);
        recordTimer = setTimeout(() => pushRecent(state.q), 1000);
      }
    } catch (e) {
      console.error('[catalog]', e);
      if (!append) {
        grid.innerHTML = '';
        renderEmptyState({ error: true });
      }
      setSentinelState(false, false);
    } finally {
      state.loading = false;
    }
  }

  function reset() {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    fetchPage(false);
  }

  function setupSentinel() {
    const sentinel = document.getElementById('grid-sentinel');
    if (!sentinel || !('IntersectionObserver' in window)) return;
    const io = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && state.hasMore && !state.loading) {
          fetchPage(true);
        }
      });
    }, { rootMargin: '0px 0px 600px 0px' });
    io.observe(sentinel);
  }

  document.addEventListener('partials:ready', () => {
    readUrl();
    bindSearch();
    bindTypes();
    bindSort();
    applyTypeChips();
    applySortActive();
    loadGenres();
    loadTags();
    setupSentinel();
    fetchPage(false);
  });
})();
