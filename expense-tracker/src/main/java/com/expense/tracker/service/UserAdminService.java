package com.expense.tracker.service;

import com.expense.tracker.dto.UserAdminDTO;
import com.expense.tracker.model.Role;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.RoleRepository;
import com.expense.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    private static final Logger logger = LoggerFactory.getLogger(UserAdminService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }


    /**
     * Returns all users. If the optional `enabled` query param is supplied,
     * filters to only active or only inactive accounts.
     *
     * @param enabled null → return everyone; true/false → filter by status
     */
    public List<UserAdminDTO> getAllUsers(Boolean enabled) {
        List<User> users = (enabled == null)
                ? userRepository.findAll()
                : userRepository.findByEnabled(enabled);

        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single user by username, or 404 if not found.
     */
    public UserAdminDTO getUserByUsername(String username) {
        User user = resolveUser(username);
        return toDTO(user);
    }


    /**
     * Replaces the user's entire role list with a single new role.
     * @param callerUsername the admin making the request (for self-demotion guard)
     */
    @Transactional
    public UserAdminDTO updateUserRole(String username,
                                       String newRoleName,
                                       String callerUsername) {
        if (username.equals(callerUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own role");
        }

        User user = resolveUser(username);

        Role baseRole = resolveRole("ROLE_USER");
        Role newRole  = resolveRole(newRoleName);

        // Replace roles — always keep ROLE_USER as the base
        user.setRoles(List.of(baseRole, newRole));
        userRepository.save(user);

        logger.info("Admin [{}] changed role of [{}] to [{}]",
                callerUsername, username, newRoleName);
        return toDTO(user);
    }

    // ── Status management ─────────────────────────────────────────────────────

    /**
     * Activates or deactivates a user account without deleting it.
     * An inactive user cannot log in (Spring Security checks isEnabled()).
     *
     * Prevents an admin from deactivating themselves.
     *
     * @param username       the user to update
     * @param enabled        true to activate, false to deactivate
     * @param callerUsername the admin making the request (for self-lock guard)
     */
    @Transactional
    public UserAdminDTO updateUserStatus(String username,
                                         boolean enabled,
                                         String callerUsername) {
        if (username.equals(callerUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own account status");
        }

        User user = resolveUser(username);
        user.setEnabled(enabled);
        userRepository.save(user);

        logger.info("Admin [{}] {} account [{}]",
                callerUsername, enabled ? "activated" : "deactivated", username);
        return toDTO(user);
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + username));
    }

    private Role resolveRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Role not found: " + roleName
                                + " — valid values: ROLE_VIEWER, ROLE_ANALYST, ROLE_ADMIN"));
    }

    private UserAdminDTO toDTO(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return new UserAdminDTO(user.getUsername(), user.isEnabled(), roleNames);
    }

}
