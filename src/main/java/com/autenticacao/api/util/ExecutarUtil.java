package com.autenticacao.api.util;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;

import com.autenticacao.api.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.exception.ValidacaoException;
import com.autenticacao.api.exception.ValidacaoNotFoundException;
import com.autenticacao.api.sample.Parse;
import com.autenticacao.api.util.enums.EValidacao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecutarUtil {

  private static String resolveMensagem(
      MessageSource messageSource, EValidacao validacao, Locale locale) {
    return messageSource.getMessage(
        "enum.EValidacao." + validacao.name(), null, validacao.name(), locale);
  }

  public static <T> T executarComandoComTratamentoErro(
      Supplier<T> comando, MessageSource messageSource, Locale locale) {
    try {
      return comando.get();
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
      String mensagem = resolveMensagem(messageSource, EValidacao.NAO_IDENTIFICADO, locale);
      throw new AutenticacaoApiRunTimeException(EValidacao.NAO_IDENTIFICADO, mensagem);
    }
  }

  public static void executarComandoComTratamentoSemRetorno(
      Runnable comando, MessageSource messageSource, Locale locale) {
    try {
      comando.run();
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      String mensagem = resolveMensagem(messageSource, EValidacao.NAO_IDENTIFICADO, locale);
      throw new AutenticacaoApiRunTimeException(EValidacao.NAO_IDENTIFICADO, mensagem);
    }
  }

  public static void executarComandoComTratamentoSemRetornoComMensagem(
      Runnable comando, String mensagem) {
    try {
      comando.run();
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      log.warn(mensagem);
      throw new AutenticacaoApiRunTimeException(mensagem);
    }
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

  public static <T> T executarComandoComTratamentoErroGenerico(
      Supplier<T> comando, MessageSource messageSource, Locale locale) {
    try {
      return comando.get();
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      String mensagem = resolveMensagem(messageSource, EValidacao.NAO_IDENTIFICADO, locale);
      throw new AutenticacaoApiRunTimeException(EValidacao.NAO_IDENTIFICADO, mensagem);
    }
  }

  public static <RequestRequest, Entity, ResponseDTO>
      ResponseDTO executarComandoComTratamentoErroComMensagemComParseResource(
          Supplier<Entity> comando,
          String mensagem,
          Parse<RequestRequest, Entity, ResponseDTO> parse) {
    try {
      Entity entity = comando.get();
      return parse.toResponse(entity);
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new AutenticacaoApiRunTimeException(mensagem);
    }
  }

  public static <RequestRequest, Entity, ResponseDTO> ResponseDTO executarComandoComParseResource(
      Supplier<Entity> comando,
      Parse<RequestRequest, Entity, ResponseDTO> parse,
      MessageSource messageSource,
      Locale locale) {
    try {
      Entity entity = comando.get();
      return parse.toResponse(entity);
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      String mensagem = resolveMensagem(messageSource, EValidacao.NAO_IDENTIFICADO, locale);
      throw new AutenticacaoApiRunTimeException(EValidacao.NAO_IDENTIFICADO, mensagem);
    }
  }

  public static <RequestRequest, Entity, ResponseDTO>
      List<ResponseDTO> executarComandoComTratamentoErroComParseListaResource(
          Supplier<List<Entity>> comando,
          Parse<RequestRequest, Entity, ResponseDTO> parse,
          MessageSource messageSource,
          Locale locale) {
    try {
      List<Entity> entities = comando.get();
      return parse.toResponseList(entities);
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      String mensagem = resolveMensagem(messageSource, EValidacao.NAO_IDENTIFICADO, locale);
      throw new AutenticacaoApiRunTimeException(EValidacao.NAO_IDENTIFICADO, mensagem);
    }
  }

  public static <RequestRequest, Entity, ResponseDTO>
      List<ResponseDTO> executarComandoComTratamentoErroComMensagemComParseListaResource(
          Supplier<List<Entity>> comando,
          String mensagem,
          Parse<RequestRequest, Entity, ResponseDTO> parse) {
    try {
      List<Entity> entities = comando.get();
      return parse.toResponseList(entities);
    } catch (ValidacaoException | ValidacaoNotFoundException ex) {
      throw ex;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new AutenticacaoApiRunTimeException(mensagem);
    }
  }
}
