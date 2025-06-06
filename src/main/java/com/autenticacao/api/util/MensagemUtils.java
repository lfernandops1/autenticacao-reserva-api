package com.autenticacao.api.util;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.autenticacao.api.util.interfaces.IEnumLabel;

import jakarta.annotation.PostConstruct;

@Component
public class MensagemUtils {
  private static MessageSource MESSAGE_SOURCE;

  @Autowired private MessageSource resourceBundle;

  @PostConstruct
  public void init() {
    MESSAGE_SOURCE = resourceBundle;
  }

  public String getMensagem(String chave, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return resourceBundle.getMessage(chave, args, locale);
  }

  public static String getMensagem(String chave) {
    Locale locale = LocaleContextHolder.getLocale();
    return MESSAGE_SOURCE.getMessage(chave, null, locale);
  }

  public static <E extends Enum<E>> String getEnumLabel(IEnumLabel<E> e) {
    Locale locale = LocaleContextHolder.getLocale();
    String messageKey = "enum." + e.getClass().getSimpleName() + "." + ((Enum<?>) e).name();
    return MESSAGE_SOURCE.getMessage(messageKey, null, locale);
  }

  public static <E extends Enum<E>> String getEnumLabel(IEnumLabel<E> e, String... params) {
    Locale locale = LocaleContextHolder.getLocale();
    String messageKey = "enum." + e.getClass().getSimpleName() + "." + ((Enum<?>) e).name();
    return MESSAGE_SOURCE.getMessage(messageKey, params, locale);
  }
}
