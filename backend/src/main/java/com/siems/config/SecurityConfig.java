package com.siems.config;

import com.siems.security.JwtAccessDeniedHandler;
import com.siems.security.JwtAuthenticationEntryPoint;
import com.siems.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                    // Suppliers -> ADMIN + IMPORT_MANAGER for write ops
                    .requestMatchers(HttpMethod.POST, "/api/v1/suppliers/**")
                        .hasAnyRole("ADMIN", "IMPORT_MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/suppliers/**")
                        .hasAnyRole("ADMIN", "IMPORT_MANAGER")

                    // Customers -> ADMIN + EXPORT_MANAGER for write ops
                    .requestMatchers(HttpMethod.POST, "/api/v1/customers/**")
                        .hasAnyRole("ADMIN", "EXPORT_MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/customers/**")
                        .hasAnyRole("ADMIN", "EXPORT_MANAGER")

                    // Inventory & Warehouses -> ADMIN + INVENTORY_MANAGER
                    .requestMatchers(HttpMethod.POST, "/api/v1/inventory/**")
                        .hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                    .requestMatchers(HttpMethod.POST, "/api/v1/warehouses/**")
                        .hasAnyRole("ADMIN", "INVENTORY_MANAGER")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/warehouses/**")
                        .hasAnyRole("ADMIN", "INVENTORY_MANAGER")

                    // Shipments -> ADMIN + IMPORT_MANAGER + EXPORT_MANAGER can create
                    .requestMatchers(HttpMethod.POST, "/api/v1/shipments/**")
                        .hasAnyRole("ADMIN", "IMPORT_MANAGER", "EXPORT_MANAGER")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/shipments/**")
                        .hasAnyRole("ADMIN", "IMPORT_MANAGER", "EXPORT_MANAGER", "INVENTORY_MANAGER")

                    // Analytics -> any authenticated manager/admin
                    .requestMatchers(HttpMethod.GET, "/api/v1/analytics/**")
                        .hasAnyRole("ADMIN", "IMPORT_MANAGER", "EXPORT_MANAGER", "INVENTORY_MANAGER")

                    // Deletes -> ADMIN only
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                        .hasRole("ADMIN")

                    // All other GET requests -> any authenticated user
                    .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()

                    .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
