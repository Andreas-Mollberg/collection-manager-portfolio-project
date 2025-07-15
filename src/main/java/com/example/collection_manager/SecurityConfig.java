package com.example.collection_manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/**").permitAll()
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Temp for ease of testing


        return http.build();
    }
}
