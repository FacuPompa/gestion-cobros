package com.facupompa.cobranzas.web.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorization -> authorization
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/*/cancel").hasRole("ADMIN")
                        .requestMatchers("/api/customers", "/api/customers/**", "/api/debts", "/api/debts/**", "/api/payments", "/api/payments/**").authenticated()
                .anyRequest().denyAll()
        )
                .csrf(csrfConfig -> csrfConfig.ignoringRequestMatchers("/api/customers", "/api/customers/**", "/api/debts", "/api/debts/**", "/api/payments", "/api/payments/**"))
                .httpBasic(Customizer.withDefaults());

                return http.build();
    }
}
