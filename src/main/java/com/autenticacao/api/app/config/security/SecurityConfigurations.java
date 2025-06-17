package com.autenticacao.api.app.config.security;

import static com.autenticacao.api.app.Constantes.Rotas.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

  private final SecurityFilter securityFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    return httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Permite acesso público à documentação Swagger
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, API_AUTENTICAR + LOGIN)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, API_USUARIOS + CRIAR)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, API_USUARIOS + CRIAR_ADMIN)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, API_AUTENTICAR + REFRESH_TOKEN)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_AUTENTICAR + ALTERAR_SENHA)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_AUTENTICAR + DESATIVAR)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_USUARIOS + DESATIVAR)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_AUTENTICAR + LISTAR_TODOS)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_USUARIOS + BUSCAR)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, API_USUARIOS + ATUALIZAR_POR_ID)
                    .authenticated()
                    .requestMatchers(HttpMethod.DELETE, API_AUTENTICAR + REVOKE_REFRESH_TOKEN)
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, API_USUARIOS + BUSCAR_POR_ID)
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  @Lazy
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
