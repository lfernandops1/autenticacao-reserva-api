package com.autenticacao.api.exception;

public class SenhaExpiradaException extends RuntimeException {
  public SenhaExpiradaException(String message) {
    super(message);
  }
}
