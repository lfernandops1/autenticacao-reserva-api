package com.autenticacao.api.app.config.security;

import static com.autenticacao.api.app.Constantes.Permissoes.SEGREDO;
import static com.autenticacao.api.app.Constantes.Util.ZONE_OFFSET_BR;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_ENQUANTO_GERAVA_TOKEN_DE_ACESSO;
import static org.apache.naming.ResourceRef.AUTH;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Service
public class TokenService {

  public String generateToken(Usuario usuario) throws RuntimeException {
    try {
      Algorithm algorithm = Algorithm.HMAC256(SEGREDO);

      return JWT.create()
          .withIssuer(AUTH)
          .withSubject(usuario.getUsername())
          .withExpiresAt(generateExpirationDate())
          .sign(algorithm);

    } catch (JWTCreationException exception) {
      throw new RuntimeException(ERRO_ENQUANTO_GERAVA_TOKEN_DE_ACESSO.getChave(), exception);
    }
  }

  public String validateToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC256(SEGREDO);

      return JWT.require(algorithm).withIssuer(AUTH).build().verify(token).getSubject();
    } catch (JWTVerificationException exception) {
      return null;
    }
  }

  // Gera data de expiração para novos tokens
  private Instant generateExpirationDate() {
    return LocalDateTime.now().plusHours(2).toInstant(ZONE_OFFSET_BR);
  }

  // Extrai a data de expiração de um token já existente
  public Instant getExpirationDate(String token) {
    Algorithm algorithm = Algorithm.HMAC256(SEGREDO);
    return JWT.require(algorithm).withIssuer(AUTH).build().verify(token).getExpiresAt().toInstant();
  }
}
