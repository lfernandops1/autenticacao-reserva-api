package com.autenticacao.api.app.service.impl;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.AutenticacaoCloneMapper;
import com.autenticacao.api.app.repository.HistoricoAutenticacaoRepository;
import com.autenticacao.api.app.service.AutenticacaoComparadorService;
import com.autenticacao.api.app.service.HistoricoAutenticacaoService;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.autenticacao.api.app.util.ExecutarUtil.executarComandoComTratamentoErroComMensagem;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO;

@Service
public class HistoricoAutenticacaoServiceImpl implements HistoricoAutenticacaoService {

    private final HistoricoAutenticacaoRepository historicoRepository;
    private final AutenticacaoCloneMapper cloneMapper;
    private final AutenticacaoComparadorService comparadorService;

    public HistoricoAutenticacaoServiceImpl(
            HistoricoAutenticacaoRepository historicoRepository,
            @Qualifier("autenticacaoCloneMapperImpl") AutenticacaoCloneMapper cloneMapper,
            AutenticacaoComparadorService comparadorService) {
        this.historicoRepository = historicoRepository;
        this.cloneMapper = cloneMapper;
        this.comparadorService = comparadorService;
    }

    @Override
    public void registrarHistoricoCompleto(
            Autenticacao antes, Autenticacao depois, Usuario responsavel) {
        executarComandoComTratamentoErroComMensagem(
                () -> {
                    if (antes == null) {
                        registrarCriacao(depois, responsavel);
                    } else if (houveDesativacao(antes, depois)) {
                        registrarDesativacao(antes, depois, responsavel);
                    } else {
                        registrarAtualizacao(antes, depois, responsavel);
                    }
                    return null;
                },
                ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO.getChave());
    }

    private void registrarCriacao(Autenticacao autenticacao, Usuario responsavel) {
        salvarHistorico(
                autenticacao,
                responsavel,
                TipoMovimentacao.CRIACAO,
                "Autenticação criada",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null);
    }

    private void registrarDesativacao(Autenticacao antes, Autenticacao depois, Usuario responsavel) {
        String campos = comparar(antes, depois);
        String camposFinal =
                campos.isBlank()
                        ? "Status alterado para desativado"
                        : campos + "; Status alterado para desativado";

        salvarHistorico(
                depois,
                responsavel,
                TipoMovimentacao.DESATIVACAO,
                camposFinal,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private void registrarAtualizacao(Autenticacao antes, Autenticacao depois, Usuario responsavel) {
        String campos = comparar(antes, depois);
        if (!campos.isBlank() && !somenteCampoAtivoAlterado(campos)) {
            salvarHistorico(
                    depois,
                    responsavel,
                    TipoMovimentacao.ATUALIZACAO_DADOS,
                    campos,
                    null,
                    LocalDateTime.now(),
                    null);
        }
    }

    private String comparar(Autenticacao antes, Autenticacao depois) {
        Autenticacao cloneAntes = cloneMapper.copy(antes);
        String campos = comparadorService.extrairDiferencas(cloneAntes, depois);
        System.out.println("Diferenças encontradas: " + campos); // para debug
        return campos == null ? "" : campos.trim();
    }

    private void salvarHistorico(
            Autenticacao autenticacao,
            Usuario responsavel,
            TipoMovimentacao tipo,
            String campos,
            LocalDateTime dataCriacao,
            LocalDateTime dataAlteracao,
            LocalDateTime dataExclusao) {
        HistoricoAutenticacao historico =
                HistoricoAutenticacao.builder()
                        .autenticacao(autenticacao)
                        .usuarioResponsavel(responsavel)
                        .tipoAlteracao(tipo)
                        .camposAlterados(campos)
                        .dataHoraCriacao(dataCriacao)
                        .dataAlteracao(dataAlteracao)
                        .dataHoraExclusao(dataExclusao)
                        .build();

        autenticacao.getHistoricoAutenticacoes().add(historico);
        historicoRepository.save(historico);
    }

    private boolean houveDesativacao(Autenticacao antes, Autenticacao depois) {
        return antes.getAtivo() && !depois.getAtivo();
    }

    private boolean somenteCampoAtivoAlterado(String camposAlterados) {
        if (camposAlterados.isEmpty()) return false;
        String[] partes = camposAlterados.split(",");
        if (partes.length > 1) return false;
        return partes[0].toLowerCase().contains("ativo");
    }
}
