package com.autenticacao.api.app.exception;

import com.autenticacao.api.app.util.enums.MensagemSistema;

import lombok.Getter;

/** Exceção base para exceções de negócio com suporte a códigos e parâmetros de mensagem. */
public abstract class ExceptionAbstract extends RuntimeException {

  private final MensagemSistema mensagemSistema;

  @Getter private String[] params;

  public Integer getCodigo() {
    return this.mensagemSistema.getCodigo();
  }

  public String getMensagem() {
    return this.mensagemSistema.getChave(); // Você pode aplicar i18n aqui se quiser
  }

  public ExceptionAbstract(MensagemSistema mensagemSistema) {
    super(mensagemSistema.getChave());
    this.mensagemSistema = mensagemSistema;
  }

  public ExceptionAbstract(MensagemSistema mensagemSistema, String... params) {
    super(mensagemSistema.getChave());
    this.mensagemSistema = mensagemSistema;
    this.params = params;
  }
}
