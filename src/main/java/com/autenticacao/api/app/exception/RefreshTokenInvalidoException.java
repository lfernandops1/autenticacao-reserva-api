package com.autenticacao.api.app.exception;

public class RefreshTokenInvalidoException extends RuntimeException {
  public RefreshTokenInvalidoException(String message) {
    super(message);
  }
}
