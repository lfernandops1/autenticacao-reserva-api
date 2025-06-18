package com.autenticacao.api.app.exception;

import com.autenticacao.api.app.util.enums.MensagemSistema;

import lombok.Getter;

@Getter
public class ValidacaoNotFoundException extends RuntimeException {

  private final MensagemSistema mensagemEnum;

  public ValidacaoNotFoundException(MensagemSistema mensagemEnum, String... params) {
    super(mensagemEnum.getChave());
    this.mensagemEnum = mensagemEnum;
  }
}
