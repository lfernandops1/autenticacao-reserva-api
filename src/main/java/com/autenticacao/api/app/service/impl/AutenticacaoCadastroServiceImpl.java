package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_AO_DESATIVAR_AUTENTICACAO_DO_USUARIO;
import static com.autenticacao.api.app.util.enums.MensagemSistema.USUARIO_JA_POSSUI_AUTENTICACAO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.autenticacao.api.app.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.service.AutenticacaoCadastroService;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do serviço responsável pelo cadastro e desativação da autenticação de usuários, com
 * tratamento centralizado de exceções para garantir robustez e rastreabilidade dos erros.
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoCadastroServiceImpl implements AutenticacaoCadastroService {

  private final AutenticacaoRepository autenticacaoRepository;
  private final PasswordEncoder passwordEncoder;
  private static final Logger logger =
      LoggerFactory.getLogger(AutenticacaoCadastroServiceImpl.class);

  /**
   * Cria uma autenticação para o usuário informado, utilizando tratamento centralizado de exceções.
   * Caso o usuário já possua autenticação, lança {@link AutenticacaoJaExistenteException}.
   *
   * @param dto Dados de cadastro da autenticação, incluindo senha.
   * @param usuario Usuário ao qual a autenticação será vinculada.
   * @throws AutenticacaoJaExistenteException caso já exista autenticação para o usuário.
   * @throws ValidacaoException em caso de erro de validação.
   * @throws ValidacaoNotFoundException em caso de recurso não encontrado durante validação.
   * @throws AutenticacaoApiRunTimeException em caso de erro inesperado na criação.
   */
  @Override
  public void criar(CadastroUsuarioRequest dto, Usuario usuario) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          autenticacaoRepository
              .findByUsuario(usuario)
              .ifPresent(
                  auth -> {
                    throw new AutenticacaoJaExistenteException(
                        USUARIO_JA_POSSUI_AUTENTICACAO.getChave());
                  });

          Autenticacao auth = new Autenticacao();
          auth.setEmail(usuario.getEmail());
          auth.setSenha(passwordEncoder.encode(dto.senha()));
          auth.setUsuario(usuario);
          auth.setAtivo(true);
          auth.setDataHoraCriacao(LocalDateTime.now());
          auth.setDataHoraAtualizacao(LocalDateTime.now());
          usuario.setAutenticacao(auth);

          autenticacaoRepository.save(auth);
          logger.info("Autenticação criada para usuário: {}", usuario.getEmail());
          return null;
        },
        "Erro ao criar autenticação para o usuário.");
  }

  /**
   * Desativa a autenticação do usuário identificado pelo ‘ID’ informado, utilizando tratamento
   * centralizado de exceções. Caso o usuário não seja encontrado, lança {@link
   * UsuarioNaoEncontradoException}.
   *
   * @param usuarioId ‘ID’ do usuário cuja autenticação será desativada.
   * @throws UsuarioNaoEncontradoException caso não exista autenticação para o usuário.
   * @throws ValidacaoException em caso de erro de validação.
   * @throws ValidacaoNotFoundException em caso de recurso não encontrado durante validação.
   * @throws AutenticacaoApiRunTimeException em caso de erro inesperado na desativação.
   */
  @Override
  public void desativar(UUID usuarioId) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Autenticacao auth =
              autenticacaoRepository
                  .buscarPorUsuarioId(usuarioId)
                  .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

          auth.setAtivo(false);
          auth.setDataHoraExclusao(LocalDateTime.now());
          autenticacaoRepository.save(auth);
          logger.info("Autenticação desativada para usuário ID: {}", usuarioId);
          return null;
        },
        ERRO_AO_DESATIVAR_AUTENTICACAO_DO_USUARIO.getChave());
  }
}
