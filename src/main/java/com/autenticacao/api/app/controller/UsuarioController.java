package com.autenticacao.api.app.controller;

import static com.autenticacao.api.app.Constantes.ROTAS.*;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.util.enums.UserRole;

import jakarta.validation.Valid;

@RestController
@RequestMapping(API_USUARIOS)
public class UsuarioController {

  private final UsuarioService usuarioService;

  @Autowired
  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @PostMapping(CRIAR)
  public ResponseEntity<UsuarioResumoResponse> criarUsuario(
      @RequestBody @Valid CadastroUsuarioRequest request) {
    var userRequest = request.withRole(UserRole.USER);
    UsuarioResumoResponse response = usuarioService.criarUsuario(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
  @PutMapping(ATUALIZAR_POR_ID)
  public ResponseEntity<UsuarioDetalhadoResponse> atualizarUsuario(
      @PathVariable UUID id, @RequestBody @Valid AtualizarUsuarioRequest request) {
    UsuarioDetalhadoResponse response = usuarioService.atualizarUsuario(id, request);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
  @PutMapping(DESATIVAR)
  public ResponseEntity<Void> desativarUsuario(@PathVariable UUID id) {
    usuarioService.desativarUsuario(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(ID)
  public ResponseEntity<UsuarioDetalhadoResponse> buscarPorId(@PathVariable UUID id) {
    UsuarioDetalhadoResponse response = usuarioService.buscarPorId(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping(LISTAR_TODOS)
  public ResponseEntity<List<UsuarioResumoResponse>> listarTodos() {
    List<UsuarioResumoResponse> usuarios = usuarioService.listarTodos();
    return ResponseEntity.ok(usuarios);
  }

  @PostMapping(CRIAR_ADMIN)
  public ResponseEntity<UsuarioResumoResponse> criarUsuarioAdmin(
      @RequestBody @Valid CadastroUsuarioRequest request) {
    var adminRequest = request.withRole(UserRole.ADMIN);
    UsuarioResumoResponse response = usuarioService.criarUsuario(adminRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
