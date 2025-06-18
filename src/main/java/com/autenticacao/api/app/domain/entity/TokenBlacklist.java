package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.Schema.AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.Tabelas.TOKEN_BLACK_LIST;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = TOKEN_BLACK_LIST, schema = AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {
  @Id private String token;

  private Instant expiryDate;
}
