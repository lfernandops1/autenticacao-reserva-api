package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import com.autenticacao.api.app.domain.entity.HistoricoUsuario;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.domain.mapper.UsuarioCloneMapper;
import com.autenticacao.api.app.repository.HistoricoUsuarioRepository;
import com.autenticacao.api.app.service.UsuarioComparadorService;
import com.autenticacao.api.app.service.impl.HistoricoUsuarioServiceImpl;
import com.autenticacao.api.app.util.enums.TipoMovimentacao;
import com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider;

@ExtendWith(MockitoExtension.class)
class HistoricoUsuarioServiceImplTest {

  @InjectMocks
  private HistoricoUsuarioServiceImpl historicoService;

  @Mock
  private HistoricoUsuarioRepository historicoRepository;

  @Mock
  @Qualifier("usuarioCloneMapperImpl")
  private UsuarioCloneMapper cloneMapper;

  @Mock
  private UsuarioComparadorService usuarioComparadorService;

  private Usuario usuarioAntes;
  private Usuario usuarioResponsavel;
  private Usuario cloneUsuario;
  private final String camposDiferenca = "nome, email";

  @BeforeEach
  void setup() {
    usuarioAntes =
            Usuario.builder()
                    .id(UUID.randomUUID())
                    .nome("João")
                    .sobrenome("Silva")
                    .email("joao@email.com")
                    .telefone("123456789")
                    .dataNascimento(LocalDate.of(1990, 1, 1))
                    .ativo(true)
                    .build();

    usuarioResponsavel =
            Usuario.builder()
                    .id(UUID.randomUUID())
                    .nome("Admin")
                    .email("admin@email.com")
                    .build();

    cloneUsuario = usuarioAntes.toBuilder().build();
  }

  @Test
  @DisplayName("Deve registrar histórico de alteração do usuário com sucesso")
  void deveRegistrarHistoricoAlteracaoUsuarioComSucesso() {
    when(cloneMapper.copy(usuarioAntes)).thenReturn(cloneUsuario);
    when(usuarioComparadorService.extrairDiferencas(cloneUsuario, cloneUsuario))
            .thenReturn(camposDiferenca);

    try (MockedStatic<UsuarioAutenticadoProvider> mockedStatic =
                 mockStatic(UsuarioAutenticadoProvider.class)) {
      mockedStatic.when(UsuarioAutenticadoProvider::obterUsuarioLogado).thenReturn(usuarioResponsavel);

      historicoService.registrarHistoricoCompleto(usuarioAntes, cloneUsuario);

      ArgumentCaptor<HistoricoUsuario> captor = ArgumentCaptor.forClass(HistoricoUsuario.class);
      verify(historicoRepository).save(captor.capture());

      HistoricoUsuario salvo = captor.getValue();
      assertThat(salvo.getUsuario()).isEqualTo(cloneUsuario);
      assertThat(salvo.getUsuarioResponsavel()).isEqualTo(usuarioResponsavel);
      assertThat(salvo.getTipoAlteracao()).isEqualTo(TipoMovimentacao.ATUALIZACAO_DADOS);
      assertThat(salvo.getCamposAlterados()).isEqualTo(camposDiferenca);
      assertThat(salvo.getDataHoraAtualizacao()).isNotNull();
    }
  }

  @Test
  @DisplayName("Deve lançar exceção ao falhar ao salvar histórico de alteração")
  void deveLancarExcecaoQuandoFalharAoRegistrarHistorico() {
    when(cloneMapper.copy(usuarioAntes)).thenReturn(cloneUsuario);
    when(usuarioComparadorService.extrairDiferencas(cloneUsuario, cloneUsuario))
            .thenReturn(camposDiferenca);

    doThrow(new RuntimeException("Falha interna"))
            .when(historicoRepository)
            .save(any(HistoricoUsuario.class));

    try (MockedStatic<UsuarioAutenticadoProvider> mockedStatic =
                 mockStatic(UsuarioAutenticadoProvider.class)) {
      mockedStatic.when(UsuarioAutenticadoProvider::obterUsuarioLogado).thenReturn(usuarioResponsavel);

      RuntimeException ex =
              assertThrows(RuntimeException.class, () -> historicoService.registrarHistoricoCompleto(usuarioAntes, cloneUsuario));

      assertThat(ex.getMessage()).contains(ERRO_REGISTRAR_HISTORICO_ALTERACAO_USUARIO.getChave());
    }
  }
}
