package com.autenticacao.api.app.endpoint.api;

import static com.autenticacao.api.app.Constantes.Rotas.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autenticacao.api.app.domain.DTO.request.AlterarSenhaRequest;
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
  @PutMapping(ALTERAR_SENHA)
  ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequest alterarSenhaRequest);

  @Operation(summary = "Revoga um refresh token (logout manual)")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Refresh token removido com sucesso"),
    @ApiResponse(responseCode = "400", description = "Token inválido ou ausente")
  })
  @DeleteMapping(REVOKE_REFRESH_TOKEN)
  ResponseEntity<Void> revokeRefreshToken(@RequestParam("token") String token);
}
