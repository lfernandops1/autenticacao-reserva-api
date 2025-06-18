package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.config.security.provider.UsuarioAutenticadoProvider;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.app.exception.UsuarioNaoAutenticadoException;
import com.autenticacao.api.app.exception.UsuarioNaoEncontradoException;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.SenhaService;
import com.autenticacao.api.app.service.impl.AutenticacaoServiceImpl;
import com.autenticacao.api.app.service.impl.RefreshTokenServiceImpl;
import com.autenticacao.api.app.service.impl.TentativaLoginServiceImpl;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceImplTest {

  @InjectMocks private AutenticacaoServiceImpl autenticacaoService;

  @Mock private UsuarioRepository usuarioRepository;
  @Mock private TokenService tokenService;
  @Mock private RefreshTokenServiceImpl refreshTokenService;
  @Mock private SenhaService senhaService;
  @Mock private TentativaLoginServiceImpl tentativaLoginService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private UsuarioAutenticadoProvider usuarioAutenticadoProvider;

  private static final UUID USUARIO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final String EMAIL = "usuario@email.com";
  private static final String SENHA = "senha123";

  private Usuario usuario;
  private LoginUsuarioRequestDTO loginRequest;

  @BeforeEach
  void setup() {
    usuario = Usuario.builder().id(USUARIO_ID).email(EMAIL).build();

    loginRequest = new LoginUsuarioRequestDTO(EMAIL, SENHA);
  }

  // ====== LOGIN - CASOS DE SUCESSO E ERRO ======

  @Test
  @DisplayName("Deve realizar login com sucesso e retornar tokens válidos")
  void login_DeveRetornarTokensQuandoCredenciaisValidas() {
    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    doNothing().when(tentativaLoginService).validarBloqueio(usuario);

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(usuario);
    when(authenticationManager.authenticate(any())).thenReturn(authentication);

    doNothing().when(tentativaLoginService).resetarTentativas(usuario);
    doNothing().when(senhaService).validarSenhaExpirada(usuario);

    when(tokenService.generateToken(usuario)).thenReturn("token-de-acesso");
    when(refreshTokenService.createRefreshToken(usuario)).thenReturn("refresh-token");

    LoginResponseDTO response = autenticacaoService.login(loginRequest);

    assertThat(response.token()).isEqualTo("token-de-acesso");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");

    verify(tentativaLoginService).validarBloqueio(usuario);
    verify(authenticationManager).authenticate(any());
    verify(tentativaLoginService).resetarTentativas(usuario);
    verify(senhaService).validarSenhaExpirada(usuario);
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar login com usuário não cadastrado")
  void loginDeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    AutenticacaoApiRunTimeException exception =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.login(loginRequest));

    assertThat(exception.getMessage()).contains(ERRO_REALIZAR_LOGIN.getChave());

    verify(tentativaLoginService, never()).validarBloqueio(any());
    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  @DisplayName("Deve registrar falha e lançar exceção customizada quando credenciais inválidas")
  void loginDeveRegistrarFalhaEAguardarExcecaoParaCredenciaisInvalidas() {
    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    doNothing().when(tentativaLoginService).validarBloqueio(any(Usuario.class));

    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.login(loginRequest));

    assertThat(ex.getMessage()).contains(ERRO_REALIZAR_LOGIN.getChave());

    verify(tentativaLoginService).registrarFalha(argThat(u -> u.getEmail().equals(EMAIL)));
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário estiver bloqueado no login")
  void loginDeveLancarExcecaoQuandoUsuarioBloqueado() {
    when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
    doThrow(new RuntimeException("Usuário bloqueado"))
        .when(tentativaLoginService)
        .validarBloqueio(any());

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.login(loginRequest));

    assertThat(ex.getMessage()).contains(ERRO_REALIZAR_LOGIN.getChave());

    verify(authenticationManager, never()).authenticate(any());
  }

  // ====== ALTERAR SENHA - CASOS DE SUCESSO E ERRO ======

  @Test
  @DisplayName("Deve alterar senha com sucesso para usuário autenticado")
  void alterarSenhaDeveAlterarSenhaQuandoUsuarioAutenticado() {
    AlterarSenhaRequest dto = new AlterarSenhaRequest("novaSenha123", "novAsenha123");

    when(usuarioAutenticadoProvider.getIdUsuarioLogado()).thenReturn(Optional.of(USUARIO_ID));
    when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
    doNothing().when(senhaService).alterarSenha(dto.senhaAtual(), dto.novaSenha());

    autenticacaoService.alterarSenha(dto);

    verify(senhaService).alterarSenha(dto.senhaAtual(), dto.novaSenha());
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar alterar senha sem usuário autenticado")
  void alterarSenha_DeveLancarExcecaoQuandoUsuarioNaoAutenticado() {
    when(usuarioAutenticadoProvider.getIdUsuarioLogado()).thenReturn(Optional.empty());

    AlterarSenhaRequest dto = new AlterarSenhaRequest("novaSenha123", "novAsenha123");

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.alterarSenha(dto));

    assertThat(ex.getMessage()).contains(ERRO_ALTERAR_SENHA.getChave());
    assertThat(ex.getCause()).isInstanceOf(UsuarioNaoAutenticadoException.class);
    assertThat(ex.getCause().getMessage()).isEqualTo(USUARIO_NAO_AUTENTICADO.getChave());

    verify(usuarioRepository, never()).findById(any());
    verify(senhaService, never()).alterarSenha(any(), any());
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário não for encontrado ao alterar senha")
  void alterarSenhaDeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    when(usuarioAutenticadoProvider.getIdUsuarioLogado()).thenReturn(Optional.of(USUARIO_ID));
    when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

    AlterarSenhaRequest dto = new AlterarSenhaRequest("novaSenha123", "novAsenha123");

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.alterarSenha(dto));

    assertThat(ex.getMessage()).contains(ERRO_ALTERAR_SENHA.getChave());
    assertThat(ex.getCause()).isInstanceOf(UsuarioNaoEncontradoException.class);
    assertThat(ex.getCause().getMessage()).isEqualTo(USUARIO_NAO_ENCONTRADO.getChave());

    verify(senhaService, never()).alterarSenha(any(), any());
  }

  @Test
  @DisplayName("Deve lançar exceção customizada ao ocorrer erro inesperado na alteração de senha")
  void alterarSenhaDeveLancarAutenticacaoApiRunTimeExceptionEmErroInesperado() {
    when(usuarioAutenticadoProvider.getIdUsuarioLogado()).thenReturn(Optional.of(USUARIO_ID));
    when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
    doThrow(new RuntimeException("Erro inesperado"))
        .when(senhaService)
        .alterarSenha("senhaAtual", "novaSenha123");

    AlterarSenhaRequest dto = new AlterarSenhaRequest("novaSenha123", "novAsenha123");

    AutenticacaoApiRunTimeException ex =
        assertThrows(
            AutenticacaoApiRunTimeException.class, () -> autenticacaoService.alterarSenha(dto));

    assertThat(ex.getMessage()).contains(ERRO_ALTERAR_SENHA.getChave());
    assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
  }
}
