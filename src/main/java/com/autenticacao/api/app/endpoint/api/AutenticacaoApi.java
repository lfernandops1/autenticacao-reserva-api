package com.autenticacao.api.app.endpoint.api;

import static com.autenticacao.api.app.Constantes.Rotas.*;
import static com.autenticacao.api.app.config.security.util.Roles.ADMIN_OR_SELF;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.LoginUsuarioRequestDTO;
import com.autenticacao.api.app.domain.DTO.request.RefreshTokenRequestDTO;
import com.autenticacao.api.app.domain.DTO.response.LoginResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RequestMapping(API_AUTENTICAR)
public interface AutenticacaoApi {

  @Operation(summary = "Realiza login do usuário")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
  })
  @PostMapping(LOGIN)
  ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginUsuarioRequestDTO loginRequest);

  @Operation(summary = "Renova o token de acesso utilizando refresh token")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
  })
  @PostMapping(REFRESH_TOKEN)
  ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody @Valid RefreshTokenRequestDTO request);

  @Operation(summary = "Altera a senha do usuário autenticado")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
    @ApiResponse(responseCode = "401", description = "Usuário não autorizado")
  })
  @PreAuthorize(ADMIN_OR_SELF)
  @PutMapping(ALTERAR_SENHA)
  ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequestDTO alterarSenhaRequest);
}
