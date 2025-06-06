package com.autenticacao.api.exception;

public class EmailEmUsoException extends RuntimeException {
  public EmailEmUsoException(String message) {
    super(message);
  }
}
