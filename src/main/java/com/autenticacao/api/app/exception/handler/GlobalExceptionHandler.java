package com.autenticacao.api.app.exception.handler;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.autenticacao.api.app.exception.EmailEmUsoException;
import com.autenticacao.api.app.exception.SenhaExpiradaException;
import com.autenticacao.api.app.exception.TelefoneEmUsoException;
import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;
import com.autenticacao.api.app.util.enums.MensagemSistema;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private final MessageSource messageSource;
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  private String getMensagem(MensagemSistema codigoEnum, Locale locale) {
    return messageSource.getMessage(
        "enum.MensagemSistema." + codigoEnum.name(), null, codigoEnum.name(), locale);
  }

  @ExceptionHandler(StackOverflowError.class)
  public ResponseEntity<String> handleStackOverflowError(StackOverflowError ex) {
    logger.error("StackOverflowError capturado:", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Erro interno: StackOverflow");
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

    MensagemSistema codigoEnum =
        switch (ex.getClass().getSimpleName()) {
          case "EmailEmUsoException" -> MensagemSistema.EMAIL_JA_CADASTRADO;
          case "TelefoneEmUsoException" -> MensagemSistema.TELEFONE_JA_CADASTRADO;
          default -> MensagemSistema.NAO_IDENTIFICADO;
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
    MensagemSistema codigoEnum = MensagemSistema.NAO_IDENTIFICADO;
    ErroDTO erro =
        ErroDTO.builder()
            .codigo(codigoEnum.getCodigo())
            .mensagem(getMensagem(codigoEnum, locale))
            .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrosDTO(List.of(erro)));
  }
}
