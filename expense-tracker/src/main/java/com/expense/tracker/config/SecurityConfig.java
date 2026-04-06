package com.expense.tracker.config;

import com.expense.tracker.model.Role;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.RoleRepository;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.service.UserDetailsServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/register",
                                "/css/**", "/js/**", "/images/**",
                                "/h2-console/**"
                        ).permitAll()


                        .requestMatchers("/api/auth/**").permitAll()

                        // Admin-only
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Dashboard/summary — Analyst and Admin only
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ANALYST", "ADMIN")

                        // Everything else requires at least a login
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Seeds the three roles and one demo user per role on startup.
     * Uses orElseGet so re-runs are safe — nothing is duplicated.
     *
     * Demo credentials (for testing):
     *   admin   / admin123   → ROLE_ADMIN   + ROLE_USER
     *   analyst / analyst123 → ROLE_ANALYST + ROLE_USER
     *   viewer  / viewer123  → ROLE_VIEWER  + ROLE_USER
     */
    @Bean
    public CommandLineRunner setupDefaults(UserRepository userRepository,
                                           RoleRepository roleRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            // Create roles if not exist
            Role roleUser = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role roleViewer = roleRepository.findByName("ROLE_VIEWER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_VIEWER")));

            Role roleAnalyst = roleRepository.findByName("ROLE_ANALYST")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ANALYST")));

            Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));


            // Create admin user if not exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(List.of(roleAdmin, roleUser));
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println("✅ Admin user created: admin / admin123  [ADMIN]");
            }

            if (userRepository.findByUsername("analyst").isEmpty()) {
                User analyst = new User();
                analyst.setUsername("analyst");
                analyst.setPassword(passwordEncoder.encode("analyst123"));
                analyst.setRoles(List.of(roleAnalyst, roleUser));
                analyst.setEnabled(true);
                userRepository.save(analyst);
                System.out.println("✅ Seeded user  →  analyst / analyst123  [ANALYST]");
            }

            if (userRepository.findByUsername("viewer").isEmpty()) {
                User viewer = new User();
                viewer.setUsername("viewer");
                viewer.setPassword(passwordEncoder.encode("viewer123"));
                viewer.setRoles(List.of(roleViewer, roleUser));
                viewer.setEnabled(true);
                userRepository.save(viewer);
                System.out.println("✅ Seeded user  →  viewer / viewer123  [VIEWER]");
            }
        };
    }
}
