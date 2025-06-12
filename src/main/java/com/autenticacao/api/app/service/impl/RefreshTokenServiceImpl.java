package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.config.security.provider.TokenGenerator;
import com.autenticacao.api.app.domain.entity.RefreshToken;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.RefreshTokenRepository;
import com.autenticacao.api.app.service.RefreshTokenService;
import com.autenticacao.api.app.exception.RefreshTokenInvalidoException;

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
  private final TokenGenerator tokenGenerator;

  @Value("${seguranca.refresh-token.validade-minutos:10080}") // 7 dias
  private long validadeTokenMinutos;

  /**
   * Cria um refresh ‘token’ para o usuário informado.
   *
   * @param usuario o usuário para quem o token será criado
   * @return token gerado
   */
  public String createRefreshToken(Usuario usuario) {
    return executarComandoComTratamentoErroComMensagem(
        () -> {
          String token = tokenGenerator.gerarToken();
          RefreshToken refreshToken = new RefreshToken();
          refreshToken.setToken(token);
          refreshToken.setUsuario(usuario);
          refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(validadeTokenMinutos));
          refreshTokenRepository.save(refreshToken);
          log.info("Refresh token criado para usuário: {}", usuario.getEmail());
          return token;
        },
        ERRO_CRIAR_REFRESH_TOKEN.getChave());
  }

  /**
   * Valida se o ‘token’ informado é válido e não expirou.
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
        () ->
            refreshTokenRepository
                .findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .isPresent(),
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
}
