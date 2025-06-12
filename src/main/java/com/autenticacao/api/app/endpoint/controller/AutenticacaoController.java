package com.autenticacao.api.app.endpoint.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.RefreshTokenRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.endpoint.api.AutenticacaoApi;
import com.autenticacao.api.app.service.AutenticacaoService;
import com.autenticacao.api.app.service.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AutenticacaoController implements AutenticacaoApi {

  private final AutenticacaoService autenticacaoService;
  private final RefreshTokenService refreshTokenService;
  private final TokenService tokenService;

  @Override
  public ResponseEntity<LoginResponseDTO> login(@Valid LoginUsuarioRequestDTO loginRequest) {
    LoginResponseDTO loginResponse = autenticacaoService.login(loginRequest);
    return ResponseEntity.ok(loginResponse);
  }

  @Override
  public ResponseEntity<LoginResponseDTO> refreshToken(@Valid RefreshTokenRequestDTO request) {
    String refreshToken = request.refreshToken();

    if (!refreshTokenService.isValid(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Usuario usuario = refreshTokenService.getUsuario(refreshToken);
    String newAccessToken = tokenService.generateToken(usuario);

    return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, refreshToken));
  }

  @Override
  public ResponseEntity<Void> alterarSenha(@Valid AlterarSenhaRequestDTO alterarSenhaRequest) {
    autenticacaoService.alterarSenha(alterarSenhaRequest);
    return ResponseEntity.noContent().build();
  }
}
