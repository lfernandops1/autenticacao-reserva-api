package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Usuario;

public interface TentativaLoginService {
  void validarBloqueio(Usuario usuario);

  void registrarFalha(Usuario usuario);

  void resetarTentativas(Usuario usuario);
}
