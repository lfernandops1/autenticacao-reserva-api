package com.autenticacao.api.app.domain.entity;

import static com.autenticacao.api.app.Constantes.SCHEMA.SCHEMA_AUTENTICACAO;
import static com.autenticacao.api.app.Constantes.TABELAS.TABELA_REFRESH_TOKEN;
import static com.autenticacao.api.app.Constantes.TABELA_AUTENTICACAO.USUARIO;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = TABELA_REFRESH_TOKEN, schema = SCHEMA_AUTENTICACAO)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = USUARIO, nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private LocalDateTime expiryDate;
}
