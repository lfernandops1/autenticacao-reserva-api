package com.autenticacao.api.exception.handler;

import static com.autenticacao.api.util.MensagemUtils.getMensagem;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.exception.erro.ErroDTO;
import com.autenticacao.api.exception.erro.ErrosDTO;
import com.autenticacao.api.util.enums.EValidacao;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidacaoParametroHandler {

  private static final String DELIMITADOR_LISTA = "[";
  private static final String SEPARADOR_CAMINHO = ".";

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(ConstraintViolationException.class)
  public ErrosDTO handleConstraintViolationException(ConstraintViolationException ex) {
    Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
    List<ErroDTO> erros =
        constraintViolations.stream().map(this::criarErroValidacao).collect(Collectors.toList());

    return ErrosDTO.builder().erros(erros).build();
  }

  private ErroDTO criarErroValidacao(ConstraintViolation<?> constraintViolation) {
    String nomeParametro = constraintViolation.getPropertyPath().toString();
    String tipoValidacao =
        constraintViolation
            .getConstraintDescriptor()
            .getAnnotation()
            .annotationType()
            .getSimpleName();

    String mensagem = construirMensagemErro(tipoValidacao, nomeParametro);
    log.warn(mensagem);

    return ErroDTO.builder()
        .codigo(EValidacao.ENTRADA_DE_DADOS_INVALIDA.getCodigo())
        .mensagem(mensagem)
        .build();
  }

  private String construirMensagemErro(String tipoValidacao, String nomeParametro) {
    if (nomeParametro.contains(DELIMITADOR_LISTA)) {
      String caminhoParametro =
          nomeParametro.substring(nomeParametro.lastIndexOf(SEPARADOR_CAMINHO));
      return getMensagem(tipoValidacao + caminhoParametro);
    }

    String caminhoParametro = nomeParametro.substring(nomeParametro.indexOf(SEPARADOR_CAMINHO));
    return getMensagem(tipoValidacao + caminhoParametro);
  }
}
