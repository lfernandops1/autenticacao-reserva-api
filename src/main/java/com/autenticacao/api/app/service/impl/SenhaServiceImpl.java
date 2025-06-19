package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider.obterUsuarioLogado;
import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.SenhaExpiradaException;
import com.autenticacao.api.app.exception.ValidacaoException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.repository.HistoricoAutenticacaoRepository;
import com.autenticacao.api.app.service.SenhaService;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SenhaServiceImpl implements SenhaService {

  private final PasswordEncoder passwordEncoder;
  private final AutenticacaoRepository autenticacaoRepository;
  private final HistoricoAutenticacaoRepository historicoAutenticacaoRepository;

  @Value("${seguranca.senha.validade-dias:90}")
  private long validadeSenhaDias;

  @Override
  public void validarSenhaExpirada(Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          LocalDateTime dataUltimaAtualizacao = buscarDataUltimaAtualizacaoSenha(usuario);
          if (dataUltimaAtualizacao == null
              || dataUltimaAtualizacao.plusDays(validadeSenhaDias).isBefore(LocalDateTime.now())) {
            throw new SenhaExpiradaException(SENHA_EXPIRADA.getChave());
          }
          return null;
        },
        ERRO_VALIDAR_EXPIRACAO_SENHA.getChave());
  }

  @Override
  public void alterarSenha(String senhaAtual, String novaSenha) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Usuario usuario = obterUsuarioLogado();
          Autenticacao autenticacao = usuario.getAutenticacao();

          validarSenhaAtual(senhaAtual, autenticacao);

          autenticacao.setSenha(passwordEncoder.encode(novaSenha));
          autenticacaoRepository.save(autenticacao);

          registrarHistoricoAlteracaoSenha(autenticacao);

          return null;
        },
        ERRO_ALTERAR_SENHA.getChave());
  }

  private void validarSenhaAtual(String senhaAtual, Autenticacao autenticacao) {
    if (!passwordEncoder.matches(senhaAtual, autenticacao.getSenha())) {
      throw new ValidacaoException(SENHA_ATUAL_INCORRETA);
    }
  }

  private void registrarHistoricoAlteracaoSenha(Autenticacao autenticacao) {
    HistoricoAutenticacao historico =
        HistoricoAutenticacao.builder()
            .autenticacao(autenticacao)
            .tipoAlteracao(TipoMovimentacao.ATUALIZACAO_SENHA)
            .dataAlteracao(LocalDateTime.now())
            .camposAlterados("Senha alterada com sucesso")
            .build();

    historicoAutenticacaoRepository.save(historico);
  }

  private LocalDateTime buscarDataUltimaAtualizacaoSenha(Usuario usuario) {
    return historicoAutenticacaoRepository
        .findTopByUsuarioResponsavelOrderByDataHoraCriacaoDesc(usuario)
        .map(HistoricoAutenticacao::getDataAlteracao)
        .orElse(null);
  }

  @Override
  public boolean senhaExpirada(Autenticacao autenticacao) {
    throw new UnsupportedOperationException("Use validarSenhaExpirada com base no histórico.");
  }
}
