package com.autenticacao.api.unitarios.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.endpoint.controller.UsuarioController;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.app.util.enums.UserRole;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

  @Mock private UsuarioService usuarioService;

  @InjectMocks private UsuarioController usuarioController;

  private Authentication authentication;

  private UUID usuarioId;
  private Usuario usuario;
  private AtualizarUsuarioRequest atualizarRequest;
  private CadastroUsuarioRequest cadastroRequest;
  private UsuarioDetalhadoResponse detalhadoResponse;
  private UsuarioResumoResponse resumoResponse;

  @BeforeEach
  void setup() {
    usuarioId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    usuario = obterUsuario();
    atualizarRequest = obterAtualizarUsuarioRequest();
    cadastroRequest = obterCadastroUsuarioRequest();
    detalhadoResponse = obterUsuarioDetalhadoResponse();
    resumoResponse = obterUsuarioResumoResponse();
    authentication = mock(Authentication.class);
  }

  @Test
  @DisplayName("Atualiza um usuário e retorna os dados detalhados")
  void deveAtualizarUsuarioERetornarDetalhado() {
    when(usuarioService.atualizarUsuario(this.usuarioId, this.atualizarRequest))
        .thenReturn(this.detalhadoResponse);

    ResponseEntity<UsuarioDetalhadoResponse> response =
        usuarioController.atualizarUsuario(this.usuarioId, this.atualizarRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo(this.detalhadoResponse);
    verify(this.usuarioService).atualizarUsuario(this.usuarioId, this.atualizarRequest);
  }

  @Test
  @DisplayName("Lista todos os usuários cadastrados")
  void deveListarTodosUsuarios() {
    List<UsuarioResumoResponse> lista = List.of(this.resumoResponse);

    when(usuarioService.listarTodos()).thenReturn(lista);

    ResponseEntity<List<UsuarioResumoResponse>> response = usuarioController.listarTodos();

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo(lista);
  }

  @Test
  @DisplayName("Retorna dados do usuário logado quando encontrado")
  void deveRetornarUsuarioLogadoQuandoEncontrado() {
    when(authentication.getName()).thenReturn(this.cadastroRequest.email());
    when(usuarioService.buscarPorEmail(this.cadastroRequest.email()))
        .thenReturn(Optional.of(this.resumoResponse));

    ResponseEntity<UsuarioResumoResponse> response =
        usuarioController.getUsuarioLogado(authentication);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isEqualTo(this.resumoResponse);
  }

  @Test
  @DisplayName("Retorna 404 quando o usuário logado não é encontrado")
  void deveRetornarNotFoundQuandoUsuarioLogadoNaoEncontrado() {
    when(authentication.getName()).thenReturn(obterCadastroUsuarioRequest().email());
    when(usuarioService.buscarPorEmail(obterCadastroUsuarioRequest().email()))
        .thenReturn(Optional.empty());

    ResponseEntity<UsuarioResumoResponse> response =
        usuarioController.getUsuarioLogado(authentication);

    assertThat(response.getStatusCodeValue()).isEqualTo(404);
    assertThat(response.getBody()).isNull();
  }

  @Test
  @DisplayName("Cria um novo usuário com perfil padrão (USER)")
  void deveCriarUsuarioComRoleUser() {
    when(usuarioService.criarUsuario(this.cadastroRequest.withRole(UserRole.USER)))
        .thenReturn(this.resumoResponse);

    ResponseEntity<UsuarioResumoResponse> response =
        usuarioController.criarUsuario(this.cadastroRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(201);
    assertThat(response.getBody()).isEqualTo(this.resumoResponse);
  }

  @Test
  @DisplayName("Cria um novo usuário com perfil ADMIN")
  void deveCriarUsuarioComRoleAdmin() {
    when(usuarioService.criarUsuario(this.cadastroRequest.withRole(UserRole.ADMIN)))
        .thenReturn(this.resumoResponse);

    ResponseEntity<UsuarioResumoResponse> response =
        usuarioController.criarUsuarioAdmin(this.cadastroRequest);

    assertThat(response.getStatusCodeValue()).isEqualTo(201);
    assertThat(response.getBody()).isEqualTo(this.resumoResponse);
  }

  private Usuario obterUsuario() {
    return Usuario.builder()
        .id(usuarioId)
        .nome("João")
        .sobrenome("Silva")
        .email("joao.silva@email.com")
        .telefone("123456789")
        .ativo(true)
        .dataNascimento(LocalDate.of(1990, 1, 1))
        .build();
  }

  private AtualizarUsuarioRequest obterAtualizarUsuarioRequest() {
    return new AtualizarUsuarioRequest(
        "João Atualizado",
        "Silva Atualizado",
        "987654321",
        "joao.atualizado@email.com",
        LocalDate.of(1991, 2, 2),
        LocalDateTime.now(),
        true,
            "SenhaForte123@");
  }

  private CadastroUsuarioRequest obterCadastroUsuarioRequest() {
    return new CadastroUsuarioRequest(
        "Maria",
        "Fernandes",
        "maria@email.com",
        "senhaSegura123",
        "111222333",
        LocalDate.of(1995, 5, 5),
        true,
        UserRole.USER);
  }

  private UsuarioDetalhadoResponse obterUsuarioDetalhadoResponse() {
    return new UsuarioDetalhadoResponse(
        usuarioId, "João", "Silva", "joao.silva@email.com", "123456789", UserRole.USER, true);
  }

  private UsuarioResumoResponse obterUsuarioResumoResponse() {
    return new UsuarioResumoResponse(
        usuarioId, "Maria Fernandes", "maria@email.com", "111222333", true);
  }
}
