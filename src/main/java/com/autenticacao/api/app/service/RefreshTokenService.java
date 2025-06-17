package com.autenticacao.api.app.service;

import com.autenticacao.api.app.domain.entity.Usuario;
import com.autenticacao.api.app.exception.RefreshTokenInvalidoException;

public interface RefreshTokenService {
  String createRefreshToken(Usuario usuario);

  boolean isValid(String token);

  Usuario getUsuario(String token) throws RefreshTokenInvalidoException;

  void deleteByToken(String token);

  String rotateRefreshToken(String oldToken);

  void deleteByUsuario(Usuario usuario);
}
