package com.autenticacao.api.exception;

import com.autenticacao.api.util.enums.EValidacao;

import lombok.Getter;

public abstract class ExceptionNotFoundAbstract extends RuntimeException {

  private final EValidacao validacao;
  @Getter private String[] params;

  public Integer getCodigo() {
    return this.validacao.getCodigo();
  }

  public String getMensagem() {
    return this.validacao.getMessageKey();
  }

  public ExceptionNotFoundAbstract(EValidacao validacao) {
    super(validacao.getMessageKey());
    this.validacao = validacao;
  }

  public ExceptionNotFoundAbstract(EValidacao validacao, String... params) {
    super(validacao.getMessageKey());
    this.validacao = validacao;
    this.params = params;
  }

  public ExceptionNotFoundAbstract(EValidacao validacao, Throwable cause) {
    super(validacao.getMessageKey(), cause);
    this.validacao = validacao;
  }
}
