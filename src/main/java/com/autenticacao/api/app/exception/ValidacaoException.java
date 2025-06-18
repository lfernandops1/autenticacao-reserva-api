package com.autenticacao.api.app.exception;

import com.autenticacao.api.app.util.enums.MensagemSistema;

public class ValidacaoException extends ExceptionAbstract {

  public ValidacaoException(MensagemSistema mensagemSistema) {
    super(mensagemSistema);
  }

  public ValidacaoException(MensagemSistema mensagemSistema, String... params) {
    super(mensagemSistema, params);
  }
}
