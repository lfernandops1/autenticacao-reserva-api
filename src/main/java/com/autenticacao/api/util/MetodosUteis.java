package com.autenticacao.api.util;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MetodosUteis {

  public static UUID gerarCodigoAleatorio() {
    return UUID.randomUUID();
  }

  public static void gerarLogDeAtividade(String mensagem) {
    System.out.println(mensagem);
  }
}
