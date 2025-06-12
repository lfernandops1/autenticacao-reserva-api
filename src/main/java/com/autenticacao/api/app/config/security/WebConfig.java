package com.autenticacao.api.app.config.security;

import static com.autenticacao.api.app.Constantes.Http.*;
import static com.autenticacao.api.app.Constantes.Util.ASTERISCO;
import static com.autenticacao.api.app.Constantes.Util.BARRA_ALL;
import static com.autenticacao.api.app.Constantes.Web.LOCALHOST;
import static com.autenticacao.api.app.Constantes.Web.PORTA_4200;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping(BARRA_ALL)
        .allowedOrigins(LOCALHOST + PORTA_4200)
        .allowedMethods(GET, POST, PUT, DELETE, PATCH, OPTIONS)
        .allowedHeaders(ASTERISCO)
        .allowCredentials(true);
    registry
        .addMapping("/v3/api-docs/**")
        .allowedOrigins(LOCALHOST + PORTA_4200)
        .allowedMethods(GET, POST, PUT, DELETE, PATCH, OPTIONS)
        .allowedHeaders("*")
        .allowCredentials(true);

    registry
        .addMapping("/swagger-ui/**")
        .allowedOrigins(LOCALHOST + PORTA_4200)
        .allowedMethods(GET, POST, PUT, DELETE, PATCH, OPTIONS)
        .allowedHeaders(ASTERISCO)
        .allowCredentials(true);

    registry
        .addMapping("/swagger-ui.html")
        .allowedOrigins(LOCALHOST + PORTA_4200)
        .allowedMethods(GET, POST, PUT, DELETE, PATCH, OPTIONS)
        .allowedHeaders(ASTERISCO)
        .allowCredentials(true);
  }
}
