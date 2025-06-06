package com.autenticacao.api.exception;

public class TelefoneEmUsoException extends RuntimeException {
  public TelefoneEmUsoException(String message) {
    super(message);
  }
}
