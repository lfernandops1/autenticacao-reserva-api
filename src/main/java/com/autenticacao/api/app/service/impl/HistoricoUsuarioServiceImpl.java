package com.autenticacao.api.app.service.impl;

import com.autenticacao.api.app.domain.entity.HistoricoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioCloneMapper;
import com.autenticacao.api.app.repository.HistoricoUsuarioRepository;
import com.autenticacao.api.app.service.HistoricoUsuarioService;
import com.autenticacao.api.app.service.UsuarioComparadorService;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider.obterUsuarioLogado;
import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO;

/**
 * Implementação do serviço para registrar o histórico completo de usuário, contemplando criação,
 * atualização e desativação.
 */
@Service
public class HistoricoUsuarioServiceImpl implements HistoricoUsuarioService {

    private final HistoricoUsuarioRepository historicoRepository;
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
     * Registra o histórico completo de criação, alteração e/ou desativação de um usuário.
     *
     * @param antes  estado anterior do usuário (null se criação)
     * @param depois estado atual do usuário
     */
    @Override
    public void registrarHistoricoCompleto(Usuario antes, Usuario depois) {
        executarComandoComTratamentoErroComMensagem(
                () -> registrarHistorico(antes, depois),
                ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO.getChave());
    }

    private Void registrarHistorico(Usuario antes, Usuario depois) {
        LocalDateTime agora = LocalDateTime.now();

        if (antes == null) {
            registrarCriacao(depois, agora);
            return null;
        }

        Usuario cloneAntes = cloneMapper.copy(antes);
        String camposAlterados = usuarioComparadorService.extrairDiferencas(cloneAntes, depois);
        if (camposAlterados == null) {
            camposAlterados = "";
        }

        boolean desativado = houveDesativacao(antes, depois);

        if (desativado) {
            registrarDesativacao(depois, obterUsuarioLogado(), agora, camposAlterados);
            return null;
        }

        if (!camposAlterados.isBlank() && !apenasCampoAtivoAlterado(camposAlterados)) {
            registrarAlteracao(depois, agora, camposAlterados);
        }

        return null;
    }

    private void registrarCriacao(Usuario usuario, LocalDateTime agora) {
        HistoricoUsuario historicoCriacao =
                HistoricoUsuario.builder()
                        .usuario(usuario)
                        .usuarioResponsavel(usuario)
                        .tipoAlteracao(TipoMovimentacao.CRIACAO)
                        .dataHoraCriacao(agora)
                        .dataHoraAtualizacao(agora)
                        .camposAlterados("Usuário criado")
                        .build();
        historicoRepository.save(historicoCriacao);
    }

    private void registrarDesativacao(
            Usuario usuario, Usuario responsavel, LocalDateTime agora, String camposAlterados) {
        String mensagem = "Status alterado para desativado";
        if (!camposAlterados.isBlank() && !apenasCampoAtivoAlterado(camposAlterados)) {
            mensagem = camposAlterados + "; " + mensagem;
        }

        HistoricoUsuario historicoDesativacao =
                HistoricoUsuario.builder()
                        .usuario(usuario)
                        .usuarioResponsavel(responsavel)
                        .tipoAlteracao(TipoMovimentacao.DESATIVACAO)
                        .dataHoraAtualizacao(agora)
                        .dataHoraExclusao(agora)
                        .camposAlterados(mensagem)
                        .build();
        historicoRepository.save(historicoDesativacao);
    }

    private void registrarAlteracao(
            Usuario usuario, LocalDateTime agora, String camposAlterados) {
        HistoricoUsuario historicoAlteracao =
                HistoricoUsuario.builder()
                        .usuario(usuario)
                        .usuarioResponsavel(obterUsuarioLogado())
                        .tipoAlteracao(TipoMovimentacao.ATUALIZACAO_DADOS)
                        .dataHoraAtualizacao(agora)
                        .camposAlterados(camposAlterados)
                        .build();
        historicoRepository.save(historicoAlteracao);
    }

    private boolean houveDesativacao(Usuario antes, Usuario depois) {
        return antes.isAtivo() && !depois.isAtivo();
    }

    private boolean apenasCampoAtivoAlterado(String camposAlterados) {
        if (camposAlterados.isEmpty()) return false;

        String[] partes = camposAlterados.split(",");
        if (partes.length > 1) return false;

        String campo = partes[0].toLowerCase().trim();
        return campo.contains("ativo");
    }
}
