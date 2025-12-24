package com.task.user_service.security.config;

import com.task.user_service.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter filter) {
        this.jwtAuthenticationFilter = filter;
        // 🛑 IF YOU DON'T SEE THIS IN CONSOLE, YOUR CONFIG IS IGNORED
        System.out.println(">>> ------------------------------------------ <<<");
        System.out.println(">>> FATAL: SECURITY CONFIG LOADED SUCCESSFULLY <<<");
        System.out.println(">>> ------------------------------------------ <<<");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DISABLE DEFAULTS
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. STATELESS SESSION (Don't keep user in memory)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. CUSTOM ERROR HANDLING (Return JSON 401, not HTML)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid or missing token\"}");
                        })
                )

                // 4. DEFINE PUBLIC vs PRIVATE ENDPOINTS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/lookup", "/api/users/validate/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 5. INJECT YOUR FILTER
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 6. STOP GENERATED PASSWORD
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}