package com.autenticacao.api.util.enums;

import lombok.Getter;

@Getter
public enum EValidacao {
  ENTRADA_DE_DADOS_INVALIDA(-1, "entrada.de.dados.invalida"),
  CAMPO_INVALIDO_NAO_IDENTIFICADO(-2, "campo.invalido.nao.identificado"),
  EMAIL_JA_CADASTRADO(-3, "email.ja.cadastrado"),
  TELEFONE_JA_CADASTRADO(-4, "telefone.ja.cadastrado"),
  EMAIL_NAO_ENCONTRADO(-5, "email.nao.encontrado"),
  USUARIO_NAO_ENCONTRADO_POR_ID(-6, "usuario.nao.encontrado.por.id"),
  USUARIO_NAO_ENCONTRADO_POR_EMAIL(-7, "usuario.nao.encontrado.por.email"),
  USUARIO_DESATIVADO(-8, "usuario.desativado"),
  NAO_IDENTIFICADO(-999, "erro.nao.identificado");

  private final Integer codigo;
  private final String messageKey;

  EValidacao(Integer codigo, String messageKey) {
    this.codigo = codigo;
    this.messageKey = messageKey;
  }
}
