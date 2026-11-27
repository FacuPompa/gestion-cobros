package com.facupompa.pellet.web.config;

import com.facupompa.pellet.web.security.JsonWebTokenUtils;
import com.facupompa.pellet.web.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http, JsonWebTokenUtils jwtUtils, UserDetailsService userDetailsService) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);
        http.authorizeHttpRequests(authorization -> authorization
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/cancel").hasRole("ADMIN")
                        .requestMatchers("/api/customers", "/api/customers/**", "/api/debts", "/api/debts/**", "/api/payments", "/api/payments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .anyRequest().denyAll()
        )
                .csrf(csrfConfig -> csrfConfig.ignoringRequestMatchers("/api/customers", "/api/customers/**", "/api/debts", "/api/debts/**", "/api/payments", "/api/payments/**", "/api/auth/login"))
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
    }
}
