package com.expense.tracker.controller;

import com.expense.tracker.dto.UserAdminDTO;
import com.expense.tracker.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin — User Management",
        description = "Endpoints for managing users and roles. Restricted to ROLE_ADMIN.")
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserAdminService userAdminService;

    @Autowired
    public AdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }


    /**
     * GET /api/admin/users
     * GET /api/admin/users?enabled=true     ← active users only
     * GET /api/admin/users?enabled=false    ← deactivated users only
     */
    @GetMapping
    @Operation(summary = "List all users, optionally filtered by enabled status [ADMIN]")
    public ResponseEntity<List<UserAdminDTO>> listUsers(
            @RequestParam(required = false) Boolean enabled) {
        logger.info("Admin listing users — filter: enabled={}", enabled);
        return ResponseEntity.ok(userAdminService.getAllUsers(enabled));
    }


    @GetMapping("/{username}")
    @Operation(summary = "Get a single user by username [ADMIN]")
    public ResponseEntity<UserAdminDTO> getUser(@PathVariable String username) {
        logger.info("Admin fetching user: {}", username);
        return ResponseEntity.ok(userAdminService.getUserByUsername(username));
    }


    @PatchMapping("/{username}/role")
    @Operation(summary = "Change a user's role [ADMIN]")
    public ResponseEntity<UserAdminDTO> updateRole(
            @PathVariable String username,
            @RequestBody Map<String, String> body,
            Authentication auth) {

        String newRole = body.get("role");
        if (newRole == null || newRole.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Admin [{}] updating role of [{}] to [{}]",
                auth.getName(), username, newRole);

        UserAdminDTO updated = userAdminService.updateUserRole(
                username, newRole.toUpperCase(), auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate a user account [ADMIN]")
    public ResponseEntity<UserAdminDTO> updateStatus(
            @PathVariable String username,
            @RequestBody Map<String, Boolean> body,
            Authentication auth) {

        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }

        logger.info("Admin [{}] setting enabled={} for [{}]",
                auth.getName(), enabled, username);

        UserAdminDTO updated = userAdminService.updateUserStatus(
                username, enabled, auth.getName());
        return ResponseEntity.ok(updated);
    }
}
