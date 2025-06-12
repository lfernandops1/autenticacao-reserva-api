package com.autenticacao.api.app.service;

import java.util.Objects;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;

import com.autenticacao.api.app.domain.entity.Usuario;

/**
 * Serviço responsável por comparar dois objetos {@link Usuario} e extrair as diferenças entre os
 * seus campos. Essa abstração melhora a testabilidade e separa responsabilidades.
 */
@Service
public class UsuarioComparadorService {

  /**
   * Compara dois objetos {@link Usuario} e retorna uma descrição das diferenças encontradas entre
   * os seus campos.
   *
   * @param original objeto original para comparação
   * @param modificado objeto modificado para comparação
   * @return ‘String’ com as diferenças formatadas, ou ‘string’ vazia caso não existam diferenças
   */
  public String extrairDiferencas(Usuario original, Usuario modificado) {
    StringJoiner diff = new StringJoiner(System.lineSeparator());

    compararCampo("nome", original.getNome(), modificado.getNome(), diff);
    compararCampo("sobrenome", original.getSobrenome(), modificado.getSobrenome(), diff);
    compararCampo(
        "data_nascimento", original.getDataNascimento(), modificado.getDataNascimento(), diff);
    compararCampo("email", original.getEmail(), modificado.getEmail(), diff);
    compararCampo("telefone", original.getTelefone(), modificado.getTelefone(), diff);

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
