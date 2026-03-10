package com.services.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults()) // Garante que o nosso CorsConfig continua a funcionar
                .csrf(csrf -> csrf.disable()) // Desativa proteção CSRF pois usaremos Tokens JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Rotas Abertas (Qualquer um pode aceder para conseguir logar ou registar)
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                        // Apenas Administradores podem gerenciar usuários
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        
                        // Rotas de Busca (O seu requisito: "Usuario, que teria acesso a busca")
                        // ADMIN e PROFESSIONAL também podem buscar
                        .requestMatchers(HttpMethod.GET, "/professionals/search").hasAnyRole("USER", "PROFESSIONAL", "ADMIN")
                        
                        // Rotas de Cadastro de Profissional e Categorias
                        .requestMatchers(HttpMethod.POST, "/professionals").hasAnyRole("PROFESSIONAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/categories").hasAnyRole("PROFESSIONAL", "ADMIN")
                        
                        // Qualquer outra rota precisa de autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Ensina o Spring Security a fazer a verificação de login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Ensina o Spring a encriptar as palavras-passe no banco de dados
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}