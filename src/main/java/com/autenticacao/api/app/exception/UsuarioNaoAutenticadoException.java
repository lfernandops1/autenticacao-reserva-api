package com.autenticacao.api.app.exception;

public class UsuarioNaoAutenticadoException extends RuntimeException {
  public UsuarioNaoAutenticadoException(String message) {
    super(message);
  }
}
