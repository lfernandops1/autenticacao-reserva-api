package com.autenticacao.api.app.config.security;

import static com.autenticacao.api.app.Constantes.Util.ASTERISTICO;
import static com.autenticacao.api.app.Constantes.Util.BARRA_ALL;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping(BARRA_ALL)
        .allowedOrigins(ENDERECO + PORTA_4200)
        .allowedMethods(GET, POST, PUT, DELETE, PATCH, OPTIONS)
        .allowedHeaders(ASTERISTICO)
        .allowCredentials(true);
  }
}
