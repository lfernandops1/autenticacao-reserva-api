package com.autenticacao.api.exception;

import com.autenticacao.api.util.enums.EValidacao;

import lombok.Getter;

public abstract class ExceptionAbstract extends RuntimeException {

  private final EValidacao validacao;
  @Getter private String[] params;

  public Integer getCodigo() {
    return this.validacao.getCodigo();
  }

  // Retorna a mensagem formatada com parâmetros, caso existam
  public String getMensagem() {
    if (params != null && params.length > 0) {
      return this.validacao.getMessageKey();
    }
    return this.validacao.getMessageKey();
  }

  public ExceptionAbstract(EValidacao validacao) {
    super(validacao.getMessageKey());
    this.validacao = validacao;
  }

  public ExceptionAbstract(EValidacao validacao, String... params) {
    super(validacao.getMessageKey());
    this.validacao = validacao;
    this.params = params;
  }
}
