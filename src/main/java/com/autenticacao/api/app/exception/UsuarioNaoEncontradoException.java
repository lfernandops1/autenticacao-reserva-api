package com.autenticacao.api.app.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
  public UsuarioNaoEncontradoException(String msg) {
    super(msg);
  }
}
