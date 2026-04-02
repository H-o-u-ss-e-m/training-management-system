package com.isi.gestion_formation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Cette méthode définit l'outil de cryptage qui sera utilisé dans toute l'application
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cette méthode configure la sécurité (on autorise tout pour le moment)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactive CSRF pour tester facilement avec Postman
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Autorise tout le monde

        return http.build();
    }
}