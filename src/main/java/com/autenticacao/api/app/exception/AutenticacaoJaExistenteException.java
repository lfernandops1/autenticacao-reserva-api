package com.autenticacao.api.app.exception;

public class AutenticacaoJaExistenteException extends RuntimeException {
  public AutenticacaoJaExistenteException(String message) {
    super(message);
  }
}
