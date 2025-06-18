package com.autenticacao.api.unitarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.HistoricoAutenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.AutenticacaoCloneMapper;
import com.autenticacao.api.app.repository.HistoricoAutenticacaoRepository;
import com.autenticacao.api.app.service.AutenticacaoComparadorService;
import com.autenticacao.api.app.service.impl.HistoricoAutenticacaoServiceImpl;

@ExtendWith(MockitoExtension.class)
class HistoricoAutenticacaoServiceImplTest {

  @InjectMocks private HistoricoAutenticacaoServiceImpl service;

  @Mock private HistoricoAutenticacaoRepository historicoRepository;

  @Mock private AutenticacaoCloneMapper cloneMapper;

  @Mock private AutenticacaoComparadorService comparadorService;

  private Autenticacao autenticacaoAntes;
  private Autenticacao autenticacaoDepois;
  private Usuario usuarioResponsavel;

  @BeforeEach
  void setup() {
    autenticacaoAntes = new Autenticacao();
    autenticacaoAntes.setAtivo(true);
    autenticacaoAntes.setHistoricoAutenticacoes(new java.util.ArrayList<>());

    autenticacaoDepois = new Autenticacao();
    autenticacaoDepois.setAtivo(true);
    autenticacaoDepois.setHistoricoAutenticacoes(new java.util.ArrayList<>());

    usuarioResponsavel = new Usuario();
    usuarioResponsavel.setId(java.util.UUID.randomUUID());
  }

  @Test
  @DisplayName("Deve registrar histórico de criação quando antes for null")
  void deveRegistrarHistoricoCriacao() {

    when(historicoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.registrarHistoricoCompleto(null, autenticacaoDepois, usuarioResponsavel);

    assertThat(autenticacaoDepois.getHistoricoAutenticacoes()).isNotEmpty();

    HistoricoAutenticacao historico = autenticacaoDepois.getHistoricoAutenticacoes().get(0);
    assertThat(historico.getTipoAlteracao().name()).isEqualTo("CRIACAO");
    assertThat(historico.getUsuarioResponsavel()).isEqualTo(usuarioResponsavel);
    assertThat(historico.getCamposAlterados()).isEqualTo("Autenticação criada");

    verify(historicoRepository).save(any(HistoricoAutenticacao.class));
  }

  @Test
  @DisplayName("Deve registrar histórico de desativação quando houve desativação")
  void deveRegistrarHistoricoDesativacao() {
    autenticacaoDepois.setAtivo(false);

    when(cloneMapper.copy(any())).thenReturn(autenticacaoAntes);
    when(comparadorService.extrairDiferencas(any(), any())).thenReturn("campoX alterado");
    when(historicoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.registrarHistoricoCompleto(autenticacaoAntes, autenticacaoDepois, usuarioResponsavel);

    assertThat(autenticacaoDepois.getHistoricoAutenticacoes()).isNotEmpty();

    HistoricoAutenticacao historico = autenticacaoDepois.getHistoricoAutenticacoes().get(0);
    assertThat(historico.getTipoAlteracao().name()).isEqualTo("DESATIVACAO");
    assertThat(historico.getCamposAlterados()).contains("campoX alterado");
    assertThat(historico.getCamposAlterados()).contains("Status alterado para desativado");

    verify(historicoRepository).save(any(HistoricoAutenticacao.class));
  }

  @Test
  @DisplayName("Deve registrar histórico de atualização quando houver alterações relevantes")
  void deveRegistrarHistoricoAtualizacao() {
    when(cloneMapper.copy(any())).thenReturn(autenticacaoAntes);
    when(comparadorService.extrairDiferencas(any(), any())).thenReturn("campoY alterado");
    when(historicoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.registrarHistoricoCompleto(autenticacaoAntes, autenticacaoDepois, usuarioResponsavel);

    assertThat(autenticacaoDepois.getHistoricoAutenticacoes()).isNotEmpty();

    HistoricoAutenticacao historico = autenticacaoDepois.getHistoricoAutenticacoes().get(0);
    assertThat(historico.getTipoAlteracao().name()).isEqualTo("ATUALIZACAO_DADOS");
    assertThat(historico.getCamposAlterados()).isEqualTo("campoY alterado");

    verify(historicoRepository).save(any(HistoricoAutenticacao.class));
  }

  @Test
  @DisplayName("Não deve registrar histórico quando só campo ativo foi alterado")
  void naoDeveRegistrarQuandoApenasAtivoAlterado() {
    when(cloneMapper.copy(any())).thenReturn(autenticacaoAntes);
    when(comparadorService.extrairDiferencas(any(), any())).thenReturn("ativo");

    service.registrarHistoricoCompleto(autenticacaoAntes, autenticacaoDepois, usuarioResponsavel);

    assertThat(autenticacaoDepois.getHistoricoAutenticacoes()).isEmpty();

    verify(historicoRepository, never()).save(any());
  }
}
