package com.autenticacao.api.app.exception;

public class EmailEmUsoException extends RuntimeException {
  public EmailEmUsoException(String message) {
    super(message);
  }
}
