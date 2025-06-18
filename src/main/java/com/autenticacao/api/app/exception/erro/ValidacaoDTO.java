package com.autenticacao.api.app.exception.erro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidacaoDTO {
  private String[] codes;
  private String field;
}
