package ru.cinema.service.admin;

import org.springframework.stereotype.Service;
import ru.cinema.dto.admin.DashboardStatsResponse;

/**
 * DashboardStatsService — заявлен отдельным сервисом в Этапе 7 для
 * админ-дашборда. Делегирует в {@link AdminService#stats()}, изолируя
 * dashboard-логику от общего admin-API.
 */
@Service
public class DashboardStatsService {

    private final AdminService adminService;

    public DashboardStatsService(AdminService adminService) {
        this.adminService = adminService;
    }

    public DashboardStatsResponse stats() {
        return adminService.stats();
    }
}
