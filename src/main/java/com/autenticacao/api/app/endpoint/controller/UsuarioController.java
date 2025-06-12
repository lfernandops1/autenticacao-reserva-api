package com.autenticacao.api.app.endpoint.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;
import com.autenticacao.api.app.endpoint.api.UsuarioApi;
import com.autenticacao.api.app.service.UsuarioService;
import com.autenticacao.api.app.util.enums.UserRole;

import jakarta.validation.Valid;

@RestController
public class UsuarioController implements UsuarioApi {

  private final UsuarioService usuarioService;

  @Autowired
  public UsuarioController(UsuarioService usuarioService) {
    this.usuarioService = usuarioService;
  }

  @Override
  public ResponseEntity<UsuarioDetalhadoResponse> atualizarUsuario(
      UUID id, @Valid AtualizarUsuarioRequest request) {
    UsuarioDetalhadoResponse response = usuarioService.atualizarUsuario(id, request);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> desativarUsuario(UUID id) {
    usuarioService.desativarUsuario(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<UsuarioDetalhadoResponse> buscarPorId(UUID id) {
    UsuarioDetalhadoResponse response = usuarioService.buscarPorId(id);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<List<UsuarioResumoResponse>> listarTodos() {
    List<UsuarioResumoResponse> usuarios = usuarioService.listarTodos();
    return ResponseEntity.ok(usuarios);
  }

  private ResponseEntity<UsuarioResumoResponse> criarUsuarioComRole(
      CadastroUsuarioRequest request, UserRole role) {
    var requestComRole = request.withRole(role);
    var response = usuarioService.criarUsuario(requestComRole);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  public ResponseEntity<UsuarioResumoResponse> criarUsuario(CadastroUsuarioRequest request) {
    return criarUsuarioComRole(request, UserRole.USER);
  }

  @Override
  public ResponseEntity<UsuarioResumoResponse> criarUsuarioAdmin(CadastroUsuarioRequest request) {
    return criarUsuarioComRole(request, UserRole.ADMIN);
  }
}
