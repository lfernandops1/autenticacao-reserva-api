package com.autenticacao.api.app.exception;

public class ContaBloqueadaException extends RuntimeException {

  public ContaBloqueadaException(String mensagem) {
    super(mensagem);
  }
}
