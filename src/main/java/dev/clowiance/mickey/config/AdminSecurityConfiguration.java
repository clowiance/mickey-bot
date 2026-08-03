package dev.clowiance.mickey.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

import java.util.UUID;

@Configuration
@EnableConfigurationProperties(AdminPanelProperties.class)
public class AdminSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService adminUsers(AdminPanelProperties properties, PasswordEncoder encoder) {
        if (properties.isEnabled() && (properties.getPassword() == null || properties.getPassword().isBlank())) {
            throw new IllegalStateException("ADMIN_PANEL_PASSWORD обов'язковий, коли адмін-панель увімкнена");
        }
        String username = properties.isEnabled() ? properties.getUsername() : "disabled-admin";
        String password = properties.isEnabled() ? properties.getPassword() : UUID.randomUUID().toString();
        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(encoder.encode(password))
                .roles("ADMIN")
                .build());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AdminPanelProperties properties) throws Exception {
        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health", "/error").permitAll();
                    if (properties.isEnabled()) {
                        auth.requestMatchers("/admin/**", "/actuator/**").hasRole("ADMIN");
                    } else {
                        auth.requestMatchers("/admin/**", "/actuator/**").denyAll();
                    }
                    auth.anyRequest().permitAll();
                })
                .httpBasic(Customizer.withDefaults())
                .formLogin(login -> login.defaultSuccessUrl("/admin", true))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
