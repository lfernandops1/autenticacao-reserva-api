package com.autenticacao.api.exception;

import com.autenticacao.api.util.enums.EValidacao;

import lombok.Getter;

@Getter
public class AutenticacaoApiRunTimeException extends RuntimeException {
  private final String descricao;

  public AutenticacaoApiRunTimeException(String msg) {
    super(msg);
    this.descricao = msg;
  }

  // Recebe mensagem já resolvida (mensagem internacionalizada)
  public AutenticacaoApiRunTimeException(EValidacao validacao, String mensagemFormatada) {
    super(mensagemFormatada);
    this.descricao = mensagemFormatada;
  }

  public AutenticacaoApiRunTimeException(String msg, Throwable causa) {
    super(msg, causa);
    this.descricao = msg;
  }
}
