package com.autenticacao.api.app.exception;

public class SenhaExpiradaException extends RuntimeException {
  public SenhaExpiradaException(String message) {
    super(message);
  }
}
