package com.autenticacao.api.app.exception.handler;

import static com.autenticacao.api.app.util.enums.MensagemSistema.ENTRADA_DE_DADOS_INVALIDA;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;

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
    List<ErroDTO> erros =
        ex.getConstraintViolations().stream()
            .map(this::criarErroValidacao)
            .collect(Collectors.toList());

    return ErrosDTO.builder().erros(erros).build();
  }

  private ErroDTO criarErroValidacao(ConstraintViolation<?> violation) {
    String parametro = violation.getPropertyPath().toString();
    String tipoValidacao =
        violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

    String mensagem = construirMensagemErro(tipoValidacao, parametro);

    log.warn(mensagem);

    return ErroDTO.builder()
        .codigo(ENTRADA_DE_DADOS_INVALIDA.getCodigo())
        .mensagem(mensagem)
        .build();
  }

  private String construirMensagemErro(String tipoValidacao, String caminhoCompleto) {
    String nomeCampo = extrairNomeCampo(caminhoCompleto);
    String chave = tipoValidacao + "." + nomeCampo;

    String mensagem = getMensagem(chave);

    if (mensagem.equals(chave)) {
      mensagem = getMensagem(tipoValidacao + ".parametro.generico");
    }

    return format(mensagem, nomeCampo);
  }

  private String extrairNomeCampo(String caminho) {
    if (caminho.contains(DELIMITADOR_LISTA)) {
      int index = caminho.lastIndexOf(SEPARADOR_CAMINHO);
      return index != -1 ? caminho.substring(index + 1) : caminho;
    }

    int index = caminho.indexOf(SEPARADOR_CAMINHO);
    return index != -1 ? caminho.substring(index + 1) : caminho;
  }

  private String getMensagem(String chave) {
    return getMensagem(chave);
  }

  private String format(String mensagem, Object... params) {
    return params.length == 0 ? mensagem : String.format(mensagem, params);
  }
}
