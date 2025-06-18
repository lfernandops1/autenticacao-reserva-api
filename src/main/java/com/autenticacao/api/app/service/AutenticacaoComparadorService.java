package com.autenticacao.api.app.service;

import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Autenticacao;
import com.autenticacao.api.app.domain.entity.Usuario;

/**
 * Serviço responsável por comparar dois objetos {@link Usuario} e extrair as diferenças entre os
 * seus campos. Essa abstração melhora a testabilidade e separa responsabilidades.
 */
@Service
public class AutenticacaoComparadorService {

  /**
   * Compara dois objetos {@link Usuario} e retorna uma descrição das diferenças encontradas entre
   * os seus campos.
   *
   * @param original objeto original para comparação
   * @param modificado objeto modificado para comparação
   * @return ‘String’ com as diferenças formatadas, ou ‘string’ vazia caso não existam diferenças
   */
  public String extrairDiferencas(Autenticacao original, Autenticacao modificado) {
    StringJoiner diff = new StringJoiner(System.lineSeparator());

    compararCampo("email", original.getEmail(), modificado.getEmail(), diff);
    compararCampo("senha", original.getSenha(), modificado.getSenha(), diff);
    compararCampo("ativo", original.getAtivo(), modificado.getAtivo(), diff);
    return diff.toString();
  }

  private void compararCampo(
      String nomeCampo, Object valorOriginal, Object valorModificado, StringJoiner diff) {
    if (!Objects.equals(valorOriginal, valorModificado)) {
      diff.add(
          "Campo '%s': de '%s' para '%s'".formatted(nomeCampo, valorOriginal, valorModificado));
    }
  }
}
