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
    // Busca a mensagem no properties usando a key igual ao nome do enum
    return messageSource.getMessage(
        "enum.EValidacao." + codigoEnum.name(), null, codigoEnum.name(), locale);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrosDTO> handleValidationException(
      MethodArgumentNotValidException ex, Locale locale) {
    List<ErroDTO> erros =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    ErroDTO.builder()
                        .codigo(HttpStatus.BAD_REQUEST.value())
                        .mensagem(error.getField() + ": " + error.getDefaultMessage())
                        .build())
            .toList();

    return ResponseEntity.badRequest().body(new ErrosDTO(erros));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrosDTO> handleConstraintViolationException(
      ConstraintViolationException ex, Locale locale) {
    List<ErroDTO> erros =
        ex.getConstraintViolations().stream()
            .map(
                cv ->
                    ErroDTO.builder()
                        .codigo(HttpStatus.BAD_REQUEST.value())
                        .mensagem(cv.getPropertyPath() + ": " + cv.getMessage())
                        .build())
            .toList();

    return ResponseEntity.badRequest().body(new ErrosDTO(erros));
  }

  @ExceptionHandler({EmailEmUsoException.class, TelefoneEmUsoException.class})
  public ResponseEntity<ErrosDTO> handleConflictExceptions(RuntimeException ex, Locale locale) {
    // Mapeie a exceção para o enum correspondente
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

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrosDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErroDTO erro =
        ErroDTO.builder().codigo(HttpStatus.BAD_REQUEST.value()).mensagem(ex.getMessage()).build();
    return ResponseEntity.badRequest().body(new ErrosDTO(List.of(erro)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrosDTO> handleGenericException(Locale locale) {
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
