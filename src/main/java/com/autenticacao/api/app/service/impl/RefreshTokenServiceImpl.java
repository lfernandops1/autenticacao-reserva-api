package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.config.security.provider.TokenGenerator;
import com.autenticacao.api.app.domain.entity.RefreshToken;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.RefreshTokenInvalidoException;
import com.autenticacao.api.app.repository.RefreshTokenRepository;
import com.autenticacao.api.app.service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerir Refresh ‘Tokens’. Responsável pela criação, validação, recuperação de usuário
 * e exclusão de ‘tokens’.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${seguranca.refresh-token.validade-minutos:10080}") // 7 dias
  private long validadeTokenMinutos;

  private static final int MAX_REFRESHES = 10;

  private final TokenGenerator tokenGenerator;

  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Cria um refresh ‘token’ para o usuário informado.
   *
   * @param usuario o usuário para quem o token será criado
   * @return token gerado
   */
  public String createRefreshToken(Usuario usuario) {
    return executarComandoComTratamentoErroComMensagem(
        () -> {
          String token = tokenGenerator.gerarToken(); // aqui!
          RefreshToken refreshToken = new RefreshToken();
          refreshToken.setToken(token);
          refreshToken.setUsuario(usuario);
          refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(validadeTokenMinutos));
          refreshToken.setRefreshCount(0);
          refreshTokenRepository.save(refreshToken);
          log.info("Refresh token criado para usuário: {}", usuario.getEmail());
          return token;
        },
        ERRO_CRIAR_REFRESH_TOKEN.getChave());
  }

  /** Gera token aleatório seguro e longo */
  private String gerarTokenSeguro() {
    byte[] randomBytes = new byte[64];
    secureRandom.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  /**
   * Valida se o ‘token’ informado é válido, não expirou e ainda pode ser renovado.
   *
   * @param token token a ser validado
   * @return true se válido, false caso contrário
   * @throws RefreshTokenInvalidoException se token for nulo, vazio ou inválido
   */
  public boolean isValid(String token) {
    if (token == null || token.isBlank()) {
      throw new RefreshTokenInvalidoException(TOKEN_NULO_OU_VAZIO.getChave());
    }
    return executarComandoComTratamentoErroComMensagem(
        () -> {
          RefreshToken rt = refreshTokenRepository.findByToken(token).orElse(null);
          if (rt == null) return false;
          if (rt.getExpiryDate().isBefore(LocalDateTime.now())) return false;
          if (rt.getRefreshCount() >= MAX_REFRESHES) return false; // limite de renovações
          return true;
        },
        ERRO_VALIDAR_REFRESH_TOKEN.getChave());
  }

  /**
   * Retorna o usuário associado ao ‘token’ informado.
   *
   * @param token token válido
   * @return usuário relacionado ao token
   * @throws RefreshTokenInvalidoException se token inválido ou expirado
   */
  public Usuario getUsuario(String token) {
    if (token == null || token.isBlank()) {
      throw new RefreshTokenInvalidoException(TOKEN_NULO_OU_VAZIO.getChave());
    }
    return executarComandoComTratamentoErroComMensagem(
        () ->
            refreshTokenRepository
                .findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(RefreshToken::getUsuario)
                .orElseThrow(
                    () ->
                        new RefreshTokenInvalidoException(
                            REFRESH_TOKEN_INVALIDO_OU_EXPIRADO.getChave())),
        ERRO_OBTER_USUARIO_REFRESH_TOKEN.getChave());
  }

  /**
   * Remove todos os refresh ‘tokens’ associados a um usuário.
   *
   * @param usuario usuário cujos tokens serão removidos
   */
  public void deleteByUsuario(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          refreshTokenRepository.deleteByUsuario(usuario);
          log.info("Refresh tokens removidos para usuário: {}", usuario.getEmail());
          return null;
        },
        ERRO_REMOVER_REFRESH_TOKENS_USUARIO.getChave());
  }

  /** Remove refresh token específico (usado para rotação e revogação). */
  @Transactional
  public void deleteByToken(String token) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          refreshTokenRepository.deleteByToken(token);
          log.info("Refresh token removido: {}", token);
          return null;
        },
        ERRO_REMOVER_REFRESH_TOKENS_USUARIO.getChave());
  }

  /**
   * Rotaciona o refresh token: remove o antigo e cria um novo. Incrementa contador de renovações.
   */
  public String rotateRefreshToken(String oldToken) {
    return executarComandoComTratamentoErroComMensagem(
        () -> {
          RefreshToken oldRefreshToken =
              refreshTokenRepository
                  .findByToken(oldToken)
                  .orElseThrow(
                      () ->
                          new RefreshTokenInvalidoException(
                              REFRESH_TOKEN_INVALIDO_OU_EXPIRADO.getChave()));

          if (oldRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenInvalidoException(REFRESH_TOKEN_INVALIDO_OU_EXPIRADO.getChave());
          }

          if (oldRefreshToken.getRefreshCount() >= MAX_REFRESHES) {
            throw new RefreshTokenInvalidoException("Limite máximo de renovações atingido");
          }

          Usuario usuario = oldRefreshToken.getUsuario();

          // Remove o token antigo
          refreshTokenRepository.delete(oldRefreshToken);

          // Cria um novo refresh token com contador incrementado
          RefreshToken novoToken = new RefreshToken();
          novoToken.setToken(gerarTokenSeguro());
          novoToken.setUsuario(usuario);
          novoToken.setExpiryDate(LocalDateTime.now().plusMinutes(validadeTokenMinutos));
          novoToken.setRefreshCount(oldRefreshToken.getRefreshCount() + 1);
          refreshTokenRepository.save(novoToken);

          log.info("Refresh token rotacionado para usuário: {}", usuario.getEmail());

          return novoToken.getToken();
        },
        ERRO_CRIAR_REFRESH_TOKEN.getChave());
  }
}
