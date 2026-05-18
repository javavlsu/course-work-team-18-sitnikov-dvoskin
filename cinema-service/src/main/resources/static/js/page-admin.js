/**
 * page-admin.js — дашборд админа в open-layout (язык .g-stat-row главной).
 * API: GET /api/v1/admin/stats → DashboardStatsResponse
 */
(function () {
  'use strict';

  const ROLE_RU    = { USER: 'Пользователи', ADMIN: 'Администраторы', MODERATOR: 'Модераторы' };
  const TYPE_RU    = { MOVIE: 'Фильмы',     SERIES: 'Сериалы' };
  const C_STATUS   = { DRAFT: 'Черновики',  PUBLISHED: 'Опубликовано', MODERATION: 'На модерации',
                       REJECTED: 'Отклонено', HIDDEN: 'Скрыто',        DELETED: 'Удалено' };
  const R_STATUS   = { DRAFT: 'Черновики',  MODERATION: 'На модерации', PUBLISHED: 'Опубликовано',
                       REJECTED: 'Отклонено', HIDDEN: 'Скрытые',        DELETED: 'Удалённые' };

  function tile(num, label) {
    return `<li><span class="dash-stat-num">${num}</span><span class="dash-stat-label">${label}</span></li>`;
  }

  function fmtNum(v) {
    if (v == null) return '0';
    return Number(v).toLocaleString('ru-RU');
  }

  function breakdown(map, dict) {
    if (!map || Object.keys(map).length === 0) return '';
    return Object.entries(map)
      .filter(([, v]) => v != null && v !== 0)
      .map(([k, v]) => `<span class="dash-chip"><span class="dash-chip-label">${dict[k] || k}</span><span class="dash-chip-num">${fmtNum(v)}</span></span>`)
      .join('');
  }

  async function load() {
    try {
      const s = await API.adminStats();

      // Users
      document.getElementById('users-stats').innerHTML = [
        tile(fmtNum(s.users.total),  'Всего'),
        tile(fmtNum(s.users.active), 'Активные'),
      ].join('');
      document.getElementById('users-breakdown').innerHTML = breakdown(s.users.byRole, ROLE_RU);

      // Content
      const cs = s.content.byStatus || {};
      document.getElementById('content-stats').innerHTML = [
        tile(fmtNum(s.content.total),     'Всего'),
        tile(fmtNum(cs.PUBLISHED || 0),   'Опубликовано'),
        tile(fmtNum(cs.DRAFT || 0),       'Черновики'),
        tile(fmtNum(cs.HIDDEN || 0),      'Скрыто'),
      ].join('');
      document.getElementById('content-breakdown').innerHTML = breakdown(s.content.byType, TYPE_RU);

      // Activity
      document.getElementById('activity-stats').innerHTML = [
        tile(fmtNum(s.reviews.total),   'Рецензий'),
        tile(fmtNum(s.comments.total),  'Комментариев'),
        tile(fmtNum(s.ratings.total),   'Оценок'),
        tile(s.ratings.averageOverall != null ? Number(s.ratings.averageOverall).toFixed(2) : '—', 'Средняя оценка'),
      ].join('');
      document.getElementById('activity-breakdown').innerHTML = breakdown(s.reviews.byStatus, R_STATUS);
    } catch (e) {
      const err = document.getElementById('dash-error');
      err.textContent = e.message || 'Не удалось загрузить статистику';
      err.removeAttribute('hidden');
    }
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    load();
  });
})();
