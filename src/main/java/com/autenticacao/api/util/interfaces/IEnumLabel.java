package com.autenticacao.api.util.interfaces;

import com.autenticacao.api.util.MensagemUtils;

public interface IEnumLabel<E extends Enum<E>> {
  default String getDescricao() {
    return MensagemUtils.getEnumLabel(this);
  }

  default String getDescricao(String[] mensagem) {
    return MensagemUtils.getEnumLabel(this, mensagem);
  }
}
