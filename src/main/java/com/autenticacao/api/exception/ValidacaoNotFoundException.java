package com.autenticacao.api.exception;

import com.autenticacao.api.util.enums.EValidacao;

public class ValidacaoNotFoundException extends ExceptionNotFoundAbstract {

  public ValidacaoNotFoundException(EValidacao validacao, String... params) {
    super(validacao, params);
  }
}
