package com.autenticacao.api.exception.handler;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.exception.erro.ErroDTO;
import com.autenticacao.api.exception.erro.ErrosDTO;
import com.autenticacao.api.exception.erro.ValidacaoDTO;
import com.autenticacao.api.util.MensagemUtils;
import com.autenticacao.api.util.enums.EValidacao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidacaoMetodoHandler {

  private static final char SEPARADOR_NODES = '.';
  private static final String CAMINHO_OBJETO_PROPERTIES = ".objeto.generico";
  private static final String CAMINHO_LISTA_PROPERTIES = ".lista.generico";
  private static final int QUANTIDADE_NODES_OBJETO = 2;
  private static final String IDENTIFICADOR_LISTAS = "[";

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(BindException.class)
  public ErrosDTO handleBindException(BindException ex) {
    return processarErrosValidacao(ex.getBindingResult());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrosDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    return processarErrosValidacao(ex.getBindingResult());
  }

  private ErrosDTO processarErrosValidacao(BindingResult result) {
    List<FieldError> fieldErrors = result.getFieldErrors();
    List<ValidacaoDTO> validacaoDTOList = new ArrayList<>();

    if (fieldErrors.isEmpty()) {
      result
          .getAllErrors()
          .forEach(
              objectError -> {
                ValidacaoDTO validacaoDTO = new ValidacaoDTO();
                validacaoDTO.setCodes(objectError.getCodes());
                validacaoDTO.setField(objectError.getObjectName());
                validacaoDTOList.add(validacaoDTO);
              });
    } else {
      fieldErrors.forEach(
          fieldError -> {
            ValidacaoDTO validacaoDTO = new ValidacaoDTO();
            validacaoDTO.setCodes(fieldError.getCodes());
            validacaoDTO.setField(fieldError.getField());
            validacaoDTOList.add(validacaoDTO);
          });
    }

    return processarErrosCampos(validacaoDTOList);
  }

  private ErrosDTO processarErrosCampos(List<ValidacaoDTO> validacaoDTOList) {
    List<ErroDTO> erros = new ArrayList<>();

    for (ValidacaoDTO validacaoDTO : validacaoDTOList) {
      String caminhoCampo =
          validacaoDTO.getCodes()[0].substring(
              validacaoDTO.getCodes()[0].lastIndexOf(SEPARADOR_NODES));
      String nomeCampo = caminhoCampo.substring(1);

      boolean possuiLista = validacaoDTO.getCodes()[0].contains(IDENTIFICADOR_LISTAS);
      boolean possuiObjetosEncadeados =
          validacaoDTO.getCodes()[0].chars().filter(ch -> ch == SEPARADOR_NODES).count()
              > QUANTIDADE_NODES_OBJETO;

      if (possuiObjetosEncadeados && !possuiLista) {
        preencherErro(erros, validacaoDTO, caminhoCampo, nomeCampo, CAMINHO_OBJETO_PROPERTIES);
        continue;
      }

      if (possuiLista) {
        preencherErro(erros, validacaoDTO, caminhoCampo, nomeCampo, CAMINHO_LISTA_PROPERTIES);
        continue;
      }

      ErroDTO erro = new ErroDTO();
      erro.setCodigo(EValidacao.ENTRADA_DE_DADOS_INVALIDA.getCodigo());
      String caminhoCampoProperties = ".campo.generico";
      String mensagemProperties =
          obterMensagem(validacaoDTO.getCodes()[0], caminhoCampoProperties, caminhoCampo);
      adicionarErro(erros, erro, format(mensagemProperties, nomeCampo));
    }

    return ErrosDTO.builder().erros(erros).build();
  }

  private void preencherErro(
      List<ErroDTO> erros,
      ValidacaoDTO validacao,
      String caminhoCampo,
      String nomeCampo,
      String caminhoProperties) {

    ErroDTO erro = new ErroDTO();
    erro.setCodigo(EValidacao.ENTRADA_DE_DADOS_INVALIDA.getCodigo());

    String caminhoCampoCompleto = validacao.getField().replaceAll("[^A-Za-z.]", "");
    String nomeGrupo =
        caminhoCampoCompleto.substring(0, caminhoCampoCompleto.indexOf(SEPARADOR_NODES));

    String mensagemProperties =
        obterMensagem(validacao.getCodes()[0], caminhoProperties, caminhoCampo);
    adicionarErro(erros, erro, format(mensagemProperties, nomeCampo, nomeGrupo));
  }

  private String obterMensagem(String fieldErro, String caminhoProperties, String caminhoCampo) {
    String substring = fieldErro.substring(0, fieldErro.indexOf(SEPARADOR_NODES));
    String parametroProperties = substring + caminhoProperties;
    String mensagemProperties = MensagemUtils.getMensagem(parametroProperties);
    String mensagemPropertiesCompleta = MensagemUtils.getMensagem(fieldErro);

    if (parametroProperties.equals(mensagemProperties)
        && mensagemPropertiesCompleta.equals(fieldErro)) {
      return MensagemUtils.getMensagem(substring + caminhoCampo);
    }

    if (!parametroProperties.equals(mensagemProperties)) {
      return mensagemProperties;
    }

    return mensagemPropertiesCompleta;
  }

  private void adicionarErro(List<ErroDTO> erros, ErroDTO novoErro, String mensagem) {
    novoErro.setMensagem(mensagem);
    log.warn(novoErro.getMensagem());
    erros.add(novoErro);
  }
}
