package com.autenticacao.api.app.service;

import java.util.UUID;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface AutenticacaoCadastroService {
  void criar(CadastroUsuarioRequest dto, Usuario usuario);

  void atualizar(AtualizarUsuarioRequest dto);

  void desativar(UUID usuarioId);
}
