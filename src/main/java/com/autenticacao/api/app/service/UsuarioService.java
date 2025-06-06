package com.autenticacao.api.app.service;

import java.util.List;
import java.util.UUID;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;

public interface UsuarioService {

  UsuarioResumoResponse prepararParaCriarUsuario(CadastroUsuarioRequest request);

  UsuarioDetalhadoResponse atualizarUsuario(UUID id, AtualizarUsuarioRequest request);

  void desativarUsuario(UUID id);

  UsuarioDetalhadoResponse buscarPorId(UUID id);

  List<UsuarioResumoResponse> listarTodos();
}
