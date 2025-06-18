package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider.obterUsuarioLogado;
import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioMapper;
import com.autenticacao.api.app.exception.ValidacaoException;
import com.autenticacao.api.app.exception.ValidacaoNotFoundException;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.AutenticacaoCadastroService;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.app.util.ValidatorUsuarioUtil;
import com.autenticacao.api.app.util.enums.MensagemSistema;
import com.autenticacao.api.app.util.enums.UserRole;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela gestão de usuários. Implementa operações para criação, atualização,
 * desativação e consulta de usuários.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final UsuarioMapper usuarioMapper;
  private final ValidatorUsuarioUtil validatorUsuarioUtil;
  private final HistoricoUsuarioService historicoUsuarioService;
  private final AutenticacaoCadastroService autenticacaoCadastroService;

  /**
   * Busca um usuário pelo seu ‘ID’.
   *
   * @param id Identificador único do usuário.
   * @return Dados detalhados do usuário.
   * @throws ValidacaoNotFoundException se usuário não for encontrado.
   */
  @Override
  public UsuarioDetalhadoResponse buscarPorId(UUID id) {
    return executarComandoComTratamentoErroComMensagem(
        () -> usuarioMapper.toDetalhado(obterUsuarioOuFalhar(id)),
        ERRO_AO_BUSCAR_USUARIO.getChave());
  }

  @Override
  public Optional<UsuarioResumoResponse> buscarPorEmail(String email) {
    return executarComandoComTratamentoErroComMensagem(
        () -> usuarioRepository.findByEmail(email).map(usuarioMapper::toResumo),
        ERRO_AO_BUSCAR_USUARIO.getChave());
  }

  /**
   * Cria um usuário a partir dos dados fornecidos. Valida a existência prévia e dados do usuário.
   *
   * @param request Dados para cadastro de novo usuário.
   * @return Resumo do usuário criado.
   * @throws ValidacaoException se e-mail ou telefone já estiverem cadastrados.
   */
  @Override
  public UsuarioResumoResponse criarUsuario(CadastroUsuarioRequest request) {
    validarNovoUsuario(request);
    Usuario usuario = salvarUsuario(usuarioMapper.toEntity(request));
    autenticacaoCadastroService.criar(request, usuario);
    historicoUsuarioService.registrarHistoricoCompleto(null, usuario);

    return usuarioMapper.toResumo(usuario);
  }

  /**
   * Atualiza dados parciais do usuário identificado pelo ‘ID’.
   *
   * @param id Identificador do usuário.
   * @param request Dados para atualização.
   * @return Dados detalhados do usuário atualizado.
   * @throws ValidacaoNotFoundException se usuário não existir.
   */
  @Override
  public UsuarioDetalhadoResponse atualizarUsuario(UUID id, AtualizarUsuarioRequest request) {
    return executarComandoComTratamentoErroComMensagem(
        () -> processarAtualizacaoUsuario(id, request), ERRO_AO_ATUALIZAR_DADOS_USUARIO.getChave());
  }

  /**
   * Processa atualização parcial do usuário.
   *
   * @param id Identificador do usuário.
   * @param request Dados para atualização.
   * @return Dados detalhados do usuário atualizado.
   */
  private UsuarioDetalhadoResponse processarAtualizacaoUsuario(
      UUID id, AtualizarUsuarioRequest request) {
    validarDados(request);

    Usuario usuario = buscarUsuarioPorId(id);

    Usuario usuarioAtualizado = aplicarAtualizacoesParciais(request, usuario);

    autenticacaoCadastroService.atualizar(request);

    historicoUsuarioService.registrarHistoricoCompleto(usuario, usuarioAtualizado);

    Usuario salvo = salvarUsuario(usuarioAtualizado);

    return usuarioMapper.toDetalhado(salvo);
  }

  /**
   * Lista todos os usuários com dados resumidos.
   *
   * @return Lista com usuários resumidos.
   */
  @Override
  public List<UsuarioResumoResponse> listarTodos() {
    var usuarioLogado = obterUsuarioLogado();
    if (usuarioLogado == null || !usuarioLogado.getRole().equals(UserRole.ADMIN)) {
      throw new ValidacaoException(ACESSO_NEGADO);
    }
    return usuarioRepository.findAll().stream().map(usuarioMapper::toResumo).toList();
  }
  /*
   * ====================
   * MÉTODOS PRIVADOS
   * ====================
   */

  /**
   * Valida dados para atualização parcial do usuário.
   *
   * @param request Dados de atualização.
   */
  private void validarDados(AtualizarUsuarioRequest request) {
    validatorUsuarioUtil.validarFormatoEmailETelefone(request);
  }

  /**
   * Aplica atualizações parciais a uma entidade Usuario usando padrão builder.
   *
   * @param request Dados para atualização.
   * @param usuario Entidade original a ser atualizada.
   * @return Novo objeto usuário atualizado.
   */
  private Usuario aplicarAtualizacoesParciais(AtualizarUsuarioRequest request, Usuario usuario) {
    Usuario.UsuarioBuilder builder = usuario.toBuilder();

    Optional.ofNullable(request.nome()).filter(s -> !s.isBlank()).ifPresent(builder::nome);

    Optional.ofNullable(request.sobrenome())
        .filter(s -> !s.isBlank())
        .ifPresent(builder::sobrenome);

    Optional.ofNullable(request.telefone()).filter(s -> !s.isBlank()).ifPresent(builder::telefone);

    Optional.ofNullable(request.email()).filter(s -> !s.isBlank()).ifPresent(builder::email);

    Optional.ofNullable(request.dataNascimento()).ifPresent(builder::dataNascimento);

    return builder.build();
  }

  /**
   * Busca um usuário pelo 'ID' ou lança exceção se não existir.
   *
   * @param id Identificador do usuário.
   * @return Usuário encontrado.
   * @throws ValidacaoNotFoundException se não encontrado.
   */
  private Usuario obterUsuarioOuFalhar(UUID id) {
    return usuarioRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ValidacaoNotFoundException(
                    MensagemSistema.USUARIO_NAO_ENCONTRADO_POR_ID, id.toString()));
  }

  /**
   * Busca usuário pelo ID, invocando método utilitário.
   *
   * @param id Identificador do usuário.
   * @return Usuário encontrado.
   */
  private Usuario buscarUsuarioPorId(UUID id) {
    return obterUsuarioOuFalhar(id);
  }

  /**
   * Salva entidade usuário no repositório com tratamento de erros.
   *
   * @param usuario Entidade a ser salva.
   * @return Usuário salvo.
   */
  private Usuario salvarUsuario(Usuario usuario) {
    return executarComandoComTratamentoErroComMensagem(
        () -> usuarioRepository.save(usuario), ERRO_AO_TENTAR_CRIAR_USUARIO.getChave());
  }

  /**
   * Valida dados para criação de novo usuário, verificando duplicidade de e-mail e telefone.
   *
   * @param request Dados para cadastro.
   * @throws ValidacaoException se e-mail ou telefone já cadastrados.
   */
  private void validarNovoUsuario(CadastroUsuarioRequest request) {
    this.validatorUsuarioUtil.validarFormatoEmailETelefone(request);

    if (usuarioRepository.findByEmail(request.email()).isPresent()) {
      throw new ValidacaoException(EMAIL_JA_CADASTRADO, request.email());
    }

    if (usuarioRepository.findByTelefone(request.telefone()).isPresent()) {
      throw new ValidacaoException(TELEFONE_JA_CADASTRADO, request.telefone());
    }
  }
}
