package com.autenticacao.api.app.service.impl;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.HistoricoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioCloneMapper;
import com.autenticacao.api.app.repository.HistoricoUsuarioRepository;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.service.UsuarioComparadorService;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;

/**
 * Implementação do serviço para registrar histórico de alterações de usuários. Aplica o princípio
 * da responsabilidade única, delegando clonagem e comparação a componentes externos.
 */
@Service
public class HistoricoUsuarioServiceImpl implements HistoricoUsuarioService {

  private final HistoricoUsuarioRepository historicoRepository;

  @Qualifier("usuarioCloneMapperImpl")
  private final UsuarioCloneMapper cloneMapper;

  private final UsuarioComparadorService usuarioComparadorService;

  public HistoricoUsuarioServiceImpl(
      HistoricoUsuarioRepository historicoRepository,
      @Qualifier("usuarioCloneMapperImpl") UsuarioCloneMapper cloneMapper,
      UsuarioComparadorService usuarioComparadorService) {
    this.historicoRepository = historicoRepository;
    this.cloneMapper = cloneMapper;
    this.usuarioComparadorService = usuarioComparadorService;
  }

  /**
   * Salva no histórico as alterações feitas num usuário, incluindo quem realizou e tipo da
   * movimentação.
   *
   * @param usuarioAntesModificacao usuário original antes da modificação
   * @param responsavel usuário que realizou a modificação
   * @param tipo tipo de movimentação realizada (ex: ALTERACAO, DESATIVACAO)
   */
  @Override
  public void registrarAlteracaoUsuario(
      Usuario usuarioAntesModificacao, Usuario responsavel, TipoMovimentacao tipo) {
    executarComandoComTratamentoErroComMensagem(
        () -> {
          Usuario original = cloneMapper.copy(usuarioAntesModificacao);
          String camposAlterados =
              usuarioComparadorService.extrairDiferencas(original, usuarioAntesModificacao);

          HistoricoUsuario historico =
              HistoricoUsuario.builder()
                  .usuario(usuarioAntesModificacao)
                  .usuarioResponsavel(responsavel)
                  .tipoAlteracao(tipo)
                  .dataAlteracao(LocalDateTime.now())
                  .camposAlterados(camposAlterados)
                  .build();

          historicoRepository.save(historico);
          return null;
        },
        ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO.getChave());
  }
}
