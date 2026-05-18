/**
 * page-admin.js — дашборд админа.
 *
 * Refero patterns:
 *  - Linear admin: stat cards row (number prominent, label uppercase).
 *  - Stripe: breakdown по статусам внутри карточки.
 *
 * API: GET /api/v1/admin/stats → DashboardStatsResponse
 */
(function () {
  'use strict';

  function statCard(num, label, breakdown) {
    return `
      <div class="col-6 col-md-3">
        <div class="dashboard-stat">
          <div class="dashboard-stat-number">${num}</div>
          <div class="dashboard-stat-label">${label}</div>
          ${breakdown ? `<div class="text-muted small mt-2">${breakdown}</div>` : ''}
        </div>
      </div>`;
  }

  function fmtBreakdown(map) {
    if (!map) return '';
    return Object.entries(map).map(([k, v]) => `${k}: <span class="text-light mono">${v}</span>`).join(' · ');
  }

  async function load() {
    try {
      const s = await API.adminStats();

      // Users
      document.getElementById('users-stats').innerHTML = [
        statCard((s.users.total || 0).toLocaleString('ru-RU'), 'Всего', fmtBreakdown(s.users.byRole)),
        statCard((s.users.active || 0).toLocaleString('ru-RU'), 'Активные'),
      ].join('');

      // Content
      document.getElementById('content-stats').innerHTML = [
        statCard((s.content.total || 0).toLocaleString('ru-RU'), 'Всего', fmtBreakdown(s.content.byType)),
        statCard((s.content.byStatus && s.content.byStatus.PUBLISHED) || 0, 'Опубликовано'),
        statCard((s.content.byStatus && s.content.byStatus.DRAFT) || 0, 'Черновики'),
        statCard((s.content.byStatus && s.content.byStatus.HIDDEN) || 0, 'Скрытые'),
      ].join('');

      // Activity
      document.getElementById('activity-stats').innerHTML = [
        statCard((s.reviews.total || 0).toLocaleString('ru-RU'), 'Рецензий', fmtBreakdown(s.reviews.byStatus)),
        statCard((s.comments.total || 0).toLocaleString('ru-RU'), 'Комментариев'),
        statCard((s.ratings.total || 0).toLocaleString('ru-RU'), 'Оценок'),
        statCard(s.ratings.averageOverall != null ? Number(s.ratings.averageOverall).toFixed(2) : '—', 'Средняя оценка'),
      ].join('');
    } catch (e) {
      document.getElementById('dash-error').textContent = e.message || 'Не удалось загрузить статистику';
      document.getElementById('dash-error').removeAttribute('hidden');
    }
  }

  document.addEventListener('partials:ready', () => {
    if (!Auth.requireAdmin()) return;
    load();
  });
})();
