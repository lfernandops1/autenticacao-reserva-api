package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.Constantes.Mensagens.*;
import static com.autenticacao.api.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.util.SecurityUtil.obterUsuarioLogado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioMapper;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.AutenticacaoService;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.exception.ValidacaoException;
import com.autenticacao.api.exception.ValidacaoNotFoundException;
import com.autenticacao.api.util.ValidatorUsuarioUtil;
import com.autenticacao.api.util.enums.EValidacao;
import com.autenticacao.api.util.enums.TipoMovimentacao;

import lombok.RequiredArgsConstructor;
/**
 * Serviço responsável pelo gerenciamento de usuários.
 * Implementa operações para criação, atualização, desativação e consulta de usuários.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final AutenticacaoService autenticacaoService;
  private final UsuarioMapper usuarioMapper;
  private final ValidatorUsuarioUtil validatorUsuarioUtil;
  private final HistoricoUsuarioService historicoUsuarioService;

  /**
   * Busca um usuário pelo seu ID.
   *
   * @param id Identificador único do usuário.
   * @return Dados detalhados do usuário.
   * @throws ValidacaoNotFoundException se usuário não for encontrado.
   */
  @Override
  public UsuarioDetalhadoResponse buscarPorId(UUID id) {
    return executarComandoComTratamentoErroComMensagem(
            () -> usuarioMapper.toDetalhado(obterUsuarioOuFalhar(id)),
            ERRO_AO_BUSCAR_USUARIO);
  }

  /**
   * Cria um novo usuário a partir dos dados fornecidos.
   * Valida a existência prévia e dados do usuário.
   *
   * @param request Dados para cadastro de novo usuário.
   * @return Resumo do usuário criado.
   * @throws ValidacaoException se email ou telefone já estiverem cadastrados.
   */
  @Override
  public UsuarioResumoResponse criarUsuario(CadastroUsuarioRequest request) {
    validarNovoUsuario(request);
    Usuario usuario = salvarUsuario(usuarioMapper.toEntity(request));
    autenticacaoService.criarAutenticacao(request, usuario);

    return usuarioMapper.toResumo(usuario);
  }

  /**
   * Atualiza dados parciais do usuário identificado pelo ID.
   *
   * @param id Identificador do usuário.
   * @param request Dados para atualização.
   * @return Dados detalhados do usuário atualizado.
   * @throws ValidacaoNotFoundException se usuário não existir.
   */
  @Override
  public UsuarioDetalhadoResponse atualizarUsuario(UUID id, AtualizarUsuarioRequest request) {
    return executarComandoComTratamentoErroComMensagem(
            () -> processarAtualizacaoUsuario(id, request), ERRO_AO_ATUALIZAR_DADOS_USUARIO);
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

    historicoUsuarioService.registrarAlteracaoUsuario(
            usuarioAtualizado, obterUsuarioLogado(), TipoMovimentacao.ATUALIZACAO_DADOS);

    Usuario salvo = salvarUsuario(usuarioAtualizado);

    return usuarioMapper.toDetalhado(salvo);
  }

  /**
   * Desativa o usuário identificado pelo ID, marcando-o como inativo e registrando histórico.
   *
   * @param id Identificador do usuário a desativar.
   * @throws ValidacaoNotFoundException se usuário não existir.
   */
  @Override
  public void desativarUsuario(UUID id) {
    executarComandoComTratamentoErroComMensagem(
            () -> {
              Usuario usuario = buscarUsuarioPorId(id);

              desativar(usuario);

              salvarUsuario(usuario);

              autenticacaoService.desativarAutenticacao(usuario.getId());

              registrarHistoricoDesativacao(usuario);

              return null;
            },
            ERRO_AO_DESATIVAR_USUARIO);
  }

  /**
   * Lista todos os usuários com dados resumidos.
   *
   * @return Lista com usuários resumidos.
   */
  @Override
  public List<UsuarioResumoResponse> listarTodos() {
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
   * Aplica atualizações parciais a uma entidade usuário usando padrão builder.
   *
   * @param request Dados para atualização.
   * @param usuario Entidade original a ser atualizada.
   * @return Novo objeto usuário atualizado.
   */
  private Usuario aplicarAtualizacoesParciais(AtualizarUsuarioRequest request, Usuario usuario) {
    Usuario.UsuarioBuilder builder = usuario.toBuilder();

    if (request.nome() != null && !request.nome().isBlank()) {
      builder.nome(request.nome());
    }
    if (request.sobrenome() != null && !request.sobrenome().isBlank()) {
      builder.sobrenome(request.sobrenome());
    }
    if (request.telefone() != null && !request.telefone().isBlank()) {
      builder.telefone(request.telefone());
    }
    if (request.email() != null && !request.email().isBlank()) {
      builder.email(request.email());
    }
    if (request.dataNascimento() != null) {
      builder.dataNascimento(request.dataNascimento());
    }

    return builder.build();
  }

  /**
   * Marca um usuário como inativo, definindo data e hora da exclusão.
   *
   * @param usuario Usuário a ser desativado.
   */
  private void desativar(Usuario usuario) {
    usuario.setAtivo(false);
    usuario.setDataHoraExclusao(LocalDateTime.now());
  }

  /**
   * Registra a desativação do usuário no histórico de alterações.
   *
   * @param usuario Usuário desativado.
   */
  private void registrarHistoricoDesativacao(Usuario usuario) {
    historicoUsuarioService.registrarAlteracaoUsuario(
            usuario, obterUsuarioLogado(), TipoMovimentacao.DESATIVACAO);
  }

  /**
   * Busca um usuário pelo ID ou lança exceção se não existir.
   *
   * @param id Identificador do usuário.
   * @return Usuário encontrado.
   * @throws ValidacaoNotFoundException se não encontrado.
   */
  private Usuario obterUsuarioOuFalhar(UUID id) {
    return usuarioRepository.findById(id)
            .orElseThrow(() -> new ValidacaoNotFoundException(EValidacao.USUARIO_NAO_ENCONTRADO_POR_ID, id.toString()));
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
            () -> usuarioRepository.save(usuario), ERRO_AO_TENTAR_CRIAR_USUARIO //
    );
  }

  /**
   * Valida dados para criação de novo usuário, verificando duplicidade de email e telefone.
   *
   * @param request Dados para cadastro.
   * @throws ValidacaoException se email ou telefone já cadastrados.
   */
  private void validarNovoUsuario(CadastroUsuarioRequest request) {
    this.validatorUsuarioUtil.validarFormatoEmailETelefone(request);

    if (usuarioRepository.findByEmail(request.email()).isPresent()) {
      throw new ValidacaoException(EValidacao.EMAIL_JA_CADASTRADO, request.email());
    }

    if (usuarioRepository.findByTelefone(request.telefone()).isPresent()) {
      throw new ValidacaoException(EValidacao.TELEFONE_JA_CADASTRADO, request.telefone());
    }
  }
}