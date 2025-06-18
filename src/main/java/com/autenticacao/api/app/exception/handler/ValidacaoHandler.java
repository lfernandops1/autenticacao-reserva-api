package com.autenticacao.api.app.exception.handler;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.app.exception.ExceptionAbstract;
import com.autenticacao.api.app.exception.ExceptionNotFoundAbstract;
import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;
import com.autenticacao.api.app.util.MensagemUtil;
import com.autenticacao.api.app.util.enums.MensagemSistema;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ValidacaoHandler {
  private final MensagemUtil mensagemUtil;

  @ExceptionHandler(ExceptionNotFoundAbstract.class)
  public ResponseEntity<Object> handleNotFound(ExceptionAbstract ex) {
    log.warn("[NOT_FOUND] {}", ex.getCodigo());
    return buildResponse(ex, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ExceptionAbstract.class)
  public ResponseEntity<Object> handleBusiness(ExceptionAbstract ex) {
    if (ex.getCodigo() == MensagemSistema.NAO_IDENTIFICADO.getCodigo()) {
      log.error("Erro interno não identificado", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ErroDTO.builder().codigo(ex.getCodigo()).mensagem("Erro interno inesperado").build());
    }

    log.warn("[BAD_REQUEST] {}", ex.getCodigo());
    return buildResponse(ex, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<Object> buildResponse(ExceptionAbstract ex, HttpStatus status) {
    ErroDTO erro =
        ErroDTO.builder().codigo(ex.getCodigo()).mensagem(getMensagemFormatada(ex)).build();
    return ResponseEntity.status(status).body(ErrosDTO.builder().erros(List.of(erro)).build());
  }

  private String getMensagemFormatada(ExceptionAbstract ex) {
    return mensagemUtil.getMensagem(ex.getMensagem(), (Object) ex.getParams());
  }
}
