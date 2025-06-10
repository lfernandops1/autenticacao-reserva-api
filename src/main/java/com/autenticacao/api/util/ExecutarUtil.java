package com.autenticacao.api.util;

import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;

import com.autenticacao.api.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.exception.ValidacaoException;
import com.autenticacao.api.exception.ValidacaoNotFoundException;
import com.autenticacao.api.util.enums.EValidacao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutarUtil {

  private static String resolveMensagem(
      MessageSource messageSource, EValidacao validacao, Locale locale) {
    return messageSource.getMessage(
        "enum.EValidacao." + validacao.name(), null, validacao.name(), locale);
  }

  public static <T> T executarComandoComTratamentoErroComMensagem(
      Supplier<T> comando, String mensagem) {
    try {
      return comando.get();
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.warn(mensagem);
      throw new AutenticacaoApiRunTimeException(mensagem);
    }
  }
}
