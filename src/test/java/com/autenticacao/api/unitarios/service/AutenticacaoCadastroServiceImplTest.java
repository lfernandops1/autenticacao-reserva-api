package com.autenticacao.api.unitarios.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.app.exception.AutenticacaoJaExistenteException;
import com.autenticacao.api.app.exception.UsuarioNaoEncontradoException;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.service.HistoricoAutenticacaoService;
import com.autenticacao.api.app.service.impl.AutenticacaoCadastroServiceImpl;
import com.autenticacao.api.app.util.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class AutenticacaoCadastroServiceImplTest {

  @InjectMocks private AutenticacaoCadastroServiceImpl service;

  @Mock private AutenticacaoRepository autenticacaoRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private HistoricoAutenticacaoService historicoAutenticacaoService;
  private Usuario usuario;
  private CadastroUsuarioRequest cadastroRequest;
  private Autenticacao autenticacaoExistente;
  private UUID usuarioId;

  @BeforeEach
  void setup() {
    usuarioId = UUID.randomUUID();
    usuario = Usuario.builder().id(usuarioId).email("usuario@email.com").build();

    cadastroRequest =
        new CadastroUsuarioRequest(
            "Nome",
            "Sobrenome",
            "usuario@email.com",
            "senha123",
            "999999999",
            LocalDate.of(1990, 1, 1),
            true,
            UserRole.USER);

    autenticacaoExistente = new Autenticacao();
    autenticacaoExistente.setUsuario(usuario);
    autenticacaoExistente.setEmail("usuario@email.com");
    autenticacaoExistente.setSenha("hash");
    autenticacaoExistente.setAtivo(true);
  }

  // ======= TESTES criar() =======
  @Test
  @DisplayName("Deve criar autenticação com sucesso quando usuário não possui autenticação")
  void deveCriarAutenticacaoComSucesso() {
    when(autenticacaoRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(cadastroRequest.senha())).thenReturn("senhaCriptografada");

    service.criar(cadastroRequest, usuario);

    verify(autenticacaoRepository)
        .save(
            argThat(
                auth ->
                    auth.getUsuario().equals(usuario)
                        && auth.getEmail().equals(usuario.getEmail())
                        && auth.getSenha().equals("senhaCriptografada")
                        && auth.getAtivo()));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar criar autenticação quando usuário já possui")
  void deveLancarExcecaoQuandoUsuarioJaPossuiAutenticacao() {
    when(autenticacaoRepository.findByUsuario(usuario))
        .thenReturn(Optional.of(autenticacaoExistente));

    AutenticacaoApiRunTimeException ex =
        Assertions.assertThrows(
            AutenticacaoApiRunTimeException.class, () -> service.criar(cadastroRequest, usuario));

    Assertions.assertNotNull(ex.getCause());
    Assertions.assertInstanceOf(AutenticacaoJaExistenteException.class, ex.getCause());
    Assertions.assertEquals("usuario.ja.possui.autenticacao", ex.getCause().getMessage());
  }

  // ======= TESTES desativar() =======
  @Test
  @DisplayName("Deve desativar autenticação com sucesso quando encontrada por usuário ID")
  void deveDesativarAutenticacaoComSucesso() {
    when(autenticacaoRepository.buscarPorUsuarioId(usuarioId))
        .thenReturn(Optional.of(autenticacaoExistente));

    service.desativar(usuarioId);

    ArgumentCaptor<Autenticacao> captor = ArgumentCaptor.forClass(Autenticacao.class);
    verify(autenticacaoRepository).save(captor.capture());

    Autenticacao salvo = captor.getValue();

    assertThat(salvo.getAtivo()).isFalse();
  }

  @Test
  @DisplayName(
      "Deve lançar exceção ao tentar desativar autenticação quando não existe para usuário")
  void deveLancarExcecaoQuandoUsuarioNaoPossuiAutenticacao() {
    when(autenticacaoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

    AutenticacaoApiRunTimeException ex =
        Assertions.assertThrows(
            AutenticacaoApiRunTimeException.class, () -> service.desativar(usuarioId));

    Assertions.assertNotNull(ex.getCause());
    Assertions.assertTrue(ex.getCause() instanceof UsuarioNaoEncontradoException);
    Assertions.assertEquals("Usuário não encontrado", ex.getCause().getMessage());
  }
}
