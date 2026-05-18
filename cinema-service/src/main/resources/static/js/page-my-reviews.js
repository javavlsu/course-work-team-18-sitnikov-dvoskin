/**
 * page-my-reviews.js — кабинет рецензий пользователя.
 *
 * Refero patterns:
 *  - Letterboxd "Your reviews": список с posters slева, title/meta/status справа.
 *  - Goodreads my reviews: status-tabs.
 *
 * API: GET /api/v1/reviews?userId={meId}&status={status}
 *      DELETE /api/v1/reviews/{id}
 *      POST /api/v1/reviews/{id}/publish
 */
(function () {
  'use strict';

  const PAGE_SIZE = 20;
  const state = { status: '', page: 0 };

  const row = (r) => UI.reviewRow(r, { actions: true });

  function setActiveTab() {
    document.querySelectorAll('.tab-pill').forEach(t => {
      t.classList.toggle('is-active', t.dataset.status === state.status);
    });
  }

  async function load() {
    const mount = document.getElementById('reviews-mount');
    mount.innerHTML = UI.skeletonList(4);
    const me = Auth.user;
    if (!me) return;
    try {
      const params = { userId: me.id, page: state.page, size: PAGE_SIZE };
      if (state.status) params.status = state.status;
      const page = await API.listReviews(params);
      const items = (page && page.items) || [];
      if (!items.length) {
        mount.innerHTML = UI.emptyState({
          title: state.status ? 'Нет рецензий в этом статусе' : 'Пока нет рецензий',
          text: 'Напишите свою первую рецензию.',
          cta: 'Создать',
          ctaHref: '/reviews/new'
        });
        document.getElementById('pagination-mount').innerHTML = '';
      } else {
        mount.innerHTML = items.map(row).join('');
        document.getElementById('pagination-mount').innerHTML = UI.pagination(page, (p) => {
          state.page = p; window.scrollTo({ top: 0, behavior: 'smooth' }); load();
        });
        bindActions();
      }
      // обновим табсчётчики только при выборе "all"
      if (!state.status) {
        document.getElementById('cnt-all').textContent = page.totalElements;
        // подгрузим остальные счётчики тихо
        ['DRAFT','MODERATION','PUBLISHED','REJECTED'].forEach(async (s) => {
          try {
            const p = await API.listReviews({ userId: me.id, status: s, size: 1 });
            const id = { DRAFT:'cnt-draft', MODERATION:'cnt-mod', PUBLISHED:'cnt-pub', REJECTED:'cnt-rej' }[s];
            document.getElementById(id).textContent = p.totalElements;
          } catch(e) {}
        });
      }
    } catch (e) {
      mount.innerHTML = UI.errorState({ onRetry: load });
    }
  }

  function bindActions() {
    document.querySelectorAll('button[data-action]').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        const action = btn.dataset.action;
        if (action === 'delete') {
          if (!confirm('Удалить рецензию? Это действие нельзя отменить.')) return;
          try {
            await API.delete(`/reviews/${id}`);
            load();
          } catch (e) { alert(e.message || 'Не удалось удалить'); }
        } else if (action === 'publish') {
          try {
            await API.post(`/reviews/${id}/publish`);
            load();
          } catch (e) { alert(e.message || 'Не удалось опубликовать'); }
        }
      });
    });
  }

  function bindTabs() {
    document.querySelectorAll('.tab-pill').forEach(t => {
      t.addEventListener('click', () => {
        state.status = t.dataset.status;
        state.page = 0;
        setActiveTab();
        load();
      });
    });
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAuth()) return;
    bindTabs();
    setActiveTab();
    load();
  });
})();
