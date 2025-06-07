package com.autenticacao.api.exception;

public class SenhaExpiradaException extends RuntimeException {

  public SenhaExpiradaException() {
    super();
  }

  public SenhaExpiradaException(String message) {
    super(message);
  }

  public SenhaExpiradaException(String message, Throwable cause) {
    super(message, cause);
  }
}
