package com.autenticacao.api.unitarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.repository.AutenticacaoRepository;
import com.autenticacao.api.app.repository.UsuarioRepository;
import com.autenticacao.api.app.service.impl.AutenticacaoServiceImpl;
import com.autenticacao.api.app.service.impl.RefreshTokenServiceImpl;
import com.autenticacao.api.app.service.impl.TentativaLoginServiceImpl;
import com.autenticacao.api.exception.SenhaExpiradaException;
import com.autenticacao.api.util.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceImplTest {

  @InjectMocks private AutenticacaoServiceImpl autenticacaoService;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private AutenticacaoRepository autenticacaoRepository;

  @Mock private TokenService tokenService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private RefreshTokenServiceImpl refreshTokenService;

  @Mock private TentativaLoginServiceImpl tentativaLoginService;

  @Mock private AuthenticationManager authenticationManager;

  private final UUID usuarioId = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private final String email = "usuario@teste.com";
  private final String senha = "Senha@123";
  private final Usuario usuario = obterUsuario();
  private final Autenticacao autenticacao = obterAutenticacao();
  private final LoginUsuarioRequestDTO loginRequest = obterLoginRequest();
  private final CadastroUsuarioRequest cadastroRequest = obterCadastroRequest();
  private final AlterarSenhaRequestDTO alterarSenhaRequest = obterAlterarSenhaRequest();

  private Usuario usuarioMock;

  @BeforeEach
  void setup() {
    usuarioMock = new Usuario();
    Autenticacao autenticacaoMock = new Autenticacao();
    usuarioMock.setAutenticacao(autenticacaoMock);

    Authentication authentication = Mockito.mock(Authentication.class);
    Mockito.when(authentication.getPrincipal()).thenReturn(usuarioMock);

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);
  }

  // ======= loadUserByUsername =======
  @Test
  void loadUserByUsernameDeveRetornarUsuarioQuandoEmailExiste() {
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

    UserDetails userDetails = autenticacaoService.loadUserByUsername(email);

    assertThat(userDetails).isEqualTo(usuario);
    verify(usuarioRepository).findByEmail(email);
  }

  @Test
  void loadUserByUsernameDeveLancarExcecaoQuandoEmailNaoExiste() {
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class, () -> autenticacaoService.loadUserByUsername(email));
  }

  // ======= login =======
  @Test
  void loginDeveRetornarTokenQuandoCredenciaisValidas() {
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any()))
        .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null));
    when(tokenService.generateToken(usuario)).thenReturn("token");
    when(refreshTokenService.createRefreshToken(usuario)).thenReturn("refreshToken");

    LoginResponseDTO response = autenticacaoService.login(loginRequest, authenticationManager);

    assertThat(response.token()).isEqualTo("token");
    assertThat(response.refreshToken()).isEqualTo("refreshToken");
    verify(tentativaLoginService).resetarTentativas(usuario);
  }

  @Test
  void loginDeveLancarExcecaoQuandoCredenciaisInvalidas() {
    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Credenciais inválidas"));

    assertThrows(
        BadCredentialsException.class,
        () -> autenticacaoService.login(loginRequest, authenticationManager));

    verify(tentativaLoginService).registrarFalha(usuario);
  }

  @Test
  void loginDeveLancarExcecaoQuandoSenhaExpirada() {
    Autenticacao autenticacaoExpirada = obterAutenticacao();
    autenticacaoExpirada.setDataHoraAtualizacao(LocalDateTime.now().minusDays(91));
    usuario.setAutenticacao(autenticacaoExpirada);

    when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
    when(authenticationManager.authenticate(any()))
        .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null));

    assertThrows(
        SenhaExpiradaException.class,
        () -> autenticacaoService.login(loginRequest, authenticationManager));
  }

  // ======= criarAutenticacao =======
  @Test
  void criarAutenticacaoDeveSalvarQuandoNaoExistir() {
    when(autenticacaoRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(any())).thenReturn("senhaCriptografada");

    autenticacaoService.criarAutenticacao(cadastroRequest, usuario);

    verify(autenticacaoRepository).save(any(Autenticacao.class));
  }

  @Test
  void criarAutenticacaoDeveLancarExcecaoQuandoJaExistir() {
    when(autenticacaoRepository.findByUsuario(usuario)).thenReturn(Optional.of(autenticacao));

    assertThrows(
        IllegalStateException.class,
        () -> autenticacaoService.criarAutenticacao(cadastroRequest, usuario));
  }

  // ======= alterarSenha =======
  @Test
  void alterarSenhaDeveAtualizarSenhaComSucesso() {

    when(usuarioRepository.findById(usuarioMock.getId())).thenReturn(Optional.of(usuarioMock));
    when(passwordEncoder.encode(any())).thenReturn("novaSenhaCriptografada");

    AlterarSenhaRequestDTO requestDTO = new AlterarSenhaRequestDTO("senhaNova");

    autenticacaoService.alterarSenha(requestDTO);

    assertThat(usuarioMock.getAutenticacao().getSenha()).isEqualTo("novaSenhaCriptografada");
    assertThat(usuarioMock.getAutenticacao().getDataHoraAtualizacao()).isNotNull();
    verify(autenticacaoRepository).save(usuarioMock.getAutenticacao());
  }

  @Test
  void alterarSenhaDeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
    when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> autenticacaoService.alterarSenha(alterarSenhaRequest));
  }

  // ======= desativarAutenticacao =======
  @Test
  void desativarAutenticacaoDeveDesativarComSucesso() {
    when(autenticacaoRepository.buscarPorUsuarioId(usuarioId))
        .thenReturn(Optional.of(autenticacao));

    autenticacaoService.desativarAutenticacao(usuarioId);

    assertThat(autenticacao.getAtivo()).isFalse();
    assertThat(autenticacao.getDataHoraExclusao()).isNotNull();
    verify(autenticacaoRepository).save(autenticacao);
  }

  @Test
  void desativarAutenticacaoDeveLancarExcecaoQuandoNaoEncontrado() {
    when(autenticacaoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class, () -> autenticacaoService.desativarAutenticacao(usuarioId));
  }

  // ======= senhaExpirada =======
  @Test
  void senhaExpiradaDeveRetornarTrueQuandoExpirada() {
    Autenticacao autenticacaoExpirada = obterAutenticacao();
    autenticacaoExpirada.setDataHoraAtualizacao(LocalDateTime.now().minusDays(91));

    boolean resultado = autenticacaoService.senhaExpirada(autenticacaoExpirada);

    assertThat(resultado).isTrue();
  }

  @Test
  void senhaExpiradaDeveRetornarFalseQuandoNaoExpirada() {
    Autenticacao autenticacaoValida = obterAutenticacao();
    autenticacaoValida.setDataHoraAtualizacao(LocalDateTime.now().minusDays(89));

    boolean resultado = autenticacaoService.senhaExpirada(autenticacaoValida);

    assertThat(resultado).isFalse();
  }

  // ===================== MÉTODOS AUXILIARES =====================

  private Usuario obterUsuario() {
    Usuario usuario = new Usuario();
    usuario.setId(usuarioId);
    usuario.setEmail(email);
    usuario.setAutenticacao(obterAutenticacao());
    return usuario;
  }

  private Autenticacao obterAutenticacao() {
    Autenticacao autenticacao = new Autenticacao();
    autenticacao.setId(UUID.randomUUID());
    autenticacao.setSenha("senhaCriptografada");
    autenticacao.setEmail(email);
    autenticacao.setUsuario(usuario);
    autenticacao.setDataHoraAtualizacao(LocalDateTime.now());
    autenticacao.setDataHoraCriacao(LocalDateTime.now());
    autenticacao.setAtivo(true);
    return autenticacao;
  }

  private LoginUsuarioRequestDTO obterLoginRequest() {
    return new LoginUsuarioRequestDTO(email, senha);
  }

  private CadastroUsuarioRequest obterCadastroRequest() {
    return new CadastroUsuarioRequest(
        "Nome",
        "Sobrenome",
        email,
        senha,
        "999999999",
        LocalDate.of(1990, 1, 1),
        true,
        UserRole.USER,
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  private AlterarSenhaRequestDTO obterAlterarSenhaRequest() {
    return new AlterarSenhaRequestDTO("NovaSenha@123");
  }
}
