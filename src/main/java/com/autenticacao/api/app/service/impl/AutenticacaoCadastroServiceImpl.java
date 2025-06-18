package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider.obterUsuarioLogado;
import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_AO_DESATIVAR_AUTENTICACAO_DO_USUARIO;
import static com.autenticacao.api.app.util.enums.MensagemSistema.USUARIO_JA_POSSUI_AUTENTICACAO;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.AutenticacaoJaExistenteException;
import com.autenticacao.api.app.exception.UsuarioNaoEncontradoException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.service.AutenticacaoCadastroService;
import com.autenticacao.api.app.service.HistoricoAutenticacaoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AutenticacaoCadastroServiceImpl implements AutenticacaoCadastroService {

  private final AutenticacaoRepository autenticacaoRepository;
  private final HistoricoAutenticacaoService historicoAutenticacaoService;
  private final PasswordEncoder passwordEncoder;
  private static final Logger logger =
      LoggerFactory.getLogger(AutenticacaoCadastroServiceImpl.class);

  @Override
  public void criar(CadastroUsuarioRequest dto, Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          validarSeAutenticacaoJaExiste(usuario);

          Autenticacao novaAutenticacao = construirNovaAutenticacao(dto, usuario);

          usuario.setAutenticacao(novaAutenticacao);
          autenticacaoRepository.save(novaAutenticacao);

          historicoAutenticacaoService.registrarHistoricoCompleto(null, novaAutenticacao, usuario);

          logger.info("Autenticação criada para usuário: {}", usuario.getEmail());
          return null;
        },
        "Erro ao criar autenticação para o usuário.");
  }

  @Override
  public void atualizar(AtualizarUsuarioRequest dto) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Usuario usuario = obterUsuarioLogado();

          Autenticacao autenticacaoAntiga = usuario.getAutenticacao();
          if (autenticacaoAntiga == null) {
            throw new UsuarioNaoEncontradoException(
                "Usuário não possui autenticação para atualizar.");
          }

          Autenticacao autenticacaoAtualizada =
              construirAutenticacaoAtualizada(dto, autenticacaoAntiga);
          usuario.setAutenticacao(autenticacaoAtualizada);

          Autenticacao autenticacaoPersistida = autenticacaoRepository.save(autenticacaoAtualizada);

          historicoAutenticacaoService.registrarHistoricoCompleto(
              autenticacaoAntiga, autenticacaoPersistida, usuario);

          logger.info("Autenticação atualizada para usuário: {}", usuario.getEmail());
          return null;
        },
        "Erro ao atualizar autenticação para o usuário.");
  }

  @Override
  public void desativar(UUID usuarioId) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Autenticacao authAntes =
              autenticacaoRepository
                  .buscarPorUsuarioId(usuarioId)
                  .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

          Autenticacao authDepois = cloneAutenticacaoParaAlteracao(authAntes);
          authDepois.setAtivo(false);

          autenticacaoRepository.save(authDepois);
          historicoAutenticacaoService.registrarHistoricoCompleto(
              authAntes, authDepois, authAntes.getUsuario());

          logger.info("Autenticação desativada para usuário ID: {}", usuarioId);
          return null;
        },
        ERRO_AO_DESATIVAR_AUTENTICACAO_DO_USUARIO.getChave());
  }

  private void validarSeAutenticacaoJaExiste(Usuario usuario) {
    autenticacaoRepository
        .findByUsuario(usuario)
        .ifPresent(
            auth -> {
              throw new AutenticacaoJaExistenteException(USUARIO_JA_POSSUI_AUTENTICACAO.getChave());
            });
  }

  private Autenticacao construirNovaAutenticacao(CadastroUsuarioRequest dto, Usuario usuario) {
    Autenticacao auth = new Autenticacao();
    auth.setEmail(usuario.getEmail());
    auth.setSenha(passwordEncoder.encode(dto.senha()));
    auth.setUsuario(usuario);
    auth.setAtivo(true);
    return auth;
  }

  private Autenticacao construirAutenticacaoAtualizada(
      AtualizarUsuarioRequest dto, Autenticacao atual) {
    Autenticacao nova = cloneAutenticacaoParaAlteracao(atual);

    if (dto.senha() != null && !dto.senha().isBlank()) {
      nova.setSenha(passwordEncoder.encode(dto.senha()));
    }
    if (dto.email() != null && !dto.email().isBlank()) {
      nova.setEmail(dto.email());
    }

    return nova;
  }

  private Autenticacao cloneAutenticacaoParaAlteracao(Autenticacao original) {
    Autenticacao clone = new Autenticacao();
    clone.setId(original.getId());
    clone.setEmail(original.getEmail());
    clone.setSenha(original.getSenha());
    clone.setUsuario(original.getUsuario());
    clone.setAtivo(original.getAtivo());
    return clone;
  }
}
