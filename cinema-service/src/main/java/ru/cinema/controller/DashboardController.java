package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cinema.dto.admin.DashboardStatsResponse;
import ru.cinema.service.admin.DashboardStatsService;

/**
 * DashboardController — заявлен отдельным контроллером в Этапе 8.
 * Делегирует в {@link DashboardStatsService}.
 */
@RestController
@RequestMapping("/api/v1/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping
    public DashboardStatsResponse stats() {
        return dashboardStatsService.stats();
    }
}
