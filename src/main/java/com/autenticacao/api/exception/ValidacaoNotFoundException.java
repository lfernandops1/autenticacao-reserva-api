package com.autenticacao.api.exception;

import com.autenticacao.api.util.enums.EValidacao;

public class ValidacaoNotFoundException extends RuntimeException {

  private final EValidacao mensagemEnum;

  public ValidacaoNotFoundException(EValidacao mensagemEnum, String... params) {
    super(mensagemEnum.getMessageKey());
    this.mensagemEnum = mensagemEnum;
  }

  public EValidacao getMensagemEnum() {
    return mensagemEnum;
  }
}
