package com.autenticacao.api.app.controller;

import static com.autenticacao.api.app.Constantes.ROTAS.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import com.autenticacao.api.app.config.security.TokenService;
import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.RefreshTokenRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.service.impl.AutenticacaoServiceImpl;
import com.autenticacao.api.app.service.impl.RefreshTokenServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(API_AUTENTICAR)
@RequiredArgsConstructor
public class AutenticacaoController {

  private final AutenticacaoServiceImpl autenticacaoService;

  private final AuthenticationManager authenticationManager;

  private final RefreshTokenServiceImpl refreshTokenService;
  private final TokenService tokenService;

  @PostMapping(LOGIN)
  public ResponseEntity<LoginResponseDTO> login(
      @RequestBody @Valid LoginUsuarioRequestDTO loginRequest) {
    LoginResponseDTO loginResponse = autenticacaoService.login(loginRequest, authenticationManager);
    return ResponseEntity.ok(loginResponse);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<LoginResponseDTO> refreshToken(
      @RequestBody @Valid RefreshTokenRequestDTO request) {
    String refreshToken = request.refreshToken();

    if (!refreshTokenService.isValid(refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Usuario usuario = refreshTokenService.getUsuario(refreshToken);
    String newAccessToken = tokenService.generateToken(usuario);

    return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, refreshToken));
  }

  @PutMapping(ALTERAR_SENHA)
  public ResponseEntity<Void> alterarSenha(
      @RequestBody @Valid AlterarSenhaRequestDTO alterarSenhaRequest) {
    autenticacaoService.alterarSenha(alterarSenhaRequest);
    return ResponseEntity.noContent().build();
  }
}
