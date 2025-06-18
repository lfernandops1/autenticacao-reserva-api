package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface HistoricoAutenticacaoService {
  void registrarHistoricoCompleto(Autenticacao antes, Autenticacao depois, Usuario responsavel);
}
