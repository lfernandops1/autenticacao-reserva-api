package com.autenticacao.api.app.exception;

import com.autenticacao.api.app.util.enums.MensagemSistema;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class ExceptionNotFoundAbstract extends RuntimeException {

  private final MensagemSistema validacao;
  @Getter private String[] params;

  public Integer getCodigo() {
    return this.validacao.getCodigo();
  }

  public String getMensagem() {
    return this.validacao.getChave();
  }
}
