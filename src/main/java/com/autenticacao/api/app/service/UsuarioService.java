package com.autenticacao.api.app.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.response.UsuarioDetalhadoResponse;
import com.autenticacao.api.app.domain.DTO.response.UsuarioResumoResponse;

public interface UsuarioService {

  UsuarioResumoResponse criarUsuario(CadastroUsuarioRequest request);

  UsuarioDetalhadoResponse atualizarUsuario(UUID id, AtualizarUsuarioRequest request);

  UsuarioDetalhadoResponse buscarPorId(UUID id);

  Optional<UsuarioResumoResponse> buscarPorEmail(String email);

  List<UsuarioResumoResponse> listarTodos();
}
