package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

public interface SenhaService {

  void validarSenhaExpirada(Usuario usuario);

  boolean senhaExpirada(Autenticacao autenticacao);

  void alterarSenha(Usuario usuario, String novaSenha);
}
