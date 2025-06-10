package com.autenticacao.api.exception.handler;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.autenticacao.api.exception.EmailEmUsoException;
import com.autenticacao.api.exception.SenhaExpiradaException;
import com.autenticacao.api.exception.TelefoneEmUsoException;
import com.autenticacao.api.exception.erro.ErroDTO;
import com.autenticacao.api.exception.erro.ErrosDTO;
import com.autenticacao.api.util.enums.EValidacao;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  private String getMensagem(EValidacao codigoEnum, Locale locale) {
    return messageSource.getMessage(
        "enum.EValidacao." + codigoEnum.name(), null, codigoEnum.name(), locale);
  }

  // Tratamento unificado para validação de DTOs (@Valid) com resposta padrão
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrosDTO> handleValidationException(
      MethodArgumentNotValidException ex, Locale locale) {

    List<ErroDTO> erros =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error -> {
                  // Tenta buscar a mensagem no messageSource para internacionalização
                  String mensagemTraduzida = messageSource.getMessage(error, locale);
                  return ErroDTO.builder()
                      .codigo(HttpStatus.BAD_REQUEST.value())
                      .mensagem(error.getField() + ": " + mensagemTraduzida)
                      .build();
                })
            .toList();

    return ResponseEntity.badRequest().body(new ErrosDTO(erros));
  }

  // Tratamento para validação de parâmetros com @Validated, lança ConstraintViolationException
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrosDTO> handleConstraintViolationException(
      ConstraintViolationException ex, Locale locale) {

    List<ErroDTO> erros =
        ex.getConstraintViolations().stream()
            .map(
                cv -> {
                  String mensagemTraduzida =
                      messageSource.getMessage(cv.getMessage(), null, cv.getMessage(), locale);
                  return ErroDTO.builder()
                      .codigo(HttpStatus.BAD_REQUEST.value())
                      .mensagem(cv.getPropertyPath() + ": " + mensagemTraduzida)
                      .build();
                })
            .toList();

    return ResponseEntity.badRequest().body(new ErrosDTO(erros));
  }

  // Tratamento de exceções customizadas com enum e mensagens externas
  @ExceptionHandler({EmailEmUsoException.class, TelefoneEmUsoException.class})
  public ResponseEntity<ErrosDTO> handleConflictExceptions(RuntimeException ex, Locale locale) {

    EValidacao codigoEnum =
        switch (ex.getClass().getSimpleName()) {
          case "EmailEmUsoException" -> EValidacao.EMAIL_JA_CADASTRADO;
          case "TelefoneEmUsoException" -> EValidacao.TELEFONE_JA_CADASTRADO;
          default -> EValidacao.NAO_IDENTIFICADO;
        };

    ErroDTO erro =
        ErroDTO.builder()
            .codigo(codigoEnum.getCodigo())
            .mensagem(getMensagem(codigoEnum, locale))
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrosDTO(List.of(erro)));
  }

  @ExceptionHandler(SenhaExpiradaException.class)
  public ResponseEntity<String> handleSenhaExpirada(SenhaExpiradaException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrosDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErroDTO erro =
        ErroDTO.builder().codigo(HttpStatus.BAD_REQUEST.value()).mensagem(ex.getMessage()).build();
    return ResponseEntity.badRequest().body(new ErrosDTO(List.of(erro)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrosDTO> handleGenericException(Exception ex, Locale locale) {
    EValidacao codigoEnum = EValidacao.NAO_IDENTIFICADO;
    ErroDTO erro =
        ErroDTO.builder()
            .codigo(codigoEnum.getCodigo())
            .mensagem(getMensagem(codigoEnum, locale))
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrosDTO(List.of(erro)));
  }
}
