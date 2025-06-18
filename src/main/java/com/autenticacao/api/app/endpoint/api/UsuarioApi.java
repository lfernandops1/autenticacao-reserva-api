package com.autenticacao.api.app.endpoint.api;

import static com.autenticacao.api.app.Constantes.Rotas.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RequestMapping(API_USUARIOS)
public interface UsuarioApi {

  @Operation(
      summary = "Atualiza um usuário",
      description =
          "Atualiza os dados de um usuário pelo ID. Apenas administradores ou o próprio usuário podem fazer isso.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
      })
  @PutMapping(ATUALIZAR_POR_ID)
  ResponseEntity<UsuarioDetalhadoResponse> atualizarUsuario(
      @PathVariable UUID id, @RequestBody @Valid AtualizarUsuarioRequest request);

  @Operation(
      summary = "Listar todos os usuários",
      description = "Lista todos os usuários cadastrados. Apenas administradores podem acessar.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
      })
  @GetMapping(LISTAR_TODOS)
  ResponseEntity<List<UsuarioResumoResponse>> listarTodos();

  @Operation(
      summary = "Criar usuário comum",
      description = "Cria um novo usuário com perfil padrão (USER).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
      })
  @PostMapping(CRIAR)
  ResponseEntity<UsuarioResumoResponse> criarUsuario(
      @RequestBody @Valid CadastroUsuarioRequest request);

  @Operation(
      summary = "Criar usuário administrador",
      description = "Cria um novo usuário com perfil ADMIN.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Administrador criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
      })
  @PostMapping(CRIAR_ADMIN)
  ResponseEntity<UsuarioResumoResponse> criarUsuarioAdmin(
      @RequestBody @Valid CadastroUsuarioRequest request);

  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
      })
  @GetMapping(GET_USUARIO_LOGADO)
  ResponseEntity<UsuarioResumoResponse> getUsuarioLogado(Authentication authentication);
}
