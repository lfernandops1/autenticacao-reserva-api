package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.util.enums.TipoMovimentacao;

public interface HistoricoUsuarioService {
  void registrarAlteracaoUsuario(Usuario modificado, Usuario responsavel, TipoMovimentacao tipo);
}
