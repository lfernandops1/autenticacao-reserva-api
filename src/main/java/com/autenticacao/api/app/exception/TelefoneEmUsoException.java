package com.autenticacao.api.app.exception;

public class TelefoneEmUsoException extends RuntimeException {
  public TelefoneEmUsoException(String message) {
    super(message);
  }
}
