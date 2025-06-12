package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.SenhaExpiradaException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.service.SenhaService;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do serviço responsável por operações relacionadas à senha do usuário, incluindo
 * validação de expiração e alteração de senha, com tratamento centralizado de exceções.
 */
@Service
@RequiredArgsConstructor
public class SenhaServiceImpl implements SenhaService {

  private final PasswordEncoder passwordEncoder;
  private final AutenticacaoRepository autenticacaoRepository;

  @Value("${seguranca.senha.validade-dias:90}")
  private long validadeSenhaDias;

  /**
   * Valida se a senha do usuário expirou, lançando exceção caso positivo. Aplica tratamento
   * centralizado para garantir robustez.
   *
   * @param usuario Usuário cuja senha será validada.
   * @throws SenhaExpiradaException se a senha do usuário estiver expirada.
   */
  @Override
  public void validarSenhaExpirada(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          if (senhaExpirada(usuario.getAutenticacao())) {
            throw new SenhaExpiradaException(SENHA_EXPIRADA.getChave());
          }
          return null;
        },
        ERRO_VALIDAR_EXPIRACAO_SENHA.getChave());
  }

  /**
   * Verifica se a senha do usuário está expirada com base na data de atualização da senha.
   *
   * @param autenticacao Entidade de autenticação contendo a data da última atualização da senha.
   * @return true se a senha estiver expirada, false caso contrário.
   */
  @Override
  public boolean senhaExpirada(Autenticacao autenticacao) {
    return autenticacao
        .getDataHoraAtualizacao()
        .plusDays(validadeSenhaDias)
        .isBefore(LocalDateTime.now());
  }

  /**
   * Altera a senha do usuário e atualiza a data da última modificação. O método utiliza tratamento
   * centralizado para erros inesperados.
   *
   * @param usuario Usuário que terá a senha alterada.
   * @param novaSenha Nova senha em texto plano que será codificada.
   */
  @Override
  public void alterarSenha(Usuario usuario, String novaSenha) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Autenticacao autenticacao = usuario.getAutenticacao();
          autenticacao.setSenha(passwordEncoder.encode(novaSenha));
          autenticacao.setDataHoraAtualizacao(LocalDateTime.now());
          autenticacaoRepository.save(autenticacao);
          return null;
        },
        ERRO_ALTERAR_SENHA.getChave());
  }
}
