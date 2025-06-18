package com.autenticacao.api.app.exception.handler;

import static com.autenticacao.api.app.util.enums.MensagemSistema.ENTRADA_DE_DADOS_INVALIDA;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.autenticacao.api.app.exception.erro.ErroDTO;
import com.autenticacao.api.app.exception.erro.ErrosDTO;
import com.autenticacao.api.app.exception.erro.ValidacaoDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidacaoMetodoHandler {

  private static final char SEPARADOR_NODES = '.';
  private static final String CAMINHO_OBJETO_PROPERTIES = ".objeto.generico";
  private static final String CAMINHO_LISTA_PROPERTIES = ".lista.generico";
  private static final int LIMITE_NODES_OBJETO = 2;
  private static final String IDENTIFICADOR_LISTAS = "[";

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
  public ErrosDTO handleValidacao(Exception ex) {
    BindingResult bindingResult =
        ex instanceof BindException
            ? ((BindException) ex).getBindingResult()
            : ((MethodArgumentNotValidException) ex).getBindingResult();

    return processarErrosValidacao(bindingResult);
  }

  private ErrosDTO processarErrosValidacao(BindingResult result) {
    var erros =
        result.getFieldErrors().stream()
            .map(this::converterParaValidacaoDTO)
            .collect(Collectors.toList());

    // se não tem fieldErrors, pega todos os erros e converte genérico
    if (erros.isEmpty()) {
      erros =
          result.getAllErrors().stream()
              .map(this::converterParaValidacaoDTOGenerico)
              .collect(Collectors.toList());
    }

    return processarErrosCampos(erros);
  }

  private ValidacaoDTO converterParaValidacaoDTO(FieldError fieldError) {
    return ValidacaoDTO.builder().codes(fieldError.getCodes()).field(fieldError.getField()).build();
  }

  private ValidacaoDTO converterParaValidacaoDTOGenerico(ObjectError error) {
    return ValidacaoDTO.builder().codes(error.getCodes()).field(error.getObjectName()).build();
  }

  private ErrosDTO processarErrosCampos(List<ValidacaoDTO> validacoes) {
    List<ErroDTO> erros = new ArrayList<>();

    for (var validacao : validacoes) {
      String codigoPrincipal = validacao.getCodes()[0];
      String caminhoCampo =
          codigoPrincipal.substring(codigoPrincipal.lastIndexOf(SEPARADOR_NODES) + 1);

      boolean temLista = codigoPrincipal.contains(IDENTIFICADOR_LISTAS);
      long countNodes = codigoPrincipal.chars().filter(ch -> ch == SEPARADOR_NODES).count();
      boolean objetosEncadeados = countNodes > LIMITE_NODES_OBJETO;

      String caminhoProperties;
      if (objetosEncadeados && !temLista) {
        caminhoProperties = CAMINHO_OBJETO_PROPERTIES;
      } else if (temLista) {
        caminhoProperties = CAMINHO_LISTA_PROPERTIES;
      } else {
        caminhoProperties = ".campo.generico";
      }

      var erro = new ErroDTO();
      erro.setCodigo(ENTRADA_DE_DADOS_INVALIDA.getCodigo());

      String mensagem =
          obterMensagem(codigoPrincipal, caminhoProperties, caminhoCampo, validacao.getField());
      adicionarErro(erros, erro, mensagem);
    }

    return ErrosDTO.builder().erros(erros).build();
  }

  private String obterMensagem(
      String codigoErro, String caminhoProperties, String nomeCampo, String campoCompleto) {
    // busca base do código para chave geral (ex: entrada.de.dados)
    String baseCodigo = codigoErro.substring(0, codigoErro.indexOf(SEPARADOR_NODES));
    String chaveComposta = baseCodigo + caminhoProperties;

    String mensagemPadrao = getMensagem(chaveComposta);
    String mensagemCompleta = getMensagem(codigoErro);

    if (mensagemPadrao.equals(chaveComposta) && mensagemCompleta.equals(codigoErro)) {
      // fallback para chave mais simples
      return getMensagem(baseCodigo + "." + nomeCampo);
    }

    // se mensagem padrão existe e diferente da chave, usa ela
    if (!mensagemPadrao.equals(chaveComposta)) {
      // se mensagem espera dois params (campo, grupo)
      if (caminhoProperties.equals(CAMINHO_OBJETO_PROPERTIES)
          || caminhoProperties.equals(CAMINHO_LISTA_PROPERTIES)) {
        String nomeGrupo = extrairNomeGrupo(campoCompleto);
        return format(mensagemPadrao, nomeCampo, nomeGrupo);
      }
      return format(mensagemPadrao, nomeCampo);
    }

    return mensagemCompleta;
  }

  private String extrairNomeGrupo(String campoCompleto) {
    String apenasPontos = campoCompleto.replaceAll("[^A-Za-z.]", "");
    int index = apenasPontos.indexOf(SEPARADOR_NODES);
    return index > 0 ? apenasPontos.substring(0, index) : "";
  }

  private void adicionarErro(List<ErroDTO> erros, ErroDTO erro, String mensagem) {
    erro.setMensagem(mensagem);
    log.warn(mensagem);
    erros.add(erro);
  }

  private String getMensagem(String chave) {

    return getMensagem(chave);
  }

  private String format(String mensagem, Object... params) {
    return params.length == 0 ? mensagem : String.format(mensagem, params);
  }
}
