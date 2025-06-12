package com.autenticacao.api.app.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MensagemUtil {

  private final MessageSource messageSource;

  public MensagemUtil(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String getMensagem(String key, Object... params) {
    return messageSource.getMessage(key, params, LocaleContextHolder.getLocale());
  }
}
