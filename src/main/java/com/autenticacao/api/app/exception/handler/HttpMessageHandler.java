package com.autenticacao.api.app.exception.handler;

import static com.autenticacao.api.app.util.enums.MensagemSistema.*;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.autenticacao.api.app.exception.ValidacaoException;
import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;
import com.autenticacao.api.app.util.MensagemUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpMessageHandler {

  private final MensagemUtil mensagemUtil;

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageException(HttpMessageNotReadableException ex) {
    return prepararValidacaoHandler(ex);
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Object> handleMismatchException(MethodArgumentTypeMismatchException ex) {
    return prepararValidacaoHandler(ex);
  }

  @ExceptionHandler({MissingPathVariableException.class})
  public ResponseEntity<Object> handleMissingPathVariableException(
      MissingPathVariableException ex) {
    return prepararValidacaoHandler(ex);
  }

  private ResponseEntity<Object> prepararValidacaoHandler(Exception ex) {
    List<ErroDTO> erros = new ArrayList<>();
    String mensagem = ENTRADA_DADOS_INVALIDA.getChave();

    List<JsonMappingException.Reference> path;

    if (ex instanceof MissingPathVariableException) {
      var reference =
          new JsonMappingException.Reference(
              ex, ((MissingPathVariableException) ex).getVariableName());
      path = new ArrayList<>();
      path.add(reference);
      mensagem = prepararMensagem(ex, path);
    }

    if (ex.getCause() instanceof MismatchedInputException) {
      path = ((MismatchedInputException) ex.getCause()).getPath();
      mensagem = prepararMensagem(ex, path);
    }

    if (ex.getCause() instanceof NumberFormatException) {
      assert ex instanceof MethodArgumentTypeMismatchException;
      mensagem =
          format(
              mensagemUtil.getMensagem(CAMPO_GENERICO_INVALIDO.getChave()),
              ((MethodArgumentTypeMismatchException) ex).getName());
    }

    if (ex instanceof MethodArgumentNotValidException) {
      mensagem =
          ((MethodArgumentNotValidException) ex)
              .getMessage(); // ou formate com MensagemSistema se desejar
    }

    erros.add(
        ErroDTO.builder().codigo(ENTRADA_DADOS_INVALIDA.getCodigo()).mensagem(mensagem).build());

    log.warn(mensagem);
    return new ResponseEntity<>(ErrosDTO.builder().erros(erros).build(), HttpStatus.BAD_REQUEST);
  }

  private String prepararMensagem(Exception ex, List<JsonMappingException.Reference> path) {
    if (Objects.isNull(path)) throw new ValidacaoException(CAMPO_INVALIDO_NAO_IDENTIFICADO);

    String grupos = null;
    String propriedade = null;

    for (int i = 0; i < path.size(); i++) {
      String campo = path.get(i).getFieldName();
      if (Objects.nonNull(campo)) {
        if (path.size() == 1 || i == path.size() - 1) {
          propriedade = campo;
          break;
        }

        grupos = (grupos == null) ? campo : grupos.concat(".").concat(campo);
      }
    }

    String valorErro = obterValorErro(ex);
    String mensagem;

    if (Objects.isNull(grupos)) {
      mensagem =
          format(
              mensagemUtil.getMensagem(CAMPO_GENERICO_INVALIDO.getChave()), propriedade, valorErro);
    } else {
      mensagem =
          format(
              mensagemUtil.getMensagem(LISTA_GENERICO_INVALIDO.getChave()),
              propriedade,
              grupos,
              valorErro);
    }

    return mensagem;
  }

  private String obterValorErro(Exception ex) {
    try {
      JsonParser parser = (JsonParser) ((MismatchedInputException) ex.getCause()).getProcessor();
      return parser.getText();
    } catch (Exception e) {
      return "não identificado";
    }
  }
}
