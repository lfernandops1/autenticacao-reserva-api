package com.autenticacao.api.app.exception;

import lombok.Getter;

@Getter
public class AutenticacaoApiRunTimeException extends RuntimeException {
  private final String descricao;
  private final Exception e;

  public AutenticacaoApiRunTimeException(String msg, Exception e) {
    super(msg, e);
    this.descricao = msg;
    this.e = e;
  }
}
