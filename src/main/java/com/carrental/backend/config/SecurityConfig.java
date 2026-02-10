package com.carrental.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ❌ WYŁĄCZAMY WSZYSTKIE LOGOWANIA
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ POZWALAMY NA STRONY I API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",               // 👈 PANEL
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/cars/**",
                                "/customers/**",
                                "/rentals/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
