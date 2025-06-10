package com.autenticacao.api.util;

import static com.autenticacao.api.app.Constantes.Mensagens.EMAIL_INVALIDO;
import static com.autenticacao.api.app.Constantes.Mensagens.TELEFONE_INVALIDO;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.autenticacao.api.app.domain.DTO.request.AtualizarUsuarioRequest;
import com.autenticacao.api.app.domain.DTO.request.CadastroUsuarioRequest;

@Component
public class ValidatorUsuarioUtil {

  private static final Pattern TELEFONE_PATTERN = Pattern.compile("^\\d{2}9\\d{8}$");

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(\\.[A-Za-z]{2,})?$");

  public void validarFormatoEmailETelefone(CadastroUsuarioRequest request) {
    validarEmail(request.email());
    validarTelefone(request.telefone());
  }

  public void validarFormatoEmailETelefone(AtualizarUsuarioRequest request) {
    if (request.email() != null) {
      validarEmail(request.email());
    }

    if (request.telefone() != null) {
      validarTelefone(request.telefone());
    }
  }

  private void validarEmail(String email) {
    if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException(EMAIL_INVALIDO + ": " + email);
    }
  }

  private void validarTelefone(String telefone) {
    if (telefone == null || !TELEFONE_PATTERN.matcher(telefone).matches()) {
      throw new IllegalArgumentException(TELEFONE_INVALIDO + ": " + telefone);
    }
  }
}
