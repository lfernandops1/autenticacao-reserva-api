package com.autenticacao.api.app.util;

import java.util.function.Supplier;

import com.autenticacao.api.app.exception.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutarUtil {

  public static <T> T executarComandoComTratamentoErroComMensagem(
      Supplier<T> comando, String mensagem) {
    try {
      return comando.get();
    } catch (ValidacaoException
             | ValidacaoNotFoundException
             | RefreshTokenInvalidoException
             | SenhaExpiradaException ex) {
      throw ex; // relança diretamente, sem envolvimento
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.warn(mensagem);
      throw new AutenticacaoApiRunTimeException(mensagem, e);
    }
  }
}
