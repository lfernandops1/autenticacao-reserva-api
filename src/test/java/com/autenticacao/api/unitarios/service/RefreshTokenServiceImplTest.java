package com.autenticacao.api.unitarios.service;

import static com.autenticacao.api.app.util.enums.MensagemSistema.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.autenticacao.api.app.config.security.provider.TokenGenerator;
import com.autenticacao.api.app.domain.entity.RefreshToken;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.RefreshTokenInvalidoException;
import com.autenticacao.api.app.repository.RefreshTokenRepository;
import com.autenticacao.api.app.service.impl.RefreshTokenServiceImpl;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  @InjectMocks private RefreshTokenServiceImpl service;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private TokenGenerator tokenGenerator;

  @Mock private Usuario usuario;

  @BeforeEach
  void setUp() {
    // Configura validade do token para 7 dias
    ReflectionTestUtils.setField(service, "validadeTokenMinutos", 10080L);
  }

  // ======= CRIAÇÃO DE REFRESH TOKEN =======
  @Test
  @DisplayName("Deve criar refresh token com sucesso")
  void deveCriarRefreshTokenComSucesso() {
    String tokenGerado = "token123";

    when(tokenGenerator.gerarToken()).thenReturn(tokenGerado);
    when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    String resultado = service.createRefreshToken(usuario);

    assertEquals(tokenGerado, resultado);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar refresh token devido a erro interno")
  void deveLancarExcecaoAoCriarRefreshTokenErroInterno() {
    when(tokenGenerator.gerarToken()).thenThrow(new RuntimeException("Erro interno"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.createRefreshToken(usuario));

    assertEquals(ERRO_CRIAR_REFRESH_TOKEN.getChave(), exception.getMessage());
  }

  // ======= VALIDAÇÃO DE TOKEN =======
  @Test
  @DisplayName("Deve validar token válido com sucesso")
  void deveValidarTokenValidoComSucesso() {
    String token = "abc123";
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

    when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

    assertTrue(service.isValid(token));
  }

  @Test
  @DisplayName("Deve retornar falso para token expirado")
  void deveRetornarFalseParaTokenExpirado() {
    String token = "abc123";
    RefreshToken tokenExpirado = new RefreshToken();
    tokenExpirado.setToken(token);
    tokenExpirado.setExpiryDate(LocalDateTime.now().minusMinutes(1));

    when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(tokenExpirado));

    assertFalse(service.isValid(token));
  }

  @Test
  @DisplayName("Deve lançar exceção para token nulo ou vazio na validação e obtenção de usuário")
  void deveLancarExcecaoSeTokenNuloOuVazio() {
    assertThrows(RefreshTokenInvalidoException.class, () -> service.isValid(null));
    assertThrows(RefreshTokenInvalidoException.class, () -> service.isValid(""));
    assertThrows(RefreshTokenInvalidoException.class, () -> service.getUsuario(null));
    assertThrows(RefreshTokenInvalidoException.class, () -> service.getUsuario(""));
  }

  @Test
  @DisplayName("Deve lançar exceção ao ocorrer erro interno na validação do token")
  void deveLancarExcecaoSeErroAoValidarToken() {
    when(refreshTokenRepository.findByToken(anyString())).thenThrow(new RuntimeException());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.isValid("token"));

    assertEquals(ERRO_VALIDAR_REFRESH_TOKEN.getChave(), exception.getMessage());
  }

  // ======= OBTENÇÃO DE USUÁRIO PELO TOKEN =======
  @Test
  @DisplayName("Deve retornar usuário com token válido")
  void deveRetornarUsuarioComTokenValido() {
    String token = "abc123";

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUsuario(this.usuario);
    refreshToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));

    when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

    Usuario resultado = service.getUsuario(token);

    assertEquals(this.usuario, resultado);
  }

  @Test
  @DisplayName("Deve lançar exceção para token expirado ou inexistente ao obter usuário")
  void deveLancarExcecaoComTokenExpiradoOuInexistente() {
    String token = "xyz";

    RefreshToken expirado = new RefreshToken();
    expirado.setExpiryDate(LocalDateTime.now().minusMinutes(5));
    when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(expirado));

    assertThrows(RefreshTokenInvalidoException.class, () -> service.getUsuario(token));

    when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

    assertThrows(RefreshTokenInvalidoException.class, () -> service.getUsuario(token));
  }

  @Test
  @DisplayName("Deve lançar exceção ao ocorrer erro interno ao buscar usuário por token")
  void deveLancarExcecaoAoBuscarUsuarioPorTokenComErroInterno() {
    when(refreshTokenRepository.findByToken(anyString()))
        .thenThrow(new RuntimeException("Erro ao buscar token"));

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.getUsuario("token123"));

    assertEquals(ERRO_OBTER_USUARIO_REFRESH_TOKEN.getChave(), exception.getMessage());
  }

  // ======= DELEÇÃO DE TOKENS =======
  @Test
  @DisplayName("Deve deletar tokens do usuário com sucesso")
  void deveDeletarTokensDoUsuarioComSucesso() {
    doNothing().when(refreshTokenRepository).deleteByUsuario(usuario);

    assertDoesNotThrow(() -> service.deleteByUsuario(usuario));

    verify(refreshTokenRepository).deleteByUsuario(usuario);
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar deletar tokens com erro interno")
  void deveLancarExcecaoAoDeletarTokensComErroInterno() {
    doThrow(new RuntimeException("Erro ao deletar"))
        .when(refreshTokenRepository)
        .deleteByUsuario(usuario);

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> service.deleteByUsuario(usuario));

    assertEquals(ERRO_REMOVER_REFRESH_TOKENS_USUARIO.getChave(), exception.getMessage());
  }
}
