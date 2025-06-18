package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Usuario;

public interface HistoricoUsuarioService {
  void registrarHistoricoCompleto(Usuario antes, Usuario depois);
}
