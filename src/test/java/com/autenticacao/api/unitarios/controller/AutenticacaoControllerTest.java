package com.autenticacao.api.unitarios.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequest;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.RefreshTokenRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.endpoint.controller.AutenticacaoController;
import com.autenticacao.api.app.service.AutenticacaoService;
import com.autenticacao.api.app.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

  @Mock private AutenticacaoService autenticacaoService;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private TokenService tokenService;

  @InjectMocks private AutenticacaoController autenticacaoController;

  private LoginUsuarioRequestDTO loginRequest;
  private LoginResponseDTO loginResponse;
  private RefreshTokenRequestDTO refreshRequest;
  private Usuario usuario;
  private AlterarSenhaRequest alterarSenhaRequest;

  @BeforeEach
  void setup() {
    loginRequest = new LoginUsuarioRequestDTO("email@email.com", "senha123");
    loginResponse = new LoginResponseDTO("access-token", "refresh-token");
    refreshRequest = new RefreshTokenRequestDTO("refresh-token-antigo");
    usuario = Usuario.builder().id(UUID.randomUUID()).email("email@email.com").build();
    alterarSenhaRequest = new AlterarSenhaRequest("senha-antiga", "nova-senha");
  }

  @Test
  @DisplayName("Deve realizar login e retornar tokens")
  void deveLogarERetornarTokens() {
    when(autenticacaoService.login(loginRequest)).thenReturn(loginResponse);

    ResponseEntity<LoginResponseDTO> response = autenticacaoController.login(loginRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo(loginResponse);
    verify(autenticacaoService).login(loginRequest);
  }

  @Test
  @DisplayName("Deve retornar 401 se refresh token for inválido")
  void deveRetornarUnauthorizedSeRefreshTokenInvalido() {
    when(refreshTokenService.isValid(refreshRequest.refreshToken())).thenReturn(false);

    ResponseEntity<LoginResponseDTO> response = autenticacaoController.refreshToken(refreshRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(401);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @DisplayName("Deve gerar novo token de acesso e refresh token válidos")
  void deveRenovarTokenComSucesso() {
    String novoRefreshToken = "novo-refresh-token";
    String novoAccessToken = "novo-access-token";

    when(refreshTokenService.isValid(refreshRequest.refreshToken())).thenReturn(true);
    when(refreshTokenService.rotateRefreshToken(refreshRequest.refreshToken()))
        .thenReturn(novoRefreshToken);
    when(refreshTokenService.getUsuario(novoRefreshToken)).thenReturn(usuario);
    when(tokenService.generateToken(usuario)).thenReturn(novoAccessToken);

    ResponseEntity<LoginResponseDTO> response = autenticacaoController.refreshToken(refreshRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody())
        .isEqualTo(new LoginResponseDTO(novoAccessToken, novoRefreshToken));
  }

  @Test
  @DisplayName("Deve alterar a senha do usuário com sucesso")
  void deveAlterarSenhaComSucesso() {
    doNothing().when(autenticacaoService).alterarSenha(alterarSenhaRequest);

    ResponseEntity<Void> response = autenticacaoController.alterarSenha(alterarSenhaRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(204);
    verify(autenticacaoService).alterarSenha(alterarSenhaRequest);
  }

  @Test
  @DisplayName("Deve revogar refresh token com sucesso")
  void deveRevogarRefreshTokenComSucesso() {
    String token = "algum-refresh-token";

    doNothing().when(refreshTokenService).deleteByToken(token);

    ResponseEntity<Void> response = autenticacaoController.revokeRefreshToken(token);

    assertThat(response.getStatusCodeValue()).isEqualTo(204);
    verify(refreshTokenService).deleteByToken(token);
  }
}
