package com.autenticacao.api.app.exception.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.app.exception.AutenticacaoApiRunTimeException;
import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RuntimeExceptionHandler {

  @ExceptionHandler(AutenticacaoApiRunTimeException.class)
  public ResponseEntity<Object> validacaoHandle(AutenticacaoApiRunTimeException ex) {
    List<ErroDTO> erros = new ArrayList<>();

    erros.add(ErroDTO.builder().codigo(-999).mensagem(ex.getDescricao()).build());
    log.error("Ocorreu um erro interno", ex);

    return new ResponseEntity<>(
        ErrosDTO.builder().erros(erros).build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
