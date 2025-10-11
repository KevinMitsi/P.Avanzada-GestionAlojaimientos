package com.avanzada.alojamientos.config;

import com.avanzada.alojamientos.security.CustomUserDetailsService;
import com.avanzada.alojamientos.security.JwtAuthenticationFilter;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String HOST_ROLE = "HOST";
    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/accommodations/**").permitAll()
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/password-reset/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Endpoints específicos por rol
                        .requestMatchers("/api/admin/**").hasRole(ADMIN_ROLE)
                        .requestMatchers("/api/host/**").hasAnyRole(HOST_ROLE, ADMIN_ROLE)

                        // Operaciones específicas de usuarios HOST
                        .requestMatchers(HttpMethod.POST, "/api/accommodations").hasRole(HOST_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/api/accommodations/**").hasRole(HOST_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/accommodations/**").hasRole(HOST_ROLE)
                        .requestMatchers(HttpMethod.POST, "/api/accommodations/*/images/**").hasRole(HOST_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").hasRole(HOST_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").hasRole(HOST_ROLE)


                        //Admin users
                        .requestMatchers("/api/users/admin/**").hasRole(ADMIN_ROLE)
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}