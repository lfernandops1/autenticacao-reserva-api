package com.autenticacao.api.app.util;

import static com.autenticacao.api.app.Constantes.Http.GET;
import static com.autenticacao.api.app.util.enums.MensagemSistema.ERRO_DURANTE_VERIFICACAO_CAMPOS;
import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidacaoObjetoUtil {

  public boolean verificarTodosCamposNulos(Object dados) {
    Field[] fields = dados.getClass().getDeclaredFields();
    List<Field> fieldsObjeto = Arrays.stream(fields).collect(Collectors.toList());

    if (Objects.nonNull(dados.getClass().getSuperclass())) {
      List<Field> fieldsHeranca =
          Arrays.stream(dados.getClass().getSuperclass().getDeclaredFields()).toList();
      fieldsObjeto.addAll(fieldsHeranca);
    }

    for (Field f : fieldsObjeto) {
      try {
        String nomePropriedade =
            f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
        Method nomeMetodo = dados.getClass().getMethod(GET + nomePropriedade);
        Object valorPropriedade = nomeMetodo.invoke(dados);

        if (Objects.nonNull(valorPropriedade)) {
          if (valorPropriedade instanceof String) return false;

          boolean retornoCascata = verificarTodosCamposNulos(valorPropriedade);
          if (!retornoCascata) return false;
        }
      } catch (NoSuchMethodException e) {
        return false;
      } catch (Exception e) {
        log.error(
            format(
                ERRO_DURANTE_VERIFICACAO_CAMPOS.getChave(),
                dados.getClass().getName(),
                f.getName()),
            e);
        return false;
      }
    }
    return true;
  }
}
