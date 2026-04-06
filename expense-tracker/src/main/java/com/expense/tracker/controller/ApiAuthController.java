package com.expense.tracker.controller;

import com.expense.tracker.model.Role;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Auth", description = "JSON login/logout for API clients and Postman testing")
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private static final Logger logger = LoggerFactory.getLogger(ApiAuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Autowired
    public ApiAuthController(AuthenticationManager authenticationManager,
                             UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/auth/login
     *
     * Request body:
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "JSON login — returns session cookie on success")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",   "Bad Request",
                    "message", "username and password are required"
            ));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            //Response
            User user = userRepository.findByUsername(username).orElseThrow();
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            logger.info("API login successful for user: {}", username);

            return ResponseEntity.ok(Map.of(
                    "message",  "Login successful",
                    "username", username,
                    "roles",    roles,
                    "hint",     "JSESSIONID cookie has been set — Postman will send it automatically"
            ));

        } catch (DisabledException ex) {
            // Account deactivated by admin
            logger.warn("Login attempt for disabled account: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error",   "Unauthorized",
                    "message", "This account has been deactivated"
            ));

        } catch (BadCredentialsException ex) {
            // Wrong password or username not found
            logger.warn("Failed login attempt for username: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error",   "Unauthorized",
                    "message", "Invalid username or password"
            ));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "JSON logout — invalidates the current session")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = "unknown";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) username = auth.getName();

            session.invalidate();
            SecurityContextHolder.clearContext();
            logger.info("API logout for user: {}", username);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }


    /**
     * GET /api/auth/me
     *
     * Returns the currently authenticated user's details.
     */
    @GetMapping("/me")
    @Operation(summary = "Returns the currently authenticated user")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error",   "Unauthorized",
                    "message", "No active session"
            ));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "username", username,
                "roles",    roles,
                "enabled",  user.isEnabled()
        ));
    }
}
