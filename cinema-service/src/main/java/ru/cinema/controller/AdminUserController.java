package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.admin.AdminUpdateUserRequest;
import ru.cinema.dto.admin.UserAdminItem;
import ru.cinema.dto.common.PageResponse;
import ru.cinema.model.enums.UserRole;
import ru.cinema.service.admin.AdminService;

/**
 * AdminUserController — заявлен отдельным контроллером в Этапе 8.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin · Users")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public PageResponse<UserAdminItem> users(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.of(adminService.listUsers(role, active, pageable).map(UserAdminItem::of));
    }

    @PatchMapping("/{id}")
    public UserAdminItem update(@PathVariable Long id,
                                @Valid @RequestBody AdminUpdateUserRequest req) {
        return UserAdminItem.of(adminService.updateUser(id, req));
    }

    @PatchMapping("/{id}/status")
    public UserAdminItem updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody AdminUpdateUserRequest req) {
        return UserAdminItem.of(adminService.updateUser(id, new AdminUpdateUserRequest(null, req.isActive())));
    }

    @PatchMapping("/{id}/role")
    public UserAdminItem updateRole(@PathVariable Long id,
                                    @Valid @RequestBody AdminUpdateUserRequest req) {
        return UserAdminItem.of(adminService.updateUser(id, new AdminUpdateUserRequest(req.role(), null)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
